package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.auth.application.AuthService.LoginResult;
import br.com.meunegocio360.core.auth.application.AuthService.RefreshResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta de login/refresh: JWT de acesso e dados do usuário.")
public record LoginResponse(
    @Schema(description = "Tipo de token (sempre 'Bearer').", example = "Bearer") String tokenType,
    @Schema(
            description =
                "JWT de acesso (HS256, expira em 15 min). Use em: Authorization: Bearer <accessToken>.",
            example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
    @Schema(description = "Validade do token de acesso, em segundos.", example = "900")
        int expiresIn,
    @Schema(description = "Dados do usuário autenticado.") UsuarioResponse usuario) {

  public static LoginResponse from(LoginResult result) {
    return new LoginResponse(
        "Bearer", result.accessToken(), result.expiresIn(), UsuarioResponse.from(result.usuario()));
  }

  public static LoginResponse from(RefreshResult result) {
    return new LoginResponse(
        "Bearer", result.accessToken(), result.expiresIn(), UsuarioResponse.from(result.usuario()));
  }
}
