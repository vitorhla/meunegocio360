package br.com.meunegocio360.core.catalogo.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Repositório de itens de catálogo (RF-BE-005). Todas as consultas são escopadas pela empresa. */
public interface ItemCatalogoRepository {

  /**
   * Busca paginada com filtros combinados com E (RN-10): {@code tipo}, {@code categoriaId} e {@code
   * situacao}, mais a busca livre {@code qNome}/{@code qCodigo} (OR entre os dois — "busca por nome
   * ou código").
   */
  Page<ItemCatalogo> buscar(
      UUID empresaId,
      TipoItem tipo,
      UUID categoriaId,
      Boolean situacao,
      String qNome,
      String qCodigo,
      Pageable pageable);

  Optional<ItemCatalogo> buscarPorEmpresaEId(UUID empresaId, UUID id);

  /**
   * Verifica unicidade do código por empresa quando informado (RN-04). {@code idIgnorado} permite a
   * edição do próprio item.
   */
  boolean existePorEmpresaECodigo(UUID empresaId, String codigo, UUID idIgnorado);

  /** Quantidade de itens vinculados à categoria (ativos e inativos) na empresa (RN-06). */
  long contarPorEmpresaECategoria(UUID empresaId, UUID categoriaId);

  ItemCatalogo save(ItemCatalogo item);

  void delete(ItemCatalogo item);
}
