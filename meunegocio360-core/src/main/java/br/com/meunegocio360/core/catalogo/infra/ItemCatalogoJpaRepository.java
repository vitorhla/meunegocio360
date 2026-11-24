package br.com.meunegocio360.core.catalogo.infra;

import br.com.meunegocio360.core.catalogo.domain.ItemCatalogo;
import br.com.meunegocio360.core.catalogo.domain.TipoItem;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemCatalogoJpaRepository extends JpaRepository<ItemCatalogo, UUID> {

  /**
   * Busca paginada escopada pela empresa (RN-01/RNF-BE-13). Os filtros {@code tipo}, {@code
   * categoriaId} e {@code situacao} combinam entre si com E; o termo {@code q} casa quando o NOME
   * contém o termo normalizado OU o CÓDIGO contém os dígitos/letras informados (OR — RN-10: "busca
   * por nome ou código"). Os termos são passados prontos (com as âncoras {@code %} e curingas
   * escapados) e NUNCA concatenados na consulta (RNF-BE-26).
   */
  @Query(
      """
      select i from ItemCatalogo i
      where i.empresaId = :empresaId
        and (:tipo is null or i.tipo = :tipo)
        and (:categoriaId is null or i.categoriaId = :categoriaId)
        and (:situacao is null or i.ativo = :situacao)
        and (
          (:qNome is null and :qCodigo is null)
          or i.nomeBusca like :qNome escape '\\'
          or i.codigoBusca like :qCodigo escape '\\'
        )
      """)
  Page<ItemCatalogo> buscar(
      @Param("empresaId") UUID empresaId,
      @Param("tipo") TipoItem tipo,
      @Param("categoriaId") UUID categoriaId,
      @Param("situacao") Boolean situacao,
      @Param("qNome") String qNome,
      @Param("qCodigo") String qCodigo,
      Pageable pageable);

  Optional<ItemCatalogo> findByEmpresaIdAndId(UUID empresaId, UUID id);

  boolean existsByEmpresaIdAndCodigo(UUID empresaId, String codigo);

  boolean existsByEmpresaIdAndCodigoAndIdNot(UUID empresaId, String codigo, UUID id);

  long countByEmpresaIdAndCategoriaId(UUID empresaId, UUID categoriaId);
}
