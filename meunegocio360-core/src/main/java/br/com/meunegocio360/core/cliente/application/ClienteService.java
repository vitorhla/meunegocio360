package br.com.meunegocio360.core.cliente.application;

import br.com.meunegocio360.core.cliente.domain.Cliente;
import br.com.meunegocio360.core.cliente.domain.ClienteRepository;
import br.com.meunegocio360.core.cliente.domain.VerificadorVinculoCliente;
import br.com.meunegocio360.core.empresa.application.EmpresaAtiva;
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
 * Caso de uso de cadastro de clientes (RF-BE-003).
 *
 * <p>Todo cliente pertence à empresa ativa, resolvida no servidor via {@link EmpresaAtiva} (RN-01,
 * RNF-096): o cliente NUNCA informa a empresa. O documento (CPF ou CNPJ) é único por empresa,
 * ativos e inativos (RN-03), e cliente com vínculo só é inativado, nunca excluído (RN-05, RN-06,
 * RNF-097).
 */
@Service
@Transactional(readOnly = true)
public class ClienteService {

  static final int TAMANHO_PAGINA_MAXIMO = 100;
  private static final Set<String> CAMPOS_SORT = Set.of("nome", "criadoEm");

  private final ClienteRepository clienteRepository;
  private final EmpresaAtiva empresaAtiva;
  private final List<VerificadorVinculoCliente> verificadoresVinculo;

  public ClienteService(
      ClienteRepository clienteRepository,
      EmpresaAtiva empresaAtiva,
      List<VerificadorVinculoCliente> verificadoresVinculo) {
    this.clienteRepository = clienteRepository;
    this.empresaAtiva = empresaAtiva;
    this.verificadoresVinculo = verificadoresVinculo;
  }

  public record ClienteData(
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

    public static ClienteData from(Cliente cliente) {
      return new ClienteData(
          cliente.getId(),
          cliente.getTipoPessoa(),
          cliente.getNome(),
          cliente.getDocumento(),
          cliente.getEmail(),
          cliente.getTelefone(),
          cliente.isAtivo(),
          cliente.getCriadoEm(),
          cliente.getAtualizadoEm(),
          cliente.getVersao());
    }
  }

  /** Resposta paginada no formato padrão (RNF-BE-13). */
  public record ClientePageData(
      List<ClienteData> content, int page, int size, long totalElements, int totalPages) {

    public static ClientePageData from(Page<Cliente> pagina) {
      return new ClientePageData(
          pagina.map(ClienteData::from).getContent(),
          pagina.getNumber(),
          pagina.getSize(),
          pagina.getTotalElements(),
          pagina.getTotalPages());
    }

    public static ClientePageData vazia(int page, int size) {
      return new ClientePageData(List.of(), page, size, 0, 0);
    }
  }

  /**
   * Lista os clientes da empresa ativa (RN-07, RN-08). Sem empresa ativa não há escopo de consulta
   * e a listagem é vazia. Busca por nome (sem caixa/acentos) e/ou por prefixo de documento,
   * combinada com o filtro de situação (RNF-BE-13/16/26).
   */
  public ClientePageData listar(String q, String situacao, int page, int size, String sort) {
    validarPaginacao(page, size);
    Sort ordenacao = ordenacaoDe(sort);
    Boolean filtroSituacao = filtroDeSituacao(situacao);

    Optional<UUID> empresaId = empresaIdDaEmpresaAtiva();
    if (empresaId.isEmpty()) {
      return ClientePageData.vazia(page, size);
    }
    TermosBusca termos = termosDe(q);
    PageRequest pagina = PageRequest.of(page, size, ordenacao);
    return ClientePageData.from(
        clienteRepository.buscar(
            empresaId.get(),
            filtroSituacao,
            termos == null ? null : termos.nome(),
            termos == null ? null : termos.documento(),
            pagina));
  }

  /** Consulta um cliente da empresa ativa; 404 CUSTOMER_NOT_FOUND em caso contrário (RN-01). */
  public ClienteData consultar(UUID id) {
    return clienteRepository
        .buscarPorEmpresaEId(empresaIdObrigatorioParaLer(), id)
        .map(ClienteData::from)
        .orElseThrow(CustomerNotFoundException::new);
  }

  /**
   * Cadastra um cliente ativo na empresa ativa (RN-01 a RN-04). Sem empresa ativa, 422
   * COMPANY_NOT_ACTIVE (RNF-096).
   */
  @Transactional
  public ClienteData cadastrar(
      TipoPessoa tipoPessoa, String nome, String documento, String email, String telefone) {
    UUID empresaId = empresaAtiva.obterId();
    String documentoRecortado = somenteDigitos(documento);
    verificarDocumentoUnico(empresaId, documentoRecortado, null);
    Cliente cliente =
        new Cliente(
            empresaId, tipoPessoa, nome.trim(), documentoRecortado, opcao(email), opcao(telefone));
    return ClienteData.from(clienteRepository.save(cliente));
  }

  /**
   * Edita o cliente informando a {@code versao} lida (RN-09, RNF-BE-28). Tipo e documento podem ser
   * corrigidos, respeitando RN-02 e RN-03.
   */
  @Transactional
  public ClienteData atualizar(
      UUID id,
      long versao,
      TipoPessoa tipoPessoa,
      String nome,
      String documento,
      String email,
      String telefone) {
    Cliente cliente = buscarNaEmpresaAtiva(id);
    verificarVersao(cliente, versao, id);
    String documentoRecortado = somenteDigitos(documento);
    verificarDocumentoUnico(cliente.getEmpresaId(), documentoRecortado, id);
    cliente.atualizar(tipoPessoa, nome.trim(), documentoRecortado, opcao(email), opcao(telefone));
    return ClienteData.from(clienteRepository.save(cliente));
  }

  /**
   * Inativa ({@code ativo = false}) ou reativa ({@code ativo = true}) o cliente (RN-07). As duas
   * operações são idempotentes: repetir a mesma situação não gera nova versão.
   */
  @Transactional
  public ClienteData alterarSituacao(UUID id, long versao, boolean ativo) {
    Cliente cliente = buscarNaEmpresaAtiva(id);
    verificarVersao(cliente, versao, id);
    if (ativo) {
      cliente.reativar();
    } else {
      cliente.inativar();
    }
    return ClienteData.from(clienteRepository.save(cliente));
  }

  /**
   * Exclui o cliente somente se NENHUM {@link VerificadorVinculoCliente} registrado apontar vínculo
   * (venda ou OS) — RN-05 e RN-06. Com vínculo, 422 CUSTOMER_HAS_HISTORY (RNF-097).
   */
  @Transactional
  public void excluir(UUID id) {
    Cliente cliente = buscarNaEmpresaAtiva(id);
    for (VerificadorVinculoCliente verificador : verificadoresVinculo) {
      if (verificador.possuiVinculo(id)) {
        throw new CustomerHasHistoryException();
      }
    }
    clienteRepository.delete(cliente);
  }

  private Cliente buscarNaEmpresaAtiva(UUID id) {
    UUID empresaId = empresaIdDaEmpresaAtiva().orElseThrow(CustomerNotFoundException::new);
    return clienteRepository
        .buscarPorEmpresaEId(empresaId, id)
        .orElseThrow(CustomerNotFoundException::new);
  }

  private UUID empresaIdObrigatorioParaLer() {
    return empresaIdDaEmpresaAtiva().orElseThrow(CustomerNotFoundException::new);
  }

  /** Id da empresa ativa ou vazio; a feature NÃO expõe a falta de empresa como erro de escrita. */
  private Optional<UUID> empresaIdDaEmpresaAtiva() {
    try {
      return Optional.of(empresaAtiva.obterId());
    } catch (EmpresaAtiva.CompanyNotActiveException e) {
      return Optional.empty();
    }
  }

  private void verificarDocumentoUnico(UUID empresaId, String documento, UUID idIgnorado) {
    if (clienteRepository.existePorEmpresaIDocumento(empresaId, documento, idIgnorado)) {
      throw new CustomerDocumentDuplicateException();
    }
  }

  private void verificarVersao(Cliente cliente, long versao, UUID id) {
    if (versao != cliente.getVersao()) {
      throw new ObjectOptimisticLockingFailureException(Cliente.class, id);
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
   * Converte o termo livre {@code q} (RN-08) em dois termos de busca combinados com OR: nome
   * (contém, sem caixa/acentos, sobre a coluna gerada {@code nome_busca}) e prefixo de documento
   * (somente dígitos informados, máscara ignorada).
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

  /** Minúsculas e sem acentos — espelha a coluna gerada {@code nome_busca} (RN-08, RNF-BE-26). */
  private static String normalizarSemAcento(String texto) {
    return Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT);
  }

  /**
   * Escapa {@code %}, {@code _} e {@code \\} para o curinga da coluna de busca não ser
   * interpretado.
   */
  private static String escaparCuringas(String termo) {
    return termo.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  /** Documento armazenado apenas com dígitos (RN-02); a máscara é removida (RN-08). */
  private static String somenteDigitos(String documento) {
    return documento.replaceAll("\\D", "");
  }

  /** Campo de texto opcional: valor em branco é tratado como não informado (RN-04). */
  private static String opcao(String valor) {
    if (valor == null) {
      return null;
    }
    String recortado = valor.trim();
    return recortado.isEmpty() ? null : recortado;
  }

  private record TermosBusca(String nome, String documento) {}

  public static class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException() {
      super("Cliente não encontrado.");
    }
  }

  public static class CustomerDocumentDuplicateException extends RuntimeException {
    public CustomerDocumentDuplicateException() {
      super("Cliente com este documento já existe (pode estar inativo).");
    }
  }

  public static class CustomerHasHistoryException extends RuntimeException {
    public CustomerHasHistoryException() {
      super(
          "O cliente possui vendas ou ordens de serviço vinculadas e não pode ser excluído."
              + " Inative o cliente.");
    }
  }

  public static class InvalidQueryException extends RuntimeException {
    public InvalidQueryException(String detail) {
      super(detail);
    }
  }
}
