package br.com.meunegocio360.core.fornecedor.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Repositório de fornecedores (RF-BE-004). Todas as consultas são escopadas pela empresa. */
public interface FornecedorRepository {

  Page<Fornecedor> buscar(
      UUID empresaId, Boolean situacao, String qNome, String qDocumento, Pageable pageable);

  Optional<Fornecedor> buscarPorEmpresaEId(UUID empresaId, UUID id);

  /**
   * Verifica unicidade do documento por empresa, entre fornecedores ativos E inativos (RF-BE-004
   * RN-02). {@code idIgnorado} permite a edição do próprio fornecedor.
   */
  boolean existePorEmpresaIDocumento(UUID empresaId, String documento, UUID idIgnorado);

  Fornecedor save(Fornecedor fornecedor);

  void delete(Fornecedor fornecedor);
}
