package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.auth.application.AuthService;
import br.com.meunegocio360.core.auth.application.AuthService.OriginNotAllowedException;
import br.com.meunegocio360.core.application.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Autenticação",
    description =
        "Login por e-mail/senha, rotação de refresh token, logout e dados do usuário"
            + " autenticado (RF-BE-001).")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private static final String COOKIE_NAME = "mn360_refresh";

  private final AuthService authService;
  private final int refreshExpiracaoDias;
  private final boolean cookieSecure;
  private final List<String> allowedOrigins;

  public AuthController(
      AuthService authService,
      @Value("${app.auth.refresh.expiracao-dias}") int refreshExpiracaoDias,
      @Value("${app.auth.cookie.secure}") boolean cookieSecure,
      @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}") String allowedOrigins) {
    this.authService = authService;
    this.refreshExpiracaoDias = refreshExpiracaoDias;
    this.cookieSecure = cookieSecure;
    this.allowedOrigins = java.util.Arrays.stream(allowedOrigins.split(","))
        .map(String::trim).filter(s -> !s.isEmpty()).toList();
  }

  @Operation(
      summary = "Autentica o usuário por e-mail e senha",
      description =
          "Retorna o JWT de acesso (15 min) no corpo e emite o refresh token (7 dias) no cookie"
              + " HttpOnly `mn360_refresh`. E-mail e senha inválidos retornam o mesmo erro 401"
              + " (INVALID_CREDENTIALS) para não revelar qual dos dois está errado. Após 5 falhas"
              + " consecutivas o usuário é bloqueado por 15 minutos (429 + Retry-After).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login bem-sucedido. JWT no corpo e cookie `mn360_refresh` emitido."),
        @ApiResponse(
            responseCode = "400",
            description = "Payload inválido (código VALIDATION_ERROR).",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas (código INVALID_CREDENTIALS).",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(
            responseCode = "403",
            description = "Origin não permitida (código ORIGIN_NOT_ALLOWED).",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(
            responseCode = "429",
            description =
                "Conta temporariamente bloqueada por tentativas falhas (código"
                    + " ACCOUNT_TEMPORARILY_LOCKED, header Retry-After).",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
      })
  @PostMapping("/login")
  public ResponseEntity<LoginResponse> login(
      @Valid @RequestBody LoginRequest request,
      @RequestHeader(value = "Origin", required = false) String origin) {

    verificarOrigin(origin);
    AuthService.LoginResult result = authService.login(request.email(), request.senha());
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, criarCookieRefresh(result.refreshTokenValue()))
        .body(LoginResponse.from(result));
  }

  @Operation(
      summary = "Rotaciona o refresh token e emite um novo JWT de acesso",
      description =
          "O refresh token enviado no cookie é revogado e um novo é emitido no cookie"
              + " (rotação). Se um token já revogado for reapresentado, a família inteira é"
              + " revogada (detecção de reuso).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Novo JWT no corpo e novo cookie `mn360_refresh` (token rotacionado)."),
        @ApiResponse(
            responseCode = "401",
            description =
                "Refresh token ausente, expirado, revogado ou desconhecido (código"
                    + " INVALID_REFRESH_TOKEN).",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE)),
        @ApiResponse(
            responseCode = "403",
            description = "Origin não permitida (código ORIGIN_NOT_ALLOWED).",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
      })
  @PostMapping("/refresh")
  public ResponseEntity<LoginResponse> refresh(
      @CookieValue(name = COOKIE_NAME, required = false) String refreshToken,
      @RequestHeader(value = "Origin", required = false) String origin) {

    verificarOrigin(origin);
    AuthService.RefreshResult result = authService.refresh(refreshToken);
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, criarCookieRefresh(result.refreshTokenValue()))
        .body(LoginResponse.from(result));
  }

  @Operation(
      summary = "Logout (revoga a família de refresh tokens)",
      description =
          "Revoga todos os refresh tokens da família do cookie recebido e expira o cookie."
              + " Operação idempotente: sempre responde 204, mesmo sem cookie válido.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "204",
            description = "Logout processado (ou já estava deslogado). Cookie expirado."),
        @ApiResponse(
            responseCode = "403",
            description = "Origin não permitida (código ORIGIN_NOT_ALLOWED).",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
      })
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(
      @CookieValue(name = COOKIE_NAME, required = false) String refreshToken,
      @RequestHeader(value = "Origin", required = false) String origin) {

    verificarOrigin(origin);
    authService.logout(refreshToken);
    return ResponseEntity.noContent()
        .header(HttpHeaders.SET_COOKIE, expirarCookieRefresh())
        .build();
  }

  @Operation(
      summary = "Dados do usuário autenticado",
      description =
          "Retorna os dados do usuário do JWT apresentado no header"
              + " `Authorization: Bearer <accessToken>`.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Dados do usuário autenticado."),
        @ApiResponse(
            responseCode = "401",
            description =
                "Token ausente, inválido, adulterado, expirado ou com assinatura"
                    + " incorreta (código UNAUTHORIZED).",
            content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE))
      })
  @GetMapping("/me")
  public ResponseEntity<UsuarioResponse> me(AuthenticatedUser authenticatedUser) {
    var data = authService.me(authenticatedUser.usuarioId());
    return ResponseEntity.ok(UsuarioResponse.from(data));
  }

  private String criarCookieRefresh(String value) {
    return ResponseCookie.from(COOKIE_NAME, value)
        .httpOnly(true).secure(cookieSecure).sameSite("Strict")
        .path("/api/v1/auth").maxAge(Duration.ofDays(refreshExpiracaoDias))
        .build().toString();
  }

  private String expirarCookieRefresh() {
    return ResponseCookie.from(COOKIE_NAME, "")
        .httpOnly(true).secure(cookieSecure).sameSite("Strict")
        .path("/api/v1/auth").maxAge(0)
        .build().toString();
  }

  private void verificarOrigin(String origin) {
    if (origin != null && !origin.isBlank() && !allowedOrigins.contains(origin)) {
      throw new OriginNotAllowedException();
    }
  }
}
