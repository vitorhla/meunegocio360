package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.Perfil;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final String emissor;
  private final int expiracaoMinutos;
  private final Clock clock;

  public TokenService(
      JwtEncoder jwtEncoder,
      JwtDecoder jwtDecoder,
      @Value("${app.auth.jwt.emissor}") String emissor,
      @Value("${app.auth.jwt.expiracao-minutos}") int expiracaoMinutos,
      Clock clock) {
    this.jwtEncoder = jwtEncoder;
    this.jwtDecoder = jwtDecoder;
    this.emissor = emissor;
    this.expiracaoMinutos = expiracaoMinutos;
    this.clock = clock;
  }

  public Jwt gerarAccessToken(UUID usuarioId, Perfil perfil) {
    Instant now = Instant.now(clock);
    JwtClaimsSet claims =
        JwtClaimsSet.builder()
            .issuer(emissor)
            .subject(usuarioId.toString())
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiracaoMinutos * 60L))
            .id(UUID.randomUUID().toString())
            .claim("perfil", perfil.name())
            .build();

    JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
    return jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
  }

  public Jwt validarAccessToken(String token) {
    return jwtDecoder.decode(token);
  }

  public String gerarValorRefreshToken() {
    byte[] bytes = new byte[32];
    SECURE_RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  public String hashRefreshToken(String valor) {
    try {
      java.security.MessageDigest digest =
          java.security.MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(valor.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(64);
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  public int getExpiracaoMinutos() {
    return expiracaoMinutos;
  }
}
