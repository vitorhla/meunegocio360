package br.com.meunegocio360.core.catalogo.infra;

import br.com.meunegocio360.core.catalogo.domain.ItemCatalogo;
import br.com.meunegocio360.core.catalogo.domain.ItemCatalogoRepository;
import br.com.meunegocio360.core.catalogo.domain.TipoItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
class ItemCatalogoRepositoryImpl implements ItemCatalogoRepository {

  private final ItemCatalogoJpaRepository jpaRepository;

  ItemCatalogoRepositoryImpl(ItemCatalogoJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Page<ItemCatalogo> buscar(
      UUID empresaId,
      TipoItem tipo,
      UUID categoriaId,
      Boolean situacao,
      String qNome,
      String qCodigo,
      Pageable pageable) {
    return jpaRepository.buscar(empresaId, tipo, categoriaId, situacao, qNome, qCodigo, pageable);
  }

  @Override
  public Optional<ItemCatalogo> buscarPorEmpresaEId(UUID empresaId, UUID id) {
    return jpaRepository.findByEmpresaIdAndId(empresaId, id);
  }

  @Override
  public boolean existePorEmpresaECodigo(UUID empresaId, String codigo, UUID idIgnorado) {
    return idIgnorado == null
        ? jpaRepository.existsByEmpresaIdAndCodigo(empresaId, codigo)
        : jpaRepository.existsByEmpresaIdAndCodigoAndIdNot(empresaId, codigo, idIgnorado);
  }

  @Override
  public long contarPorEmpresaECategoria(UUID empresaId, UUID categoriaId) {
    return jpaRepository.countByEmpresaIdAndCategoriaId(empresaId, categoriaId);
  }

  @Override
  public ItemCatalogo save(ItemCatalogo item) {
    // saveAndFlush sincroniza o @Version em memória com o banco antes de a resposta ser
    // montada (RNF-BE-28: a API devolve a nova versao).
    return jpaRepository.saveAndFlush(item);
  }

  @Override
  public void delete(ItemCatalogo item) {
    jpaRepository.delete(item);
  }
}
