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
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seed de desenvolvimento (RF-BE-002, RN-08): cria um usuário por perfil (ADMIN, OPERADOR_COMERCIAL
 * e OPERADOR_FINANCEIRO) com senhas lidas do ambiente. NÃO cria empresa, para que o fluxo de
 * primeiro cadastro possa ser verificado (RN-08).
 */
@Configuration
@Profile("dev")
public class DevSeedConfig implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DevSeedConfig.class);

  private final UsuarioRepository usuarioRepository;
  private final PasswordEncoder passwordEncoder;
  private final Environment environment;

  public DevSeedConfig(
      UsuarioRepository usuarioRepository,
      PasswordEncoder passwordEncoder,
      Environment environment) {
    this.usuarioRepository = usuarioRepository;
    this.passwordEncoder = passwordEncoder;
    this.environment = environment;
  }

  @Override
  public void run(ApplicationArguments args) {
    // Lê do Environment do Spring (importa o ../.env via spring.config.import)
    criarUsuarioSeNaoExistir(
        "admin@meunegocio360.com", "Administrador", "SEED_ADMIN_SENHA", Perfil.ADMIN);
    criarUsuarioSeNaoExistir(
        "usuario@meunegocio360.com",
        "Operador Comercial",
        "SEED_USUARIO_SENHA",
        Perfil.OPERADOR_COMERCIAL);
    criarUsuarioSeNaoExistir(
        "financeiro@meunegocio360.com",
        "Operador Financeiro",
        "SEED_FINANCEIRO_SENHA",
        Perfil.OPERADOR_FINANCEIRO);
  }

  private void criarUsuarioSeNaoExistir(
      String email, String nome, String nomeVariavelSenha, Perfil perfil) {
    String senha = environment.getProperty(nomeVariavelSenha);
    if (senha == null || senha.isBlank()) {
      log.warn(
          "{} não configurada. Usuário {} ({}) não será criado.", nomeVariavelSenha, nome, email);
      return;
    }
    if (usuarioRepository.findByEmailIgnoreCase(email).isEmpty()) {
      usuarioRepository.save(new Usuario(nome, email, passwordEncoder.encode(senha), perfil));
      log.info("Usuário {} criado: {}", perfil, email);
    }
  }
}
