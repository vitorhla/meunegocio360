package br.com.meunegocio360.core.catalogo.application;

import br.com.meunegocio360.core.catalogo.domain.Categoria;
import br.com.meunegocio360.core.catalogo.domain.CategoriaRepository;
import br.com.meunegocio360.core.catalogo.domain.ItemCatalogo;
import br.com.meunegocio360.core.catalogo.domain.ItemCatalogoRepository;
import br.com.meunegocio360.core.catalogo.domain.TipoItem;
import br.com.meunegocio360.core.catalogo.domain.VerificadorVinculoItem;
import br.com.meunegocio360.core.empresa.application.EmpresaAtiva;
import java.math.BigDecimal;
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
 * Caso de uso do catálogo de produtos e serviços (RF-BE-005).
 *
 * <p>Produtos e serviços coexistem no mesmo catálogo, distinguidos pelo {@code tipo} imutável
 * (RN-02). O preço é obrigatório, decimal com até 2 casas e não negativo (RN-03, RNF-033); a
 * conversão usa {@link BigDecimal} exato, sem {@code float} (RNF-033). A categoria do item deve
 * existir na empresa ativa e estar ativa (RN-05). Item com vínculo (venda, compra) é inativado,
 * nunca excluído (RN-09, RNF-097). {@code estoque_minimo} existe no banco mas NENHUM DTO o lê ou
 * grava (RN-07).
 */
@Service
@Transactional(readOnly = true)
public class ItemCatalogoService {

  static final int TAMANHO_PAGINA_MAXIMO = 100;
  private static final Set<String> CAMPOS_SORT = Set.of("nome", "preco", "criadoEm");

  private final ItemCatalogoRepository itemCatalogoRepository;
  private final CategoriaRepository categoriaRepository;
  private final EmpresaAtiva empresaAtiva;
  private final List<VerificadorVinculoItem> verificadoresVinculo;

  public ItemCatalogoService(
      ItemCatalogoRepository itemCatalogoRepository,
      CategoriaRepository categoriaRepository,
      EmpresaAtiva empresaAtiva,
      List<VerificadorVinculoItem> verificadoresVinculo) {
    this.itemCatalogoRepository = itemCatalogoRepository;
    this.categoriaRepository = categoriaRepository;
    this.empresaAtiva = empresaAtiva;
    this.verificadoresVinculo = verificadoresVinculo;
  }

  public record ItemData(
      UUID id,
      TipoItem tipo,
      String codigo,
      String nome,
      String descricao,
      String unidade,
      BigDecimal preco,
      UUID categoriaId,
      String categoriaNome,
      boolean ativo,
      Instant criadoEm,
      Instant atualizadoEm,
      long versao) {

    public static ItemData from(ItemCatalogo item) {
      return new ItemData(
          item.getId(),
          item.getTipo(),
          item.getCodigo(),
          item.getNome(),
          item.getDescricao(),
          item.getUnidade(),
          item.getPreco(),
          item.getCategoriaId(),
          null,
          item.isAtivo(),
          item.getCriadoEm(),
          item.getAtualizadoEm(),
          item.getVersao());
    }

    ItemData comCategoria(String nome) {
      return new ItemData(
          id,
          tipo,
          codigo,
          nome,
          descricao,
          unidade,
          preco,
          categoriaId,
          nome,
          ativo,
          criadoEm,
          atualizadoEm,
          versao);
    }
  }

  /** Resposta paginada no formato padrão (RNF-BE-13). */
  public record ItemPageData(
      List<ItemData> content, int page, int size, long totalElements, int totalPages) {

    public static ItemPageData from(Page<ItemData> pagina) {
      return new ItemPageData(
          pagina.getContent(),
          pagina.getNumber(),
          pagina.getSize(),
          pagina.getTotalElements(),
          pagina.getTotalPages());
    }

    public static ItemPageData vazia(int page, int size) {
      return new ItemPageData(List.of(), page, size, 0, 0);
    }
  }

  /**
   * Lista os itens da empresa ativa (RN-10): filtros {@code tipo}, {@code categoriaId} e {@code
   * situacao} combinam entre si (E), mais a busca {@code q} por nome ou código (OR, sem
   * caixa/acentos). {@code sort} aceita {@code nome} (padrão), {@code preco} ou {@code criadoEm}
   * (RNF-BE-16). Sem empresa ativa a listagem é vazia.
   */
  public ItemPageData listar(
      String q, String tipo, UUID categoriaId, String situacao, int page, int size, String sort) {
    validarPaginacao(page, size);
    Sort ordenacao = ordenacaoDe(sort);
    TipoItem filtroTipo = filtroDeTipo(tipo);
    Boolean filtroSituacao = filtroDeSituacao(situacao);

    Optional<UUID> empresaId = empresaIdDaEmpresaAtiva();
    if (empresaId.isEmpty()) {
      return ItemPageData.vazia(page, size);
    }
    TermosBusca termos = termosDe(q);
    PageRequest pagina = PageRequest.of(page, size, ordenacao);
    Page<ItemCatalogo> itens =
        itemCatalogoRepository.buscar(
            empresaId.get(),
            filtroTipo,
            categoriaId,
            filtroSituacao,
            termos == null ? null : termos.nome(),
            termos == null ? null : termos.codigo(),
            pagina);
    Page<ItemData> dados =
        itens.map(
            item -> {
              ItemData data = ItemData.from(item);
              String nomeCategoria =
                  item.getCategoriaId() == null
                      ? null
                      : nomeDaCategoria(empresaId.get(), item.getCategoriaId());
              return nomeCategoria == null ? data : data.comCategoria(nomeCategoria);
            });
    return ItemPageData.from(dados);
  }

  /** Consulta um item da empresa ativa; 404 ITEM_NOT_FOUND em caso contrário (RN-01). */
  public ItemData consultar(UUID id) {
    UUID empresaId = empresaIdObrigatorioParaLer();
    ItemCatalogo item =
        itemCatalogoRepository
            .buscarPorEmpresaEId(empresaId, id)
            .orElseThrow(ItemNotFoundException::new);
    ItemData data = ItemData.from(item);
    String nomeCategoria =
        item.getCategoriaId() == null ? null : nomeDaCategoria(empresaId, item.getCategoriaId());
    return nomeCategoria == null ? data : data.comCategoria(nomeCategoria);
  }

  /** Nome da categoria vinculada (quando o item tem categoria e ela existe na empresa). */
  private String nomeDaCategoria(UUID empresaId, UUID categoriaId) {
    return categoriaRepository
        .buscarPorEmpresaEId(empresaId, categoriaId)
        .map(Categoria::getNome)
        .orElse(null);
  }

  /** Monta o {@link ItemData} já com o nome da categoria vinculada (quando houver). */
  private ItemData comCategoria(ItemCatalogo item, UUID empresaId, UUID categoriaId) {
    ItemData data = ItemData.from(itemCatalogoRepository.save(item));
    String nomeCategoria = categoriaId == null ? null : nomeDaCategoria(empresaId, categoriaId);
    return nomeCategoria == null ? data : data.comCategoria(nomeCategoria);
  }

  /**
   * Cadastra um item ativo na empresa ativa (RN-01 a RN-05). Sem empresa ativa, 422
   * COMPANY_NOT_ACTIVE (RNF-096). Código duplicado → 409 ITEM_CODE_DUPLICATE (RN-04); categoria
   * inexistente ou inativa → 422 CATEGORY_INVALID (RN-05).
   */
  @Transactional
  public ItemData cadastrar(
      TipoItem tipo,
      String codigo,
      String nome,
      String descricao,
      String unidade,
      BigDecimal preco,
      UUID categoriaId) {
    UUID empresaId = empresaAtiva.obterId();
    String codigoRecortada = opcao(codigo);
    if (codigoRecortada != null) {
      verificarCodigoUnico(empresaId, codigoRecortada, null);
    }
    validarCategoria(empresaId, categoriaId);
    ItemCatalogo item =
        new ItemCatalogo(
            empresaId,
            categoriaId,
            tipo,
            codigoRecortada,
            nome.trim(),
            opcao(descricao),
            unidadePadrao(unidade, tipo),
            preco);
    return comCategoria(item, empresaId, categoriaId);
  }

  /**
   * Edita o item informando a {@code versao} lida (RNF-BE-28). O {@code tipo} enviado DEVE ser
   * igual ao atual (RN-02) — caso contrário, 422 ITEM_TYPE_IMMUTABLE.
   */
  @Transactional
  public ItemData atualizar(
      UUID id,
      long versao,
      TipoItem tipo,
      String codigo,
      String nome,
      String descricao,
      String unidade,
      BigDecimal preco,
      UUID categoriaId) {
    ItemCatalogo item = buscarNaEmpresaAtiva(id);
    verificarVersao(item, versao, id);
    if (tipo != item.getTipo()) {
      throw new ItemTypeImmutableException();
    }
    String codigoRecortada = opcao(codigo);
    if (codigoRecortada != null) {
      verificarCodigoUnico(item.getEmpresaId(), codigoRecortada, id);
    }
    validarCategoria(item.getEmpresaId(), categoriaId);
    item.atualizar(
        tipo,
        codigoRecortada,
        nome.trim(),
        opcao(descricao),
        unidadePadrao(unidade, item.getTipo()),
        preco,
        categoriaId);
    return comCategoria(item, item.getEmpresaId(), item.getCategoriaId());
  }

  /**
   * Inativa ({@code ativo = false}) ou reativa ({@code ativo = true}) o item (RN-10). As duas
   * operações são idempotentes.
   */
  @Transactional
  public ItemData alterarSituacao(UUID id, long versao, boolean ativo) {
    ItemCatalogo item = buscarNaEmpresaAtiva(id);
    verificarVersao(item, versao, id);
    if (ativo) {
      item.reativar();
    } else {
      item.inativar();
    }
    return comCategoria(item, item.getEmpresaId(), item.getCategoriaId());
  }

  /**
   * Exclui o item somente se NENHUM {@link VerificadorVinculoItem} registrado apontar vínculo
   * (venda ou compra) — RN-09. Com vínculo, 422 ITEM_HAS_HISTORY (RNF-097).
   */
  @Transactional
  public void excluir(UUID id) {
    ItemCatalogo item = buscarNaEmpresaAtiva(id);
    for (VerificadorVinculoItem verificador : verificadoresVinculo) {
      if (verificador.possuiVinculo(id)) {
        throw new ItemHasHistoryException();
      }
    }
    itemCatalogoRepository.delete(item);
  }

  private ItemCatalogo buscarNaEmpresaAtiva(UUID id) {
    UUID empresaId = empresaIdDaEmpresaAtiva().orElseThrow(ItemNotFoundException::new);
    return itemCatalogoRepository
        .buscarPorEmpresaEId(empresaId, id)
        .orElseThrow(ItemNotFoundException::new);
  }

  private UUID empresaIdObrigatorioParaLer() {
    return empresaIdDaEmpresaAtiva().orElseThrow(ItemNotFoundException::new);
  }

  /** Id da empresa ativa ou vazio; a feature NÃO expõe a falta de empresa como erro de leitura. */
  private Optional<UUID> empresaIdDaEmpresaAtiva() {
    try {
      return Optional.of(empresaAtiva.obterId());
    } catch (EmpresaAtiva.CompanyNotActiveException e) {
      return Optional.empty();
    }
  }

  private void verificarCodigoUnico(UUID empresaId, String codigo, UUID idIgnorado) {
    if (itemCatalogoRepository.existePorEmpresaECodigo(empresaId, codigo, idIgnorado)) {
      throw new ItemCodeDuplicateException();
    }
  }

  /**
   * Categoria opcional (RN-04): quando informada, DEVE existir na empresa ativa e estar ATIVA
   * (RN-05) — caso contrário, 422 CATEGORY_INVALID.
   */
  private void validarCategoria(UUID empresaId, UUID categoriaId) {
    if (categoriaId == null) {
      return;
    }
    Optional<Categoria> categoria = categoriaRepository.buscarPorEmpresaEId(empresaId, categoriaId);
    if (categoria.isEmpty() || !categoria.get().isAtivo()) {
      throw new CategoryInvalidException();
    }
  }

  private void verificarVersao(ItemCatalogo item, long versao, UUID id) {
    if (versao != item.getVersao()) {
      throw new ObjectOptimisticLockingFailureException(ItemCatalogo.class, id);
    }
  }

  /**
   * Unidade: 1 a 6 maiúsculas (RN-04). Não informada, padrão {@code UN} para produto e {@code H}
   * para serviço.
   */
  private static String unidadePadrao(String unidade, TipoItem tipo) {
    String informada = opcao(unidade);
    if (informada != null) {
      return informada;
    }
    return tipo == TipoItem.PRODUTO ? "UN" : "H";
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
   * Caminhos permitidos para {@code sort} (RNF-BE-16): {@code nome} (padrão), {@code preco} ou
   * {@code criadoEm}, com direção opcional {@code ,asc}/{@code ,desc}. Qualquer outro valor é 400.
   */
  private Sort ordenacaoDe(String sort) {
    String valor = (sort == null || sort.isBlank()) ? "nome" : sort.trim();
    String[] partes = valor.split(",", 2);
    String campo = partes[0].trim().toLowerCase(Locale.ROOT);
    if (!CAMPOS_SORT.contains(campo)) {
      throw new InvalidQueryException(
          "sort: campo inválido ('"
              + partes[0].trim()
              + "'); campos permitidos: nome, preco, criadoEm.");
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

  /** Filtro {@code tipo}: {@code PRODUTO} ou {@code SERVICO}; ausente → sem filtro (RN-02/08). */
  private TipoItem filtroDeTipo(String tipo) {
    if (tipo == null || tipo.isBlank()) {
      return null;
    }
    String valor = tipo.trim().toUpperCase(Locale.ROOT);
    return switch (valor) {
      case "PRODUTO" -> TipoItem.PRODUTO;
      case "SERVICO" -> TipoItem.SERVICO;
      default ->
          throw new InvalidQueryException(
              "tipo: valor inválido ('" + tipo + "'); use PRODUTO ou SERVICO.");
    };
  }

  /**
   * {@code ATIVO} (padrão) → só ativos; {@code INATIVO} → só inativos; {@code TODOS} → sem filtro
   * (RN-10).
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
   * Converte o termo livre {@code q} (RN-10) em dois termos de busca combinados com OR: nome
   * (contém, sem caixa/acentos) e código (contém, sem caixa/acentos — a coluna gerada já cuida da
   * parte de acento/caixa).
   */
  private TermosBusca termosDe(String q) {
    if (q == null || q.trim().isEmpty()) {
      return null;
    }
    String termo = q.trim();
    String normalizado = escaparCuringas(normalizarSemAcento(termo));
    return new TermosBusca("%" + normalizado + "%", "%" + normalizado + "%");
  }

  /** Campo de texto opcional: valor em branco é tratado como não informado (RN-04). */
  private static String opcao(String valor) {
    if (valor == null) {
      return null;
    }
    String recortado = valor.trim();
    return recortado.isEmpty() ? null : recortado;
  }

  private record TermosBusca(String nome, String codigo) {}

  /**
   * Minúsculas e sem acentos — espelha as colunas geradas {@code nome_busca}/{@code codigo_busca}
   * (RNF-BE-26).
   */
  private static String normalizarSemAcento(String texto) {
    return Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT);
  }

  /** Escapa {@code %}, {@code _} e {@code \\} para o curinga não ser interpretado. */
  private static String escaparCuringas(String termo) {
    return termo.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

  public static class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException() {
      super("Item de catálogo não encontrado.");
    }
  }

  public static class ItemCodeDuplicateException extends RuntimeException {
    public ItemCodeDuplicateException() {
      super("Já existe um item com este código na empresa.");
    }
  }

  public static class ItemTypeImmutableException extends RuntimeException {
    public ItemTypeImmutableException() {
      super("O tipo do item (PRODUTO/SERVICO) não pode ser alterado após a criação.");
    }
  }

  public static class CategoryInvalidException extends RuntimeException {
    public CategoryInvalidException() {
      super("A categoria informada não existe na empresa ativa ou está inativa.");
    }
  }

  public static class ItemHasHistoryException extends RuntimeException {
    public ItemHasHistoryException() {
      super("O item possui vendas ou compras vinculadas e não pode ser excluído. Inative o item.");
    }
  }

  public static class InvalidQueryException extends RuntimeException {
    public InvalidQueryException(String detail) {
      super(detail);
    }
  }
}
