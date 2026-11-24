package br.com.meunegocio360.core.catalogo.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Repositório de categorias (RF-BE-005). Todas as consultas são escopadas pela empresa. */
public interface CategoriaRepository {

  Page<Categoria> buscar(UUID empresaId, Boolean situacao, String qNome, Pageable pageable);

  Optional<Categoria> buscarPorEmpresaEId(UUID empresaId, UUID id);

  /**
   * Verifica unicidade do nome por empresa, sem distinguir maiúsculas e acentos (RN-06): compara o
   * nome normalizado com a coluna gerada {@code nome_busca}. {@code idIgnorado} permite o renomeio
   * da própria categoria.
   */
  boolean existePorEmpresaENomeNormalizado(UUID empresaId, String nomeNormalizado, UUID idIgnorado);

  Categoria save(Categoria categoria);

  void delete(Categoria categoria);
}
