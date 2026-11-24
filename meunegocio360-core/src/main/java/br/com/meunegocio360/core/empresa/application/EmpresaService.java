package br.com.meunegocio360.core.empresa.application;

import br.com.meunegocio360.core.empresa.domain.Empresa;
import br.com.meunegocio360.core.empresa.domain.EmpresaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de cadastro de empresa (RF-BE-002).
 *
 * <p>Modelo de empresa única ativa (RN-02); a empresa é inativada, nunca excluída (RN-04). Somente
 * {@code ADMIN} escreve; qualquer perfil autenticado consulta (RN-03).
 */
@Service
@Transactional(readOnly = true)
public class EmpresaService {

  static final String FUSO_PADRAO = "America/Sao_Paulo";
  static final String MOEDA_PADRAO = "BRL";

  private final EmpresaRepository empresaRepository;

  public EmpresaService(EmpresaRepository empresaRepository) {
    this.empresaRepository = empresaRepository;
  }

  public record EmpresaData(
      UUID id,
      String razaoSocial,
      String nomeFantasia,
      String cnpj,
      String email,
      String telefone,
      String fusoHorario,
      String moeda,
      boolean ativa,
      Instant criadoEm,
      Instant atualizadoEm,
      long versao) {

    public static EmpresaData from(Empresa empresa) {
      return new EmpresaData(
          empresa.getId(),
          empresa.getRazaoSocial(),
          empresa.getNomeFantasia(),
          empresa.getCnpj(),
          empresa.getEmail(),
          empresa.getTelefone(),
          empresa.getFusoHorario(),
          empresa.getMoeda(),
          empresa.isAtiva(),
          empresa.getCriadoEm(),
          empresa.getAtualizadoEm(),
          empresa.getVersao());
    }
  }

  /** Consulta a empresa ativa; lança {@link CompanyNotRegisteredException} se não houver. */
  public EmpresaData consultarAtiva() {
    return empresaRepository
        .findAtiva()
        .map(EmpresaData::from)
        .orElseThrow(
            () ->
                new CompanyNotRegisteredException(
                    "Nenhuma empresa ativa cadastrada.", empresaRepository.existsByAtivaFalse()));
  }

  /** Lista todas as empresas (ativas e inativas), da mais antiga para a mais nova. */
  public List<EmpresaData> listar() {
    return empresaRepository.findAll().stream().map(EmpresaData::from).toList();
  }

  /**
   * Cria a empresa (já ativa, RN-05 com os padrões de fuso e moeda). Retorna {@code 409}
   * COMPANY_ALREADY_ACTIVE se já existe uma ativa e COMPANY_CNPJ_DUPLICATE se o CNPJ já está
   * cadastrado (RN-01, RN-02).
   */
  @Transactional
  public EmpresaData cadastrar(
      String razaoSocial,
      String nomeFantasia,
      String cnpj,
      String email,
      String telefone,
      String fusoHorario,
      String moeda) {
    if (empresaRepository.findAtiva().isPresent()) {
      throw new CompanyAlreadyActiveException();
    }
    String cnpjRecortado = cnpj.trim();
    if (empresaRepository.existsByCnpj(cnpjRecortado, null)) {
      throw new CompanyCnpjDuplicateException();
    }
    Empresa empresa =
        new Empresa(
            razaoSocial.trim(),
            opcao(nomeFantasia),
            cnpjRecortado,
            opcao(email),
            opcao(telefone),
            padraoOu(fusoHorario, FUSO_PADRAO),
            padraoOu(moeda, MOEDA_PADRAO));
    return EmpresaData.from(empresaRepository.save(empresa));
  }

  /** Edita a empresa informando a {@code versao} lida (RNF-BE-28; RN-01, RN-06). */
  @Transactional
  public EmpresaData atualizar(
      UUID id,
      long versao,
      String razaoSocial,
      String nomeFantasia,
      String cnpj,
      String email,
      String telefone,
      String fusoHorario,
      String moeda) {
    Empresa empresa = buscarOu404(id);
    verificarVersao(empresa, versao, id);
    String cnpjRecortado = cnpj.trim();
    if (empresaRepository.existsByCnpj(cnpjRecortado, id)) {
      throw new CompanyCnpjDuplicateException();
    }
    empresa.atualizar(
        razaoSocial.trim(),
        opcao(nomeFantasia),
        cnpjRecortado,
        opcao(email),
        opcao(telefone),
        padraoOu(fusoHorario, FUSO_PADRAO),
        padraoOu(moeda, MOEDA_PADRAO));
    return EmpresaData.from(empresaRepository.save(empresa));
  }

  /**
   * Inativa ({@code ativa = false}) ou reativa ({@code ativa = true}) a empresa (RN-02, RN-04).
   * Reativar com outra empresa ativa retorna COMPANY_ALREADY_ACTIVE.
   */
  @Transactional
  public EmpresaData alterarSituacao(UUID id, long versao, boolean ativa) {
    Empresa empresa = buscarOu404(id);
    verificarVersao(empresa, versao, id);
    if (ativa && !empresa.isAtiva() && empresaRepository.findAtiva().isPresent()) {
      throw new CompanyAlreadyActiveException();
    }
    if (ativa) {
      empresa.reativar();
    } else {
      empresa.inativar();
    }
    return EmpresaData.from(empresaRepository.save(empresa));
  }

  private Empresa buscarOu404(UUID id) {
    return empresaRepository
        .findById(id)
        .orElseThrow(
            () ->
                new CompanyNotRegisteredException(
                    "Empresa não encontrada.", empresaRepository.existsByAtivaFalse()));
  }

  private void verificarVersao(Empresa empresa, long versao, UUID id) {
    if (versao != empresa.getVersao()) {
      throw new ObjectOptimisticLockingFailureException(Empresa.class, id);
    }
  }

  /** Campo de texto opcional: valor em branco é tratado como não informado (RN-06). */
  private static String opcao(String valor) {
    if (valor == null) {
      return null;
    }
    String recortado = valor.trim();
    return recortado.isEmpty() ? null : recortado;
  }

  /** Parâmetro opcional com padrão: valor em branco adota o padrão (RN-05). */
  private static String padraoOu(String valor, String padrao) {
    return valor == null || valor.isBlank() ? padrao : valor.trim();
  }

  public static class CompanyNotRegisteredException extends RuntimeException {

    private final boolean existeInativa;

    public CompanyNotRegisteredException() {
      this("Nenhuma empresa ativa cadastrada.", false);
    }

    public CompanyNotRegisteredException(String detail) {
      this(detail, false);
    }

    public CompanyNotRegisteredException(String detail, boolean existeInativa) {
      super(detail);
      this.existeInativa = existeInativa;
    }

    /** Existe empresa inativa no sistema (orienta a reativação na interface). */
    public boolean existeInativa() {
      return existeInativa;
    }
  }

  public static class CompanyCnpjDuplicateException extends RuntimeException {
    public CompanyCnpjDuplicateException() {
      super("CNPJ já cadastrado em outra empresa.");
    }
  }

  public static class CompanyAlreadyActiveException extends RuntimeException {
    public CompanyAlreadyActiveException() {
      super("Já existe uma empresa ativa.");
    }
  }
}
