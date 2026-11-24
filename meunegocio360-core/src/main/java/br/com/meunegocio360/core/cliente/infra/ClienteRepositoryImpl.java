package br.com.meunegocio360.core.cliente.infra;

import br.com.meunegocio360.core.cliente.domain.Cliente;
import br.com.meunegocio360.core.cliente.domain.ClienteRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
class ClienteRepositoryImpl implements ClienteRepository {

  private final ClienteJpaRepository jpaRepository;

  ClienteRepositoryImpl(ClienteJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Page<Cliente> buscar(
      UUID empresaId, Boolean situacao, String qNome, String qDocumento, Pageable pageable) {
    return jpaRepository.buscar(empresaId, situacao, qNome, qDocumento, pageable);
  }

  @Override
  public Optional<Cliente> buscarPorEmpresaEId(UUID empresaId, UUID id) {
    return jpaRepository.findByEmpresaIdAndId(empresaId, id);
  }

  @Override
  public boolean existePorEmpresaIDocumento(UUID empresaId, String documento, UUID idIgnorado) {
    return idIgnorado == null
        ? jpaRepository.existsByEmpresaIdAndDocumento(empresaId, documento)
        : jpaRepository.existsByEmpresaIdAndDocumentoAndIdNot(empresaId, documento, idIgnorado);
  }

  @Override
  public Cliente save(Cliente cliente) {
    // saveAndFlush sincroniza o @Version em memória com o banco antes de a resposta ser
    // montada (RNF-BE-28: a API devolve a nova versao).
    return jpaRepository.saveAndFlush(cliente);
  }

  @Override
  public void delete(Cliente cliente) {
    jpaRepository.delete(cliente);
  }
}
