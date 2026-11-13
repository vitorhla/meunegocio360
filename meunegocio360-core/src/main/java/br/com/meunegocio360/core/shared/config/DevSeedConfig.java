package br.com.meunegocio360.core.shared.config;

import br.com.meunegocio360.core.auth.domain.Perfil;
import br.com.meunegocio360.core.auth.domain.Usuario;
import br.com.meunegocio360.core.auth.domain.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev")
public class DevSeedConfig implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DevSeedConfig.class);

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;

  public DevSeedConfig(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public void run(ApplicationArguments args) {
    String adminSenha = System.getenv("SEED_ADMIN_SENHA");
    String usuarioSenha = System.getenv("SEED_USUARIO_SENHA");

    if (adminSenha == null || adminSenha.isBlank()) {
      log.warn("SEED_ADMIN_SENHA não configurada. Seed não será executado.");
      return;
    }
    if (usuarioSenha == null || usuarioSenha.isBlank()) {
      log.warn("SEED_USUARIO_SENHA não configurada. Seed não será executado.");
      return;
    }

    if (usuarioRepository.findByEmailIgnoreCase("admin@meunegocio360.com.br").isEmpty()) {
      Usuario admin = new Usuario("Administrador", "admin@meunegocio360.com.br",
          passwordEncoder.encode(adminSenha), Perfil.ADMIN);
      usuarioRepository.save(admin);
      log.info("Usuário ADMIN criado: admin@meunegocio360.com.br");
    }

    if (usuarioRepository.findByEmailIgnoreCase("usuario@meunegocio360.com.br").isEmpty()) {
      Usuario usuario = new Usuario("Usuário", "usuario@meunegocio360.com.br",
          passwordEncoder.encode(usuarioSenha), Perfil.USUARIO);
      usuarioRepository.save(usuario);
      log.info("Usuário USUARIO criado: usuario@meunegocio360.com.br");
    }
  }
}
