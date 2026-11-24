package br.com.meunegocio360.core.catalogo.api;

import br.com.meunegocio360.core.catalogo.application.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(
    name = "Categorias",
    description =
        "Categorias do catálogo da empresa ativa (RF-BE-005): cadastro mínimo, renomeio,"
            + " inativação/reativação e exclusão condicionada (RN-01, RN-06). Somente ADMIN e"
            + " OPERADOR_COMERCIAL (matriz de autorização); OPERADOR_FINANCEIRO recebe 403"
            + " (RNF-BE-47).")
@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

  private static final String JSON_CATEGORIA =
      """
      {
        "id": "2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3",
        "nome": "Informática",
        "ativo": true,
        "criadoEm": "2026-11-18T14:30:00Z",
        "atualizadoEm": "2026-11-18T14:30:00Z",
        "versao": 0
      }
      """;

  private static final String JSON_CATEGORIA_VERSAO_1 =
      """
      {
        "id": "2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3",
        "nome": "Informática e Periféricos",
        "ativo": true,
        "criadoEm": "2026-11-18T14:30:00Z",
        "atualizadoEm": "2026-11-18T15:10:00Z",
        "versao": 1
      }
      """;

  private static final String JSON_CATEGORIAS_PAGINA =
      """
      {
        "content": [
          {
            "id": "2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3",
            "nome": "Informática",
            "ativo": true,
            "criadoEm": "2026-11-18T14:30:00Z",
            "atualizadoEm": "2026-11-18T14:30:00Z",
            "versao": 0
          }
        ],
        "page": 0,
        "size": 20,
        "totalElements": 1,
        "totalPages": 1
      }
      """;

  private static final String JSON_NAO_AUTENTICADO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/nao-autorizado",
        "title": "Não autenticado",
        "status": 401,
        "detail": "Token de acesso ausente, inválido ou expirado.",
        "instance": "/api/v1/categorias",
        "code": "UNAUTHORIZED",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_SEM_PERMISSAO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/sem-permissao",
        "title": "Sem permissão",
        "status": 403,
        "detail": "Você não tem permissão para executar esta operação.",
        "instance": "/api/v1/categorias",
        "code": "ACCESS_DENIED",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_VALIDACAO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/validacao",
        "title": "Dados inválidos",
        "status": 400,
        "detail": "Um ou mais campos são inválidos.",
        "instance": "/api/v1/categorias",
        "code": "VALIDATION_ERROR",
        "errors": [
          {
            "field": "nome",
            "message": "deve ter entre 2 e 80 caracteres"
          }
        ],
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_CATEGORIA_NAO_ENCONTRADA =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/categoria-nao-encontrada",
        "title": "Categoria não encontrada",
        "status": 404,
        "detail": "Categoria não encontrada.",
        "instance": "/api/v1/categorias/2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3",
        "code": "CATEGORY_NOT_FOUND",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_NOME_DUPLICADO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/categoria-nome-duplicado",
        "title": "Nome já cadastrado",
        "status": 409,
        "detail": "Já existe uma categoria com este nome na empresa (maiúsculas e acentos não diferenciam).",
        "instance": "/api/v1/categorias",
        "code": "CATEGORY_NAME_DUPLICATE",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_CONFLITO_VERSAO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/conflito-de-versao",
        "title": "Conflito de versão",
        "status": 409,
        "detail": "A versão informada está desatualizada. Recarregue os dados e tente novamente.",
        "instance": "/api/v1/categorias/2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3",
        "code": "VERSION_CONFLICT",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_EMPRESA_NAO_ATIVA =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/empresa-nao-ativa",
        "title": "Empresa não ativa",
        "status": 422,
        "detail": "Operação exige empresa ativa. Cadastre e ative a empresa antes de executar esta operação.",
        "instance": "/api/v1/categorias",
        "code": "COMPANY_NOT_ACTIVE",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_CATEGORIA_EM_USO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/categoria-em-uso",
        "title": "Categoria não pode ser excluída",
        "status": 422,
        "detail": "A categoria possui 3 item(ns) vinculado(s) e não pode ser excluída. Inative a categoria.",
        "instance": "/api/v1/categorias/2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3",
        "code": "CATEGORY_IN_USE",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private final CategoriaService categoriaService;

  public CategoriaController(CategoriaService categoriaService) {
    this.categoriaService = categoriaService;
  }

  @Operation(
      summary = "Lista categorias da empresa ativa",
      description =
          "Listagem paginada (RNF-BE-13) com busca livre `q` por nome (contém, sem distinguir"
              + " maiúsculas e acentos, RN-06) e filtro `situacao` (padrão ATIVO). `sort` aceita"
              + " somente `nome` (padrão), com direção opcional `asc`/`desc` (RNF-BE-16).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categorias da página atual.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "pagina", value = JSON_CATEGORIAS_PAGINA))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Parâmetro de consulta inválido (ex.: sort fora de nome, situacao fora de"
                    + " ATIVO/INATIVO/TODOS, size acima de 100) (código VALIDATION_ERROR).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "validation", value = JSON_VALIDACAO))),
        @ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado (código UNAUTHORIZED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "unauthorized", value = JSON_NAO_AUTENTICADO))),
        @ApiResponse(
            responseCode = "403",
            description = "Perfil sem permissão (código ACCESS_DENIED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "access-denied", value = JSON_SEM_PERMISSAO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @GetMapping
  public ResponseEntity<CategoriaPageResponse> listar(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "ATIVO") String situacao,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "nome") String sort) {
    return ResponseEntity.ok(
        CategoriaPageResponse.from(categoriaService.listar(q, situacao, page, size, sort)));
  }

  @Operation(
      summary = "Consulta uma categoria",
      description =
          "Consulta uma categoria da empresa ativa (RN-01). Categorias de outras empresas"
              + " retornam 404 (escopo da empresa ativa, RNF-BE-26). Categorias inativas"
              + " continuam consultáveis (RN-06).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categoria encontrada.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "categoria", value = JSON_CATEGORIA))),
        @ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado (código UNAUTHORIZED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "unauthorized", value = JSON_NAO_AUTENTICADO))),
        @ApiResponse(
            responseCode = "403",
            description = "Perfil sem permissão (código ACCESS_DENIED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "access-denied", value = JSON_SEM_PERMISSAO))),
        @ApiResponse(
            responseCode = "404",
            description = "Categoria inexistente na empresa ativa (código CATEGORY_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_CATEGORIA_NAO_ENCONTRADA)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @GetMapping("/{id}")
  public ResponseEntity<CategoriaResponse> consultar(@PathVariable UUID id) {
    return ResponseEntity.ok(CategoriaResponse.from(categoriaService.consultar(id)));
  }

  @Operation(
      summary = "Cadastra uma categoria",
      description =
          "Cria uma categoria ativa na empresa ativa (RN-01). O nome é único por empresa sem"
              + " distinguir maiúsculas e acentos (RN-06) — duplicidade retorna 409"
              + " CATEGORY_NAME_DUPLICATE. Retorna 201 com Location. Sem empresa ativa, 422"
              + " COMPANY_NOT_ACTIVE (RNF-096).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Categoria criada, já ativa, com header Location.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "categoria-criada", value = JSON_CATEGORIA))),
        @ApiResponse(
            responseCode = "400",
            description = "Campos inválidos (código VALIDATION_ERROR).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "validation", value = JSON_VALIDACAO))),
        @ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado (código UNAUTHORIZED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "unauthorized", value = JSON_NAO_AUTENTICADO))),
        @ApiResponse(
            responseCode = "403",
            description = "Perfil sem permissão (código ACCESS_DENIED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "access-denied", value = JSON_SEM_PERMISSAO))),
        @ApiResponse(
            responseCode = "409",
            description = "Nome já cadastrado na empresa (código CATEGORY_NAME_DUPLICATE, RN-06).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "name-duplicate", value = JSON_NOME_DUPLICADO))),
        @ApiResponse(
            responseCode = "422",
            description = "Sem empresa ativa (código COMPANY_NOT_ACTIVE, RNF-096).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "company-not-active",
                            value = JSON_EMPRESA_NAO_ATIVA)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @PostMapping
  public ResponseEntity<CategoriaResponse> cadastrar(@Valid @RequestBody CategoriaRequest request) {
    var categoria = categoriaService.cadastrar(request.nome());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(categoria.id())
            .toUri();
    return ResponseEntity.created(location).body(CategoriaResponse.from(categoria));
  }

  @Operation(
      summary = "Renomeia uma categoria",
      description =
          "Atualiza o nome da categoria informando a `versao` lida (controle otimista,"
              + " RNF-BE-28), respeitando a unicidade do nome sem caixa/acentos (RN-06).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categoria renomeada (versao incrementada).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "categoria-renomeada",
                            value = JSON_CATEGORIA_VERSAO_1))),
        @ApiResponse(
            responseCode = "400",
            description = "Campos inválidos (código VALIDATION_ERROR).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "validation", value = JSON_VALIDACAO))),
        @ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado (código UNAUTHORIZED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "unauthorized", value = JSON_NAO_AUTENTICADO))),
        @ApiResponse(
            responseCode = "403",
            description = "Perfil sem permissão (código ACCESS_DENIED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "access-denied", value = JSON_SEM_PERMISSAO))),
        @ApiResponse(
            responseCode = "404",
            description = "Categoria inexistente na empresa ativa (código CATEGORY_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_CATEGORIA_NAO_ENCONTRADA))),
        @ApiResponse(
            responseCode = "409",
            description =
                "Versão desatualizada (código VERSION_CONFLICT) ou nome já cadastrado (código"
                    + " CATEGORY_NAME_DUPLICATE, RN-06).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "version-conflict", value = JSON_CONFLITO_VERSAO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @PutMapping("/{id}")
  public ResponseEntity<CategoriaResponse> renomear(
      @PathVariable UUID id, @Valid @RequestBody CategoriaRequest request) {
    var categoria =
        categoriaService.renomear(
            id, request.versao() == null ? -1 : request.versao(), request.nome());
    return ResponseEntity.ok(CategoriaResponse.from(categoria));
  }

  @Operation(
      summary = "Inativa ou reativa uma categoria",
      description =
          "Alterna a situação da categoria (RN-06): inativa sai da listagem padrão, mas continua"
              + " consultável e seus itens permanecem inalterados (RN-05). A operação é"
              + " idempotente e exige a `versao` lida (RNF-BE-28).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Situação alterada.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "categoria-inativada",
                            value = JSON_CATEGORIA_VERSAO_1))),
        @ApiResponse(
            responseCode = "400",
            description = "Payload inválido (código VALIDATION_ERROR).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "validation", value = JSON_VALIDACAO))),
        @ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado (código UNAUTHORIZED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "unauthorized", value = JSON_NAO_AUTENTICADO))),
        @ApiResponse(
            responseCode = "403",
            description = "Perfil sem permissão (código ACCESS_DENIED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "access-denied", value = JSON_SEM_PERMISSAO))),
        @ApiResponse(
            responseCode = "404",
            description = "Categoria inexistente na empresa ativa (código CATEGORY_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_CATEGORIA_NAO_ENCONTRADA))),
        @ApiResponse(
            responseCode = "409",
            description = "Versão desatualizada (código VERSION_CONFLICT).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "version-conflict", value = JSON_CONFLITO_VERSAO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @PatchMapping("/{id}/situacao")
  public ResponseEntity<CategoriaResponse> alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody SituacaoCategoriaRequest request) {
    var categoria = categoriaService.alterarSituacao(id, request.versao(), request.ativo());
    return ResponseEntity.ok(CategoriaResponse.from(categoria));
  }

  @Operation(
      summary = "Exclui uma categoria",
      description =
          "Exclui a categoria apenas se NENHUM item a usa (RN-06): com itens vinculados, 422"
              + " CATEGORY_IN_USE com a quantidade no detail — a categoria deve ser inativada."
              + " Não há exclusão em cascata de itens (RNF-BE-23).")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Categoria excluída (sem itens)."),
        @ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado (código UNAUTHORIZED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "unauthorized", value = JSON_NAO_AUTENTICADO))),
        @ApiResponse(
            responseCode = "403",
            description = "Perfil sem permissão (código ACCESS_DENIED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "access-denied", value = JSON_SEM_PERMISSAO))),
        @ApiResponse(
            responseCode = "404",
            description = "Categoria inexistente na empresa ativa (código CATEGORY_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_CATEGORIA_NAO_ENCONTRADA))),
        @ApiResponse(
            responseCode = "422",
            description =
                "Categoria com itens vinculados não pode ser excluída (código"
                    + " CATEGORY_IN_USE, RN-06).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "category-in-use", value = JSON_CATEGORIA_EM_USO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    categoriaService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
