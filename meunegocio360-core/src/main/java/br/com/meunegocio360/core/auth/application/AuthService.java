package br.com.meunegocio360.core.auth.application;

import br.com.meunegocio360.core.auth.domain.Perfil;
import br.com.meunegocio360.core.auth.domain.RefreshToken;
import br.com.meunegocio360.core.auth.domain.RefreshTokenRepository;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.UsuarioRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

  private final UsuarioRepository usuarioRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final TokenService tokenService;
  private final PasswordEncoder passwordEncoder;
  private final Clock clock;
  private final int maxTentativas;
  private final long bloqueioMinutos;
  private final int refreshExpiracaoDias;

  public AuthService(
      UsuarioRepository usuarioRepository,
      RefreshTokenRepository refreshTokenRepository,
      TokenService tokenService,
      PasswordEncoder passwordEncoder,
      Clock clock,
      @Value("${app.auth.max-tentativas}") int maxTentativas,
      @Value("${app.auth.bloqueio-minutos}") long bloqueioMinutos,
      @Value("${app.auth.refresh.expiracao-dias}") int refreshExpiracaoDias) {
    this.usuarioRepository = usuarioRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.tokenService = tokenService;
    this.passwordEncoder = passwordEncoder;
    this.clock = clock;
    this.maxTentativas = maxTentativas;
    this.bloqueioMinutos = bloqueioMinutos;
    this.refreshExpiracaoDias = refreshExpiracaoDias;
  }

  public record LoginResult(String accessToken, int expiresIn, UsuarioData usuario, String refreshTokenValue) {}
  public record UsuarioData(UUID id, String nome, String email, Perfil perfil) {}
  public record RefreshResult(String accessToken, int expiresIn, UsuarioData usuario, String refreshTokenValue) {}

  @Transactional
  public LoginResult login(String email, String senha) {
    Instant agora = Instant.now(clock);
    Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim()).orElse(null);

    if (usuario == null) {
      passwordEncoder.matches(senha, "$2a$10$XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
      throw new InvalidCredentialsException();
    }

    if (usuario.estaBloqueado(agora)) {
      throw new AccountLockedException(usuario.getBloqueadoAte());
    }

    if (!usuario.isAtivo()) {
      throw new InvalidCredentialsException();
    }

    if (!passwordEncoder.matches(senha, usuario.getSenhaHash())) {
      usuario.registrarFalha(maxTentativas, bloqueioMinutos);
      usuarioRepository.save(usuario);
      throw new InvalidCredentialsException();
    }

    usuario.registrarLoginSucesso();
    usuarioRepository.save(usuario);

    Jwt accessToken = tokenService.gerarAccessToken(usuario.getId(), usuario.getPerfil());
    String refreshValor = tokenService.gerarValorRefreshToken();
    String tokenHash = tokenService.hashRefreshToken(refreshValor);

    RefreshToken refreshToken =
        new RefreshToken(usuario.getId(), UUID.randomUUID(), tokenHash,
            agora.plusSeconds(refreshExpiracaoDias * 24L * 3600L));
    refreshTokenRepository.save(refreshToken);

    return new LoginResult(
        accessToken.getTokenValue(),
        tokenService.getExpiracaoMinutos() * 60,
        new UsuarioData(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil()),
        refreshValor);
  }

  @Transactional
  public RefreshResult refresh(String refreshTokenValue) {
    Instant agora = Instant.now(clock);

    if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
      throw new InvalidRefreshTokenException();
    }

    String tokenHash = tokenService.hashRefreshToken(refreshTokenValue);
    RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash).orElse(null);

    if (token == null || token.isExpirado(agora)) {
      if (token != null) {
        token.revogar();
        refreshTokenRepository.save(token);
      }
      throw new InvalidRefreshTokenException();
    }

    if (token.isRevogado()) {
      revogarFamilia(token.getFamiliaId());
      throw new InvalidRefreshTokenException();
    }

    token.revogar();
    refreshTokenRepository.save(token);

    Usuario usuario = usuarioRepository.findById(token.getUsuarioId())
        .orElseThrow(InvalidRefreshTokenException::new);

    if (!usuario.isAtivo() || usuario.estaBloqueado(agora)) {
      throw new InvalidRefreshTokenException();
    }

    String novoValor = tokenService.gerarValorRefreshToken();
    String novoHash = tokenService.hashRefreshToken(novoValor);
    RefreshToken novoToken =
        new RefreshToken(usuario.getId(), token.getFamiliaId(), novoHash,
            agora.plusSeconds(refreshExpiracaoDias * 24L * 3600L));
    refreshTokenRepository.save(novoToken);

    Jwt accessToken = tokenService.gerarAccessToken(usuario.getId(), usuario.getPerfil());

    return new RefreshResult(
        accessToken.getTokenValue(),
        tokenService.getExpiracaoMinutos() * 60,
        new UsuarioData(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil()),
        novoValor);
  }

  @Transactional
  public void logout(String refreshTokenValue) {
    if (refreshTokenValue == null || refreshTokenValue.isBlank()) {
      return;
    }
    String tokenHash = tokenService.hashRefreshToken(refreshTokenValue);
    RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash).orElse(null);
    if (token != null && !token.isRevogado()) {
      revogarFamilia(token.getFamiliaId());
    }
  }

  public UsuarioData me(UUID usuarioId) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Usuário não encontrado"));
    return new UsuarioData(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPerfil());
  }

  private void revogarFamilia(UUID familiaId) {
    for (var t : refreshTokenRepository.findByFamiliaId(familiaId)) {
      if (!t.isRevogado()) {
        t.revogar();
        refreshTokenRepository.save(t);
      }
    }
  }

  public static class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() { super("E-mail ou senha inválidos."); }
  }

  public static class AccountLockedException extends RuntimeException {
    private final Instant bloqueadoAte;
    public AccountLockedException(Instant bloqueadoAte) {
      super("Conta temporariamente bloqueada por tentativas excessivas.");
      this.bloqueadoAte = bloqueadoAte;
    }
    public Instant getBloqueadoAte() { return bloqueadoAte; }
  }

  public static class InvalidRefreshTokenException extends RuntimeException {
    public InvalidRefreshTokenException() { super("Refresh token inválido, expirado ou revogado."); }
  }

  public static class OriginNotAllowedException extends RuntimeException {
    public OriginNotAllowedException() { super("Origem não permitida."); }
  }
}
