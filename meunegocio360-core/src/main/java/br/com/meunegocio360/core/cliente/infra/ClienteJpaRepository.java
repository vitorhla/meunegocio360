package br.com.meunegocio360.core.cliente.infra;

import br.com.meunegocio360.core.cliente.domain.Cliente;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClienteJpaRepository extends JpaRepository<Cliente, UUID> {

  /**
   * Busca paginada escopada pela empresa (RN-01, RN-07, RN-08). O termo {@code q} casa quando o
   * NOME contém o termo normalizado OU o DOCUMENTO começa com os dígitos informados (OR — RN-08:
   * "busca por nome ou documento"). Os termos são passados prontos (com as âncoras {@code %} e
   * curingas escapados) e NUNCA concatenados na consulta (RNF-BE-26; "Pontos de atenção" do
   * RF-BE-003).
   */
  @Query(
      """
      select c from Cliente c
      where c.empresaId = :empresaId
        and (:situacao is null or c.ativo = :situacao)
        and (
          (:qNome is null and :qDocumento is null)
          or c.nomeBusca like :qNome escape '\\'
          or c.documento like :qDocumento escape '\\'
        )
      """)
  Page<Cliente> buscar(
      @Param("empresaId") UUID empresaId,
      @Param("situacao") Boolean situacao,
      @Param("qNome") String qNome,
      @Param("qDocumento") String qDocumento,
      Pageable pageable);

  Optional<Cliente> findByEmpresaIdAndId(UUID empresaId, UUID id);

  boolean existsByEmpresaIdAndDocumento(UUID empresaId, String documento);

  boolean existsByEmpresaIdAndDocumentoAndIdNot(UUID empresaId, String documento, UUID id);
}
