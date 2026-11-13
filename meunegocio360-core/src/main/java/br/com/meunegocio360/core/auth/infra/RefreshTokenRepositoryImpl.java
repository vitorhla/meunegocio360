package br.com.meunegocio360.core.auth.infra;

import br.com.meunegocio360.core.auth.domain.RefreshToken;
import br.com.meunegocio360.core.auth.domain.RefreshTokenRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

  private final RefreshTokenJpaRepository jpaRepository;

  RefreshTokenRepositoryImpl(RefreshTokenJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<RefreshToken> findByTokenHash(String tokenHash) {
    return jpaRepository.findByTokenHash(tokenHash);
  }

  @Override
  public List<RefreshToken> findByFamiliaId(UUID familiaId) {
    return jpaRepository.findByFamiliaId(familiaId);
  }

  @Override
  public RefreshToken save(RefreshToken refreshToken) {
    return jpaRepository.save(refreshToken);
  }

  @Override
  public void deleteAllByExpiraEmBefore(Instant dataLimite) {
    jpaRepository.deleteAllByExpiraEmBefore(dataLimite);
  }

  @Override
  public void deleteAllByUsuarioId(UUID usuarioId) {
    jpaRepository.deleteAllByUsuarioId(usuarioId);
  }
}
