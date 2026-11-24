package br.com.meunegocio360.core.catalogo.infra;

import br.com.meunegocio360.core.catalogo.domain.Categoria;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoriaJpaRepository extends JpaRepository<Categoria, UUID> {

  /**
   * Busca paginada escopada pela empresa (RN-01/RNF-BE-13). O termo {@code q} casa quando o NOME
   * contém o termo normalizado (sem caixa/acentos, sobre a coluna gerada {@code nome_busca} —
   * RN-06/RNF-BE-26). O termo é passado pronto (com as âncoras {@code %} e curingas escapados) e
   * NUNCA concatenado na consulta.
   */
  @Query(
      """
      select c from Categoria c
      where c.empresaId = :empresaId
        and (:situacao is null or c.ativo = :situacao)
        and (:qNome is null or c.nomeBusca like :qNome escape '\\')
      """)
  Page<Categoria> buscar(
      @Param("empresaId") UUID empresaId,
      @Param("situacao") Boolean situacao,
      @Param("qNome") String qNome,
      Pageable pageable);

  Optional<Categoria> findByEmpresaIdAndId(UUID empresaId, UUID id);

  boolean existsByEmpresaIdAndNomeBusca(UUID empresaId, String nomeBusca);

  boolean existsByEmpresaIdAndNomeBuscaAndIdNot(UUID empresaId, String nomeBusca, UUID id);
}
