package br.com.meunegocio360.core.cliente.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Porta de persistência do cliente (RF-BE-003). Implementada em {@code cliente.infra}. */
public interface ClienteRepository {

  /**
   * Busca paginada escopada pela empresa ativa (RN-07, RN-08; RNF-BE-13/16/26).
   *
   * @param empresaId empresa do escopo da consulta (RNF-BE-26)
   * @param situacao {@code true} só ativos, {@code false} só inativos, {@code null} todos
   * @param qNome termo normalizado (minúsculas, sem acento) para busca por nome; {@code null} se
   *     não houver
   * @param qDocumento prefixo de dígitos para busca por documento (com {@code %}); {@code null} se
   *     não houver
   */
  Page<Cliente> buscar(
      UUID empresaId, Boolean situacao, String qNome, String qDocumento, Pageable pageable);

  /** Consulta pelo id ESCOPADO pela empresa (RN-01; RNF-BE-26). */
  Optional<Cliente> buscarPorEmpresaEId(UUID empresaId, UUID id);

  /**
   * Verifica se o documento já está cadastrado na empresa (RN-03), ativos e inativos.
   *
   * @param idIgnorado cliente a ignorar na verificação (na edição, o próprio cliente); {@code null}
   *     no cadastro
   */
  boolean existePorEmpresaIDocumento(UUID empresaId, String documento, UUID idIgnorado);

  Cliente save(Cliente cliente);

  void delete(Cliente cliente);
}
