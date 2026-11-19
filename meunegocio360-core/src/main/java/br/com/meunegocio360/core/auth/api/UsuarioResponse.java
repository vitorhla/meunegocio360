package br.com.meunegocio360.core.auth.api;

import br.com.meunegocio360.core.auth.application.AuthService.UsuarioData;
import br.com.meunegocio360.core.auth.domain.Perfil;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Dados públicos do usuário.")
public record UsuarioResponse(
    @Schema(description = "Identificador único do usuário.", example = "6f1e...-uuid") UUID id,
    @Schema(description = "Nome completo do usuário.", example = "Administrador") String nome,
    @Schema(description = "E-mail do usuário.", example = "admin@meunegocio360.com.br")
        String email,
    @Schema(description = "Perfil de acesso do usuário.", example = "ADMIN") Perfil perfil) {

  public static UsuarioResponse from(UsuarioData data) {
    return new UsuarioResponse(data.id(), data.nome(), data.email(), data.perfil());
  }
}
