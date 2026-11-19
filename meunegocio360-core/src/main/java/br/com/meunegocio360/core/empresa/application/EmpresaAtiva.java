package br.com.meunegocio360.core.empresa.application;

import br.com.meunegocio360.core.empresa.domain.Empresa;
import br.com.meunegocio360.core.empresa.domain.EmpresaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Mecanismo transversal que exige empresa ativa (RF-BE-002, RNF-096).
 *
 * <p>Os demais casos de uso (clientes, fornecedores, catálogo — RF-BE-003 em diante) resolvem o
 * {@code empresa_id} aqui, <strong>no servidor</strong>; o cliente NUNCA o informa. Sem empresa
 * ativa, a criação de registros de negócio falha com {@code 422} COMPANY_NOT_ACTIVE (RN-04).
 */
@Service
public class EmpresaAtiva {

  private final EmpresaRepository empresaRepository;

  public EmpresaAtiva(EmpresaRepository empresaRepository) {
    this.empresaRepository = empresaRepository;
  }

  /**
   * Resolve o {@code empresa_id} da empresa ativa no servidor.
   *
   * @throws CompanyNotActiveException se não houver empresa ativa (422 COMPANY_NOT_ACTIVE)
   */
  public UUID obterId() {
    return empresaRepository
        .findAtiva()
        .map(Empresa::getId)
        .orElseThrow(CompanyNotActiveException::new);
  }

  public static class CompanyNotActiveException extends RuntimeException {
    public CompanyNotActiveException() {
      super(
          "Operação exige empresa ativa. Cadastre e ative a empresa antes de executar"
              + " esta operação.");
    }
  }
}
