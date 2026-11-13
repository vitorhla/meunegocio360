package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.auth.application.AuthService.LoginResult;
import br.com.meunegocio360.core.auth.application.AuthService.RefreshResult;

public record LoginResponse(String tokenType, String accessToken, int expiresIn, UsuarioResponse usuario) {

  public static LoginResponse from(LoginResult result) {
    return new LoginResponse("Bearer", result.accessToken(), result.expiresIn(),
        UsuarioResponse.from(result.usuario()));
  }

  public static LoginResponse from(RefreshResult result) {
    return new LoginResponse("Bearer", result.accessToken(), result.expiresIn(),
        UsuarioResponse.from(result.usuario()));
  }
}
