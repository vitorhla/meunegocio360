package br.com.meunegocio360.core.fornecedor.infra;

import br.com.meunegocio360.core.fornecedor.domain.Fornecedor;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FornecedorJpaRepository extends JpaRepository<Fornecedor, UUID> {

  /**
   * Busca paginada escopada pela empresa (RF-BE-003 RN-01/07/08). O termo {@code q} casa quando o
   * NOME contém o termo normalizado OU o DOCUMENTO começa com os dígitos informados (OR — "busca
   * por nome ou documento"). Os termos são passados prontos (com as âncoras {@code %} e curingas
   * escapados) e NUNCA concatenados na consulta (RNF-BE-26; "Pontos de atenção" do RF-BE-003/004).
   */
  @Query(
      """
      select f from Fornecedor f
      where f.empresaId = :empresaId
        and (:situacao is null or f.ativo = :situacao)
        and (
          (:qNome is null and :qDocumento is null)
          or f.nomeBusca like :qNome escape '\\'
          or f.documento like :qDocumento escape '\\'
        )
      """)
  Page<Fornecedor> buscar(
      @Param("empresaId") UUID empresaId,
      @Param("situacao") Boolean situacao,
      @Param("qNome") String qNome,
      @Param("qDocumento") String qDocumento,
      Pageable pageable);

  Optional<Fornecedor> findByEmpresaIdAndId(UUID empresaId, UUID id);

  boolean existsByEmpresaIdAndDocumento(UUID empresaId, String documento);

  boolean existsByEmpresaIdAndDocumentoAndIdNot(UUID empresaId, String documento, UUID id);
}
