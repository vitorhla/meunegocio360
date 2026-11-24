package br.com.meunegocio360.core.fornecedor.infra;

import br.com.meunegocio360.core.fornecedor.domain.Fornecedor;
import br.com.meunegocio360.core.fornecedor.domain.FornecedorRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
class FornecedorRepositoryImpl implements FornecedorRepository {

  private final FornecedorJpaRepository jpaRepository;

  FornecedorRepositoryImpl(FornecedorJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Page<Fornecedor> buscar(
      UUID empresaId, Boolean situacao, String qNome, String qDocumento, Pageable pageable) {
    return jpaRepository.buscar(empresaId, situacao, qNome, qDocumento, pageable);
  }

  @Override
  public Optional<Fornecedor> buscarPorEmpresaEId(UUID empresaId, UUID id) {
    return jpaRepository.findByEmpresaIdAndId(empresaId, id);
  }

  @Override
  public boolean existePorEmpresaIDocumento(UUID empresaId, String documento, UUID idIgnorado) {
    return idIgnorado == null
        ? jpaRepository.existsByEmpresaIdAndDocumento(empresaId, documento)
        : jpaRepository.existsByEmpresaIdAndDocumentoAndIdNot(empresaId, documento, idIgnorado);
  }

  @Override
  public Fornecedor save(Fornecedor fornecedor) {
    // saveAndFlush sincroniza o @Version em memória com o banco antes de a resposta ser
    // montada (RNF-BE-28: a API devolve a nova versao).
    return jpaRepository.saveAndFlush(fornecedor);
  }

  @Override
  public void delete(Fornecedor fornecedor) {
    jpaRepository.delete(fornecedor);
  }
}
