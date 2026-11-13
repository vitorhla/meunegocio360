package br.com.meunegocio360.core.shared.security;

import br.com.meunegocio360.core.shared.exception.GlobalExceptionHandler;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.Arrays;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final List<String> allowedOrigins;
  private final String jwtSecret;

  public SecurityConfig(
      @Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}") String allowedOrigins,
      @Value("${JWT_SECRET}") String jwtSecret) {
    this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
        .map(String::trim).filter(s -> !s.isEmpty()).toList();
    this.jwtSecret = jwtSecret;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http, GlobalExceptionHandler exceptionHandler) throws Exception {

    http.csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .cors(c -> c.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth ->
            auth.requestMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/logout",
                    "/actuator/health",
                    "/swagger-ui/**",
                    "/v3/api-docs/**")
                .permitAll()
                .anyRequest().authenticated())
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())))
        .exceptionHandling(ex -> {
          ex.authenticationEntryPoint((request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/problem+json");
            response.getWriter().write(exceptionHandler.buildUnauthorized(request, authException));
          });
          ex.accessDeniedHandler((request, response, accessDeniedException) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/problem+json");
            response.getWriter().write(exceptionHandler.buildForbiddenOrigin(request));
          });
        });

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(allowedOrigins);
    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Request-Id"));
    config.setExposedHeaders(Arrays.asList("X-Request-Id", "Retry-After", "WWW-Authenticate"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(buildSecretKey()));
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    return NimbusJwtDecoder.withSecretKey(buildSecretKey()).build();
  }

  private SecretKey buildSecretKey() {
    byte[] keyBytes;
    try {
      keyBytes = java.util.Base64.getDecoder().decode(jwtSecret);
    } catch (IllegalArgumentException e) {
      keyBytes = jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
    if (keyBytes.length < 32) {
      throw new IllegalStateException(
          "JWT_SECRET must be at least 32 bytes. Current: " + keyBytes.length);
    }
    return new SecretKeySpec(keyBytes, "HmacSHA256");
  }
}
