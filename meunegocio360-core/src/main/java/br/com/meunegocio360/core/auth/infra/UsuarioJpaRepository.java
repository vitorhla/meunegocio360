package br.com.meunegocio360.core.auth.infra;

import br.com.meunegocio360.core.auth.domain.Usuario;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioJpaRepository extends JpaRepository<Usuario, UUID> {
  Optional<Usuario> findByEmailIgnoreCase(String email);
}
