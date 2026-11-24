package br.com.meunegocio360.core.catalogo.infra;

import br.com.meunegocio360.core.catalogo.domain.Categoria;
import br.com.meunegocio360.core.catalogo.domain.CategoriaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
class CategoriaRepositoryImpl implements CategoriaRepository {

  private final CategoriaJpaRepository jpaRepository;

  CategoriaRepositoryImpl(CategoriaJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Page<Categoria> buscar(UUID empresaId, Boolean situacao, String qNome, Pageable pageable) {
    return jpaRepository.buscar(empresaId, situacao, qNome, pageable);
  }

  @Override
  public Optional<Categoria> buscarPorEmpresaEId(UUID empresaId, UUID id) {
    return jpaRepository.findByEmpresaIdAndId(empresaId, id);
  }

  @Override
  public boolean existePorEmpresaENomeNormalizado(
      UUID empresaId, String nomeNormalizado, UUID idIgnorado) {
    return idIgnorado == null
        ? jpaRepository.existsByEmpresaIdAndNomeBusca(empresaId, nomeNormalizado)
        : jpaRepository.existsByEmpresaIdAndNomeBuscaAndIdNot(
            empresaId, nomeNormalizado, idIgnorado);
  }

  @Override
  public Categoria save(Categoria categoria) {
    // saveAndFlush sincroniza o @Version em memória com o banco antes de a resposta ser
    // montada (RNF-BE-28: a API devolve a nova versao).
    return jpaRepository.saveAndFlush(categoria);
  }

  @Override
  public void delete(Categoria categoria) {
    jpaRepository.delete(categoria);
  }
}
