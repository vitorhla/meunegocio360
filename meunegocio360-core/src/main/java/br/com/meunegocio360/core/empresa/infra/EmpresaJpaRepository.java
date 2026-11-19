package br.com.meunegocio360.core.empresa.infra;

import br.com.meunegocio360.core.empresa.domain.Empresa;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaJpaRepository extends JpaRepository<Empresa, UUID> {
  Optional<Empresa> findFirstByAtivaTrue();

  boolean existsByCnpj(String cnpj);

  boolean existsByCnpjAndIdNot(String cnpj, UUID id);
}
