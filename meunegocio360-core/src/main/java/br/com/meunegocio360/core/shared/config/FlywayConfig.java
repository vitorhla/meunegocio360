package br.com.meunegocio360.core.shared.config;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Força a inicialização do Flyway antes do EntityManagerFactory (JPA).
 * Sem isso, o Spring Boot 4.x pode não executar as migrations automaticamente.
 */
@Configuration
public class FlywayConfig {

  @Bean
  public Flyway flyway(DataSource dataSource) {
    Flyway flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load();
    flyway.migrate();
    return flyway;
  }
}
