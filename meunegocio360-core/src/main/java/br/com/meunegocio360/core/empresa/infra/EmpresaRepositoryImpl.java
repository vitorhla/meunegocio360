package br.com.meunegocio360.core.empresa.infra;

import br.com.meunegocio360.core.empresa.domain.Empresa;
import br.com.meunegocio360.core.empresa.domain.EmpresaRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
class EmpresaRepositoryImpl implements EmpresaRepository {

  private final EmpresaJpaRepository jpaRepository;

  EmpresaRepositoryImpl(EmpresaJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<Empresa> findAtiva() {
    return jpaRepository.findFirstByAtivaTrue();
  }

  @Override
  public Optional<Empresa> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public boolean existsByCnpj(String cnpj, UUID idIgnorado) {
    return idIgnorado == null
        ? jpaRepository.existsByCnpj(cnpj)
        : jpaRepository.existsByCnpjAndIdNot(cnpj, idIgnorado);
  }

  @Override
  public Empresa save(Empresa empresa) {
    // saveAndFlush sincroniza o @Version em memória com o banco antes de a resposta ser
    // montada (RNF-BE-28: a API devolve a nova versao).
    return jpaRepository.saveAndFlush(empresa);
  }
}
