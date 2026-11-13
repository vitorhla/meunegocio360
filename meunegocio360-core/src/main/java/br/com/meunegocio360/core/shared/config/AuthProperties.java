package br.com.meunegocio360.core.shared.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthProperties {

  @Bean
  public Clock clock() {
    return Clock.systemUTC();
  }
}
