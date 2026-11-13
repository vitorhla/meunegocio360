package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.auth.application.AuthService;
import br.com.meunegocio360.core.auth.application.AuthService.OriginNotAllowedException;
import br.com.meunegocio360.core.application.AuthenticatedUser;
import jakarta.validation.Valid;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
