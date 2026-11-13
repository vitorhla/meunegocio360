package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.RefreshTokenRepository;
import java.time.Clock;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenCleanupScheduler {

  private static final Logger log = LoggerFactory.getLogger(TokenCleanupScheduler.class);

  private final RefreshTokenRepository refreshTokenRepository;
  private final Clock clock;
  private final int limpezaDias;

  public TokenCleanupScheduler(
      RefreshTokenRepository refreshTokenRepository,
      Clock clock,
      @Value("${app.auth.refresh.limpeza-dias:30}") int limpezaDias) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.clock = clock;
    this.limpezaDias = limpezaDias;
  }

  @Scheduled(cron = "0 0 1 * * *")
  public void limparTokensExpirados() {
    Instant dataLimite = Instant.now(clock).minusSeconds(limpezaDias * 24L * 3600L);
    refreshTokenRepository.deleteAllByExpiraEmBefore(dataLimite);
    log.info("Limpeza de refresh tokens expirados concluída.");
  }
}
