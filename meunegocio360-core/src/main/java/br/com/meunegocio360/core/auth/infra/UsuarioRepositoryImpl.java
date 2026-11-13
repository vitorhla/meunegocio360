package br.com.meunegocio360.core.auth.infra;

import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.UsuarioRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class UsuarioRepositoryImpl implements UsuarioRepository {

  private final UsuarioJpaRepository jpaRepository;

  UsuarioRepositoryImpl(UsuarioJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<Usuario> findByEmailIgnoreCase(String email) {
    return jpaRepository.findByEmailIgnoreCase(email);
  }

  @Override
  public Optional<Usuario> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public Usuario save(Usuario usuario) {
    return jpaRepository.save(usuario);
  }
}
