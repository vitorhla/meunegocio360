package br.com.meunegocio360.core.auth.domain;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioRepository {
  Optional<Usuario> findByEmailIgnoreCase(String email);
  Optional<Usuario> findById(UUID id);
  Usuario save(Usuario usuario);
}
