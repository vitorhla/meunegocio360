package br.com.meunegocio360.core.fornecedor.application;

import br.com.meunegocio360.core.empresa.application.EmpresaAtiva;
import br.com.meunegocio360.core.fornecedor.domain.Fornecedor;
import br.com.meunegocio360.core.fornecedor.domain.FornecedorRepository;
import br.com.meunegocio360.core.fornecedor.domain.VerificadorVinculoFornecedor;
import br.com.meunegocio360.core.shared.domain.TipoPessoa;
import java.text.Normalizer;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de cadastro de fornecedores (RF-BE-004). Segue o mesmo desenho de RF-BE-003 (cujo
 * RN-01 a RN-09 se aplicam sem alteração, conforme RN-01 deste requisito): todo fornecedor pertence
 * à empresa ativa, resolvida no servidor (RNF-096); o documento é único por empresa, ativos e
 * inativos (RN-02); fornecedor com vínculo é inativado, nunca excluído (RN-03, RNF-097).
 */
@Service
@Transactional(readOnly = true)
public class FornecedorService {

  static final int TAMANHO_PAGINA_MAXIMO = 100;
  private static final Set<String> CAMPOS_SORT = Set.of("nome", "criadoEm");

  private final FornecedorRepository fornecedorRepository;
  private final EmpresaAtiva empresaAtiva;
  private final List<VerificadorVinculoFornecedor> verificadoresVinculo;

  public FornecedorService(
      FornecedorRepository fornecedorRepository,
      EmpresaAtiva empresaAtiva,
      List<VerificadorVinculoFornecedor> verificadoresVinculo) {
    this.fornecedorRepository = fornecedorRepository;
    this.empresaAtiva = empresaAtiva;
    this.verificadoresVinculo = verificadoresVinculo;
  }

  public record FornecedorData(
      UUID id,
      TipoPessoa tipoPessoa,
      String nome,
      String documento,
      String email,
      String telefone,
      boolean ativo,
      Instant criadoEm,
      Instant atualizadoEm,
      long versao) {

    public static FornecedorData from(Fornecedor fornecedor) {
      return new FornecedorData(
          fornecedor.getId(),
          fornecedor.getTipoPessoa(),
          fornecedor.getNome(),
          fornecedor.getDocumento(),
          fornecedor.getEmail(),
          fornecedor.getTelefone(),
          fornecedor.isAtivo(),
          fornecedor.getCriadoEm(),
          fornecedor.getAtualizadoEm(),
          fornecedor.getVersao());
    }
  }

  /** Resposta paginada no formato padrão (RNF-BE-13). */
  public record FornecedorPageData(
      List<FornecedorData> content, int page, int size, long totalElements, int totalPages) {

    public static FornecedorPageData from(Page<Fornecedor> pagina) {
      return new FornecedorPageData(
          pagina.map(FornecedorData::from).getContent(),
          pagina.getNumber(),
          pagina.getSize(),
          pagina.getTotalElements(),
          pagina.getTotalPages());
    }

    public static FornecedorPageData vazia(int page, int size) {
      return new FornecedorPageData(List.of(), page, size, 0, 0);
    }
  }

  /**
   * Lista os fornecedores da empresa ativa (RF-BE-003 RN-07/RN-08). Sem empresa ativa não há escopo
   * de consulta e a listagem é vazia. Busca por nome (sem caixa/acentos) e/ou por prefixo de
   * documento, combinada com o filtro de situação (RNF-BE-13/16/26).
   */
  public FornecedorPageData listar(String q, String situacao, int page, int size, String sort) {
    validarPaginacao(page, size);
    Sort ordenacao = ordenacaoDe(sort);
    Boolean filtroSituacao = filtroDeSituacao(situacao);

    Optional<UUID> empresaId = empresaIdDaEmpresaAtiva();
    if (empresaId.isEmpty()) {
      return FornecedorPageData.vazia(page, size);
    }
    TermosBusca termos = termosDe(q);
    PageRequest pagina = PageRequest.of(page, size, ordenacao);
    return FornecedorPageData.from(
        fornecedorRepository.buscar(
            empresaId.get(),
            filtroSituacao,
            termos == null ? null : termos.nome(),
            termos == null ? null : termos.documento(),
            pagina));
  }

  /**
   * Consulta um fornecedor da empresa ativa; 404 SUPPLIER_NOT_FOUND em caso contrário (RF-BE-003
   * RN-01).
   */
  public FornecedorData consultar(UUID id) {
    return fornecedorRepository
        .buscarPorEmpresaEId(empresaIdObrigatorioParaLer(), id)
        .map(FornecedorData::from)
        .orElseThrow(SupplierNotFoundException::new);
  }

  /**
   * Cadastra um fornecedor ativo na empresa ativa (RF-BE-003 RN-01 a RN-04). Sem empresa ativa, 422
   * COMPANY_NOT_ACTIVE (RNF-096).
   */
  @Transactional
  public FornecedorData cadastrar(
      TipoPessoa tipoPessoa, String nome, String documento, String email, String telefone) {
    UUID empresaId = empresaAtiva.obterId();
    String documentoRecortado = somenteDigitos(documento);
    verificarDocumentoUnico(empresaId, documentoRecortado, null);
    Fornecedor fornecedor =
        new Fornecedor(
            empresaId, tipoPessoa, nome.trim(), documentoRecortado, opcao(email), opcao(telefone));
    return FornecedorData.from(fornecedorRepository.save(fornecedor));
  }

  /**
   * Edita o fornecedor informando a {@code versao} lida (RF-BE-003 RN-09, RNF-BE-28). Tipo e
   * documento podem ser corrigidos, respeitando a validação (RN-02) e a unicidade por empresa
   * (RF-BE-004 RN-02).
   */
  @Transactional
  public FornecedorData atualizar(
      UUID id,
      long versao,
      TipoPessoa tipoPessoa,
      String nome,
      String documento,
      String email,
      String telefone) {
    Fornecedor fornecedor = buscarNaEmpresaAtiva(id);
    verificarVersao(fornecedor, versao, id);
    String documentoRecortado = somenteDigitos(documento);
    verificarDocumentoUnico(fornecedor.getEmpresaId(), documentoRecortado, id);
    fornecedor.atualizar(
        tipoPessoa, nome.trim(), documentoRecortado, opcao(email), opcao(telefone));
    return FornecedorData.from(fornecedorRepository.save(fornecedor));
  }

  /**
   * Inativa ({@code ativo = false}) ou reativa ({@code ativo = true}) o fornecedor (RF-BE-003
   * RN-07). As duas operações são idempotentes: repetir a mesma situação não gera nova versão.
   */
  @Transactional
  public FornecedorData alterarSituacao(UUID id, long versao, boolean ativo) {
    Fornecedor fornecedor = buscarNaEmpresaAtiva(id);
    verificarVersao(fornecedor, versao, id);
    if (ativo) {
      fornecedor.reativar();
    } else {
      fornecedor.inativar();
    }
    return FornecedorData.from(fornecedorRepository.save(fornecedor));
  }

  /**
   * Exclui o fornecedor somente se NENHUM {@link VerificadorVinculoFornecedor} registrado apontar
   * vínculo (pedido de compra ou conta a pagar) — RF-BE-004 RN-03. Com vínculo, 422
   * SUPPLIER_HAS_HISTORY (RNF-097).
   */
  @Transactional
  public void excluir(UUID id) {
    Fornecedor fornecedor = buscarNaEmpresaAtiva(id);
    for (VerificadorVinculoFornecedor verificador : verificadoresVinculo) {
      if (verificador.possuiVinculo(id)) {
        throw new SupplierHasHistoryException();
      }
    }
    fornecedorRepository.delete(fornecedor);
  }

  private Fornecedor buscarNaEmpresaAtiva(UUID id) {
    UUID empresaId = empresaIdDaEmpresaAtiva().orElseThrow(SupplierNotFoundException::new);
    return fornecedorRepository
        .buscarPorEmpresaEId(empresaId, id)
        .orElseThrow(SupplierNotFoundException::new);
  }

  private UUID empresaIdObrigatorioParaLer() {
    return empresaIdDaEmpresaAtiva().orElseThrow(SupplierNotFoundException::new);
  }

  /** Id da empresa ativa ou vazio; a feature NÃO expõe a falta de empresa como erro de leitura. */
  private Optional<UUID> empresaIdDaEmpresaAtiva() {
    try {
      return Optional.of(empresaAtiva.obterId());
    } catch (EmpresaAtiva.CompanyNotActiveException e) {
      return Optional.empty();
    }
  }

  private void verificarDocumentoUnico(UUID empresaId, String documento, UUID idIgnorado) {
    if (fornecedorRepository.existePorEmpresaIDocumento(empresaId, documento, idIgnorado)) {
      throw new SupplierDocumentDuplicateException();
    }
  }

  private void verificarVersao(Fornecedor fornecedor, long versao, UUID id) {
    if (versao != fornecedor.getVersao()) {
      throw new ObjectOptimisticLockingFailureException(Fornecedor.class, id);
    }
  }

  private void validarPaginacao(int page, int size) {
    if (page < 0) {
      throw new InvalidQueryException("page deve ser 0 ou maior.");
    }
    if (size < 1 || size > TAMANHO_PAGINA_MAXIMO) {
      throw new InvalidQueryException("size deve estar entre 1 e " + TAMANHO_PAGINA_MAXIMO + ".");
    }
  }

  /**
   * Caminhos permitidos para {@code sort} (RNF-BE-16): {@code nome} (padrão) ou {@code criadoEm},
   * com direção opcional {@code ,asc}/{@code ,desc}. Qualquer outro valor é 400.
   */
  private Sort ordenacaoDe(String sort) {
    String valor = (sort == null || sort.isBlank()) ? "nome" : sort.trim();
    String[] partes = valor.split(",", 2);
    String campo = partes[0].trim().toLowerCase(Locale.ROOT);
    if (!CAMPOS_SORT.contains(campo)) {
      throw new InvalidQueryException(
          "sort: campo inválido ('" + partes[0].trim() + "'); campos permitidos: nome, criadoEm.");
    }
    Sort.Direction direcao = Sort.Direction.ASC;
    if (partes.length == 2 && !partes[1].isBlank()) {
      String direcaoTexto = partes[1].trim().toLowerCase(Locale.ROOT);
      if ("asc".equals(direcaoTexto)) {
        direcao = Sort.Direction.ASC;
      } else if ("desc".equals(direcaoTexto)) {
        direcao = Sort.Direction.DESC;
      } else {
        throw new InvalidQueryException(
            "sort: direção inválida ('" + partes[1].trim() + "'); use asc ou desc.");
      }
    }
    return Sort.by(direcao, campo);
  }

  /**
   * {@code ATIVO} (padrão) → só ativos; {@code INATIVO} → só inativos; {@code TODOS} → sem filtro.
   */
  private Boolean filtroDeSituacao(String situacao) {
    String valor =
        (situacao == null || situacao.isBlank())
            ? "ATIVO"
            : situacao.trim().toUpperCase(Locale.ROOT);
    return switch (valor) {
      case "ATIVO" -> true;
      case "INATIVO" -> false;
      case "TODOS" -> null;
      default ->
          throw new InvalidQueryException(
              "situacao: valor inválido ('" + situacao + "'); use ATIVO, INATIVO ou TODOS.");
    };
  }

  /**
   * Converte o termo livre {@code q} (RF-BE-003 RN-08) em dois termos de busca combinados com OR:
   * nome (contém, sem caixa/acentos, sobre a coluna gerada {@code nome_busca}) e prefixo de
   * documento (somente dígitos informados, máscara ignorada).
   */
  private TermosBusca termosDe(String q) {
    if (q == null || q.trim().isEmpty()) {
      return null;
    }
    String termo = q.trim();
    String digitos = termo.replaceAll("\\D", "");
    return new TermosBusca(
        "%" + escaparCuringas(normalizarSemAcento(termo)) + "%",
        digitos.isEmpty() ? null : escaparCuringas(digitos) + "%");
  }

  /** Minúsculas e sem acentos — espelha a coluna gerada {@code nome_busca} (RNF-BE-26). */
  private static String normalizarSemAcento(String texto) {
    return Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT);
  }

  /** Escapa {@code %}, {@code _} e {@code \\} para o curinga não ser interpretado. */
  private static String escaparCuringas(String termo) {
    return termo.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  /** Documento armazenado apenas com dígitos (RNF-BE-27); a máscara é removida. */
  private static String somenteDigitos(String documento) {
    return documento.replaceAll("\\D", "");
  }

  /** Campo de texto opcional: valor em branco é tratado como não informado (RF-BE-003 RN-04). */
  private static String opcao(String valor) {
    if (valor == null) {
      return null;
    }
    String recortado = valor.trim();
    return recortado.isEmpty() ? null : recortado;
  }

  private record TermosBusca(String nome, String documento) {}

  public static class SupplierNotFoundException extends RuntimeException {
    public SupplierNotFoundException() {
      super("Fornecedor não encontrado.");
    }
  }

  public static class SupplierDocumentDuplicateException extends RuntimeException {
    public SupplierDocumentDuplicateException() {
      super("Fornecedor com este documento já existe (pode estar inativo).");
    }
  }

  public static class SupplierHasHistoryException extends RuntimeException {
    public SupplierHasHistoryException() {
      super(
          "O fornecedor possui pedidos de compra ou contas a pagar vinculados e não pode ser"
              + " excluído. Inative o fornecedor.");
    }
  }

  public static class InvalidQueryException extends RuntimeException {
    public InvalidQueryException(String detail) {
      super(detail);
    }
  }
}
