package br.com.meunegocio360.core.catalogo.application;

import br.com.meunegocio360.core.catalogo.domain.Categoria;
import br.com.meunegocio360.core.catalogo.domain.CategoriaRepository;
import br.com.meunegocio360.core.catalogo.domain.ItemCatalogoRepository;
import br.com.meunegocio360.core.empresa.application.EmpresaAtiva;
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
 * Caso de uso de categorias do catálogo (RF-BE-005).
 *
 * <p>Toda categoria pertence à empresa ativa (RN-01, RNF-096), resolvida no servidor. O nome é
 * único por empresa sem distinguir maiúsculas e acentos (RN-06). A categoria com itens vinculados
 * não pode ser excluída, apenas inativada (RN-06): a exclusão em cascata de itens é proibida
 * (RNF-BE-23; "Pontos de atenção").
 */
@Service
@Transactional(readOnly = true)
public class CategoriaService {

  static final int TAMANHO_PAGINA_MAXIMO = 100;
  private static final Set<String> CAMPOS_SORT = Set.of("nome");

  private final CategoriaRepository categoriaRepository;
  private final ItemCatalogoRepository itemCatalogoRepository;
  private final EmpresaAtiva empresaAtiva;

  public CategoriaService(
      CategoriaRepository categoriaRepository,
      ItemCatalogoRepository itemCatalogoRepository,
      EmpresaAtiva empresaAtiva) {
    this.categoriaRepository = categoriaRepository;
    this.itemCatalogoRepository = itemCatalogoRepository;
    this.empresaAtiva = empresaAtiva;
  }

  public record CategoriaData(
      UUID id, String nome, boolean ativo, Instant criadoEm, Instant atualizadoEm, long versao) {

    public static CategoriaData from(Categoria categoria) {
      return new CategoriaData(
          categoria.getId(),
          categoria.getNome(),
          categoria.isAtivo(),
          categoria.getCriadoEm(),
          categoria.getAtualizadoEm(),
          categoria.getVersao());
    }
  }

  /** Resposta paginada no formato padrão (RNF-BE-13). */
  public record CategoriaPageData(
      List<CategoriaData> content, int page, int size, long totalElements, int totalPages) {

    public static CategoriaPageData from(Page<Categoria> pagina) {
      return new CategoriaPageData(
          pagina.map(CategoriaData::from).getContent(),
          pagina.getNumber(),
          pagina.getSize(),
          pagina.getTotalElements(),
          pagina.getTotalPages());
    }

    public static CategoriaPageData vazia(int page, int size) {
      return new CategoriaPageData(List.of(), page, size, 0, 0);
    }
  }

  /**
   * Lista as categorias da empresa ativa (RNF-BE-13/16). Sem empresa ativa não há escopo de
   * consulta e a listagem é vazia. Busca por nome (sem caixa/acentos) e filtro de situação.
   */
  public CategoriaPageData listar(String q, String situacao, int page, int size, String sort) {
    validarPaginacao(page, size);
    Sort ordenacao = ordenacaoDe(sort);
    Boolean filtroSituacao = filtroDeSituacao(situacao);

    Optional<UUID> empresaId = empresaIdDaEmpresaAtiva();
    if (empresaId.isEmpty()) {
      return CategoriaPageData.vazia(page, size);
    }
    String qNome = (q == null || q.trim().isEmpty()) ? null : termosDeNome(q);
    PageRequest pagina = PageRequest.of(page, size, ordenacao);
    return CategoriaPageData.from(
        categoriaRepository.buscar(empresaId.get(), filtroSituacao, qNome, pagina));
  }

  /** Consulta uma categoria da empresa ativa; 404 CATEGORY_NOT_FOUND em caso contrário (RN-01). */
  public CategoriaData consultar(UUID id) {
    return categoriaRepository
        .buscarPorEmpresaEId(empresaIdObrigatorioParaLer(), id)
        .map(CategoriaData::from)
        .orElseThrow(CategoryNotFoundException::new);
  }

  /**
   * Cadastra uma categoria ativa na empresa ativa (RN-01). Sem empresa ativa, 422
   * COMPANY_NOT_ACTIVE (RNF-096). Nome duplicado (sem caixa/acentos) → 409 CATEGORY_NAME_DUPLICATE
   * (RN-06).
   */
  @Transactional
  public CategoriaData cadastrar(String nome) {
    UUID empresaId = empresaAtiva.obterId();
    String nomeRecortado = nome.trim();
    verificarNomeUnico(empresaId, nomeRecortado, null);
    Categoria categoria = new Categoria(empresaId, nomeRecortado);
    return CategoriaData.from(categoriaRepository.save(categoria));
  }

  /**
   * Renomeia a categoria informando a {@code versao} lida (RNF-BE-28), respeitando a unicidade do
   * nome (RN-06).
   */
  @Transactional
  public CategoriaData renomear(UUID id, long versao, String nome) {
    Categoria categoria = buscarNaEmpresaAtiva(id);
    verificarVersao(categoria, versao, id);
    String nomeRecortado = nome.trim();
    verificarNomeUnico(categoria.getEmpresaId(), nomeRecortado, id);
    categoria.renomear(nomeRecortado);
    return CategoriaData.from(categoriaRepository.save(categoria));
  }

  /**
   * Inativa ({@code ativo = false}) ou reativa ({@code ativo = true}) a categoria (RN-06). As duas
   * operações são idempotentes. Inativar NÃO altera os itens que a usam (RN-05).
   */
  @Transactional
  public CategoriaData alterarSituacao(UUID id, long versao, boolean ativo) {
    Categoria categoria = buscarNaEmpresaAtiva(id);
    verificarVersao(categoria, versao, id);
    if (ativo) {
      categoria.reativar();
    } else {
      categoria.inativar();
    }
    return CategoriaData.from(categoriaRepository.save(categoria));
  }

  /**
   * Exclui a categoria apenas se NENHUM item a usa (RN-06): com itens, 422 CATEGORY_IN_USE com a
   * quantidade no detail. Sem ON DELETE em cascata no banco (RNF-BE-23).
   */
  @Transactional
  public void excluir(UUID id) {
    Categoria categoria = buscarNaEmpresaAtiva(id);
    long itens =
        itemCatalogoRepository.contarPorEmpresaECategoria(
            categoria.getEmpresaId(), categoria.getId());
    if (itens > 0) {
      throw new CategoryInUseException(itens);
    }
    categoriaRepository.delete(categoria);
  }

  private Categoria buscarNaEmpresaAtiva(UUID id) {
    UUID empresaId = empresaIdDaEmpresaAtiva().orElseThrow(CategoryNotFoundException::new);
    return categoriaRepository
        .buscarPorEmpresaEId(empresaId, id)
        .orElseThrow(CategoryNotFoundException::new);
  }

  private UUID empresaIdObrigatorioParaLer() {
    return empresaIdDaEmpresaAtiva().orElseThrow(CategoryNotFoundException::new);
  }

  /** Id da empresa ativa ou vazio; a feature NÃO expõe a falta de empresa como erro de leitura. */
  private Optional<UUID> empresaIdDaEmpresaAtiva() {
    try {
      return Optional.of(empresaAtiva.obterId());
    } catch (EmpresaAtiva.CompanyNotActiveException e) {
      return Optional.empty();
    }
  }

  private void verificarNomeUnico(UUID empresaId, String nome, UUID idIgnorado) {
    if (categoriaRepository.existePorEmpresaENomeNormalizado(
        empresaId, normalizarSemAcento(nome), idIgnorado)) {
      throw new CategoryNameDuplicateException();
    }
  }

  private void verificarVersao(Categoria categoria, long versao, UUID id) {
    if (versao != categoria.getVersao()) {
      throw new ObjectOptimisticLockingFailureException(Categoria.class, id);
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
   * Caminhos permitidos para {@code sort} (RNF-BE-16): {@code nome} (padrão), com direção opcional
   * {@code ,asc}/{@code ,desc}. Qualquer outro valor é 400.
   */
  private Sort ordenacaoDe(String sort) {
    String valor = (sort == null || sort.isBlank()) ? "nome" : sort.trim();
    String[] partes = valor.split(",", 2);
    String campo = partes[0].trim().toLowerCase(Locale.ROOT);
    if (!CAMPOS_SORT.contains(campo)) {
      throw new InvalidQueryException(
          "sort: campo inválido ('" + partes[0].trim() + "'); campos permitidos: nome.");
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
   * {@code ATIVO} (padrão) → só ativas; {@code INATIVO} → só inativas; {@code TODOS} → sem filtro.
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

  /** Termo de busca por nome: contém, sem caixa/acentos (RN-06), com curingas escapados. */
  private static String termosDeNome(String q) {
    return "%" + escaparCuringas(normalizarSemAcento(q.trim())) + "%";
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

  public static class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException() {
      super("Categoria não encontrada.");
    }
  }

  public static class CategoryNameDuplicateException extends RuntimeException {
    public CategoryNameDuplicateException() {
      super(
          "Já existe uma categoria com este nome na empresa (maiúsculas e acentos não"
              + " diferenciam).");
    }
  }

  public static class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(long quantidadeItens) {
      super(
          "A categoria possui "
              + quantidadeItens
              + " item(ns) vinculado(s) e não pode ser excluída. Inative a categoria.");
    }
  }

  public static class InvalidQueryException extends RuntimeException {
    public InvalidQueryException(String detail) {
      super(detail);
    }
  }
}
