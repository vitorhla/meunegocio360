package br.com.meunegocio360.core.auth.infra;

import br.com.meunegocio360.core.auth.domain.RefreshToken;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, UUID> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findByFamiliaId(UUID familiaId);

  @Modifying
  @Query("delete from RefreshToken r where r.expiraEm < :dataLimite")
  void deleteAllByExpiraEmBefore(@Param("dataLimite") Instant dataLimite);

  @Modifying
  @Query("delete from RefreshToken r where r.usuarioId = :usuarioId")
  void deleteAllByUsuarioId(@Param("usuarioId") UUID usuarioId);
}
