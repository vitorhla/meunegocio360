package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.auth.application.AuthService.UsuarioData;
import br.com.meunegocio360.core.auth.domain.Perfil;
import java.util.UUID;

public record UsuarioResponse(UUID id, String nome, String email, Perfil perfil) {
  public static UsuarioResponse from(UsuarioData data) {
    return new UsuarioResponse(data.id(), data.nome(), data.email(), data.perfil());
  }
}
