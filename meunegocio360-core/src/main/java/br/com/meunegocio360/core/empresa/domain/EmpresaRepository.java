package br.com.meunegocio360.core.empresa.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Porta de persistência da empresa (RF-BE-002). Implementada em {@code empresa.infra}. */
public interface EmpresaRepository {

  /** No máximo uma empresa ativa por instalação (RN-02). */
  Optional<Empresa> findAtiva();

  /** Todas as empresas (ativas e inativas), da mais antiga para a mais nova. */
  List<Empresa> findAll();

  /** Existe alguma empresa inativa? (orienta a reativação quando não há ativa) */
  boolean existsByAtivaFalse();

  Optional<Empresa> findById(UUID id);

  /**
   * Verifica se o CNPJ já está cadastrado (RN-01), ativas ou inativas.
   *
   * @param idIgnorado empresa a ignorar na verificação (na edição, a própria empresa); {@code null}
   *     no cadastro
   */
  boolean existsByCnpj(String cnpj, UUID idIgnorado);

  Empresa save(Empresa empresa);
}
