package br.com.meunegocio360.core.catalogo.api;

import br.com.meunegocio360.core.catalogo.application.ItemCatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
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
    name = "Itens de Catálogo",
    description =
        "Catálogo de produtos e serviços da empresa ativa (RF-BE-005): produtos (PRODUTO) e"
            + " serviços (SERVICO) coexistem no mesmo catálogo, com tipo imutável (RN-02), preço"
            + " decimal em string (RN-03, RNF-033) e exclusão condicionada (RN-09). Somente ADMIN"
            + " e OPERADOR_COMERCIAL (matriz de autorização); OPERADOR_FINANCEIRO recebe 403"
            + " (RNF-BE-47).")
@RestController
@RequestMapping("/api/v1/itens-catalogo")
public class ItemCatalogoController {

  private static final String JSON_ITEM =
      """
      {
        "id": "9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44",
        "tipo": "PRODUTO",
        "codigo": "MSE-01",
        "nome": "Mouse sem fio",
        "descricao": "Mouse óptico 1200 dpi",
        "unidade": "UN",
        "preco": "89.90",
        "categoria": {
          "id": "2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3",
          "nome": "Informática"
        },
        "ativo": true,
        "criadoEm": "2026-11-18T14:30:00Z",
        "atualizadoEm": "2026-11-18T14:30:00Z",
        "versao": 0
      }
      """;

  private static final String JSON_ITEM_VERSAO_1 =
      """
      {
        "id": "9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44",
        "tipo": "PRODUTO",
        "codigo": "MSE-01",
        "nome": "Mouse sem fio",
        "descricao": "Mouse óptico 1200 dpi",
        "unidade": "UN",
        "preco": "99.90",
        "categoria": {
          "id": "2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3",
          "nome": "Informática"
        },
        "ativo": true,
        "criadoEm": "2026-11-18T14:30:00Z",
        "atualizadoEm": "2026-11-18T15:10:00Z",
        "versao": 1
      }
      """;

  private static final String JSON_ITENS_PAGINA =
      """
      {
        "content": [
          {
            "id": "9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44",
            "tipo": "PRODUTO",
            "codigo": "MSE-01",
            "nome": "Mouse sem fio",
            "descricao": "Mouse óptico 1200 dpi",
            "unidade": "UN",
            "preco": "89.90",
            "categoria": {
              "id": "2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3",
              "nome": "Informática"
            },
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
        "instance": "/api/v1/itens-catalogo",
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
        "instance": "/api/v1/itens-catalogo",
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
        "instance": "/api/v1/itens-catalogo",
        "code": "VALIDATION_ERROR",
        "errors": [
          {
            "field": "preco",
            "message": "deve ser um número não negativo com no máximo 2 casas decimais (ex.: 89.90)"
          }
        ],
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_ITEM_NAO_ENCONTRADO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/item-nao-encontrado",
        "title": "Item de catálogo não encontrado",
        "status": 404,
        "detail": "Item de catálogo não encontrado.",
        "instance": "/api/v1/itens-catalogo/9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44",
        "code": "ITEM_NOT_FOUND",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_CODIGO_DUPLICADO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/item-codigo-duplicado",
        "title": "Código já cadastrado",
        "status": 409,
        "detail": "Já existe um item com este código na empresa.",
        "instance": "/api/v1/itens-catalogo",
        "code": "ITEM_CODE_DUPLICATE",
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
        "instance": "/api/v1/itens-catalogo/9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44",
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
        "instance": "/api/v1/itens-catalogo",
        "code": "COMPANY_NOT_ACTIVE",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_TIPO_IMUTAVEL =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/item-tipo-imutavel",
        "title": "Tipo do item não pode ser alterado",
        "status": 422,
        "detail": "O tipo do item (PRODUTO/SERVICO) não pode ser alterado após a criação.",
        "instance": "/api/v1/itens-catalogo/9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44",
        "code": "ITEM_TYPE_IMMUTABLE",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_CATEGORIA_INVALIDA =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/categoria-invalida",
        "title": "Categoria inválida",
        "status": 422,
        "detail": "A categoria informada não existe na empresa ativa ou está inativa.",
        "instance": "/api/v1/itens-catalogo",
        "code": "CATEGORY_INVALID",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_ITEM_COM_HISTORICO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/item-com-historico",
        "title": "Item não pode ser excluído",
        "status": 422,
        "detail": "O item possui vendas ou compras vinculadas e não pode ser excluído. Inative o item.",
        "instance": "/api/v1/itens-catalogo/9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44",
        "code": "ITEM_HAS_HISTORY",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private final ItemCatalogoService itemCatalogoService;

  public ItemCatalogoController(ItemCatalogoService itemCatalogoService) {
    this.itemCatalogoService = itemCatalogoService;
  }

  @Operation(
      summary = "Lista itens do catálogo da empresa ativa",
      description =
          "Listagem paginada (RNF-BE-13) com busca livre `q` por nome ou código (contém, sem"
              + " distinguir maiúsculas e acentos, RN-10) e filtros `tipo` (PRODUTO/SERVICO),"
              + " `categoriaId` e `situacao` (padrão ATIVO), combinados entre si (E). `sort`"
              + " aceita `nome` (padrão), `preco` ou `criadoEm`, com direção opcional"
              + " `asc`/`desc` (RNF-BE-16). Telas de controle de estoque DEVEM consultar com"
              + " `tipo=PRODUTO` (RN-08).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Itens da página atual.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "pagina", value = JSON_ITENS_PAGINA))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Parâmetro de consulta inválido (ex.: sort fora de nome/preco/criadoEm, tipo"
                    + " fora de PRODUTO/SERVICO, situacao fora de ATIVO/INATIVO/TODOS, size acima"
                    + " de 100) (código VALIDATION_ERROR).",
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
  public ResponseEntity<ItemCatalogoPageResponse> listar(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String tipo,
      @RequestParam(required = false) UUID categoriaId,
      @RequestParam(defaultValue = "ATIVO") String situacao,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "nome") String sort) {
    return ResponseEntity.ok(
        ItemCatalogoPageResponse.from(
            itemCatalogoService.listar(q, tipo, categoriaId, situacao, page, size, sort)));
  }

  @Operation(
      summary = "Consulta um item do catálogo",
      description =
          "Consulta um item da empresa ativa (RN-01). Itens de outras empresas retornam 404"
              + " (escopo da empresa ativa, RNF-BE-26). Itens inativos continuam consultáveis"
              + " (RN-10). A resposta não contém `estoqueMinimo` (RN-07).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Item encontrado.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "item", value = JSON_ITEM))),
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
            description = "Item inexistente na empresa ativa (código ITEM_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_ITEM_NAO_ENCONTRADO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @GetMapping("/{id}")
  public ResponseEntity<ItemCatalogoResponse> consultar(@PathVariable UUID id) {
    return ResponseEntity.ok(ItemCatalogoResponse.from(itemCatalogoService.consultar(id)));
  }

  @Operation(
      summary = "Cadastra um item no catálogo",
      description =
          "Cria um item ativo na empresa ativa (RN-01 a RN-05). O tipo é obrigatório e imutável"
              + " (RN-02); o preço é decimal em string, não negativo, com até 2 casas (RN-03,"
              + " RNF-033); a unidade tem padrão UN (produto) ou H (serviço) quando ausente"
              + " (RN-04); a categoria, quando informada, deve existir e estar ativa (RN-05)."
              + " Retorna 201 com Location. Sem empresa ativa, 422 COMPANY_NOT_ACTIVE (RNF-096).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Item criado, já ativo, com header Location.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "item-criado", value = JSON_ITEM))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Campos inválidos (ex.: preço negativo ou com mais de 2 casas, unidade fora de"
                    + " 1 a 6 maiúsculas) (código VALIDATION_ERROR).",
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
            description = "Código já usado na empresa (código ITEM_CODE_DUPLICATE, RN-04).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "code-duplicate", value = JSON_CODIGO_DUPLICADO))),
        @ApiResponse(
            responseCode = "422",
            description =
                "Sem empresa ativa (código COMPANY_NOT_ACTIVE, RNF-096) ou categoria inexistente"
                    + " ou inativa (código CATEGORY_INVALID, RN-05).",
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
  public ResponseEntity<ItemCatalogoResponse> cadastrar(
      @Valid @RequestBody ItemCatalogoRequest request) {
    var item =
        itemCatalogoService.cadastrar(
            request.tipo(),
            request.codigo(),
            request.nome(),
            request.descricao(),
            request.unidade(),
            new BigDecimal(request.preco()),
            request.categoriaId());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(item.id())
            .toUri();
    return ResponseEntity.created(location).body(ItemCatalogoResponse.from(item));
  }

  @Operation(
      summary = "Edita um item do catálogo",
      description =
          "Atualiza os dados do item informando a `versao` lida (RNF-BE-28). O `tipo` enviado"
              + " DEVE ser igual ao atual (RN-02) — caso contrário, 422 ITEM_TYPE_IMMUTABLE. A"
              + " categoria, quando informada, deve existir e estar ativa (RN-05).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Item atualizado (versao incrementada).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "item-atualizado", value = JSON_ITEM_VERSAO_1))),
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
            description = "Item inexistente na empresa ativa (código ITEM_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_ITEM_NAO_ENCONTRADO))),
        @ApiResponse(
            responseCode = "409",
            description =
                "Versão desatualizada (código VERSION_CONFLICT) ou código já usado na empresa"
                    + " (código ITEM_CODE_DUPLICATE, RN-04).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "version-conflict", value = JSON_CONFLITO_VERSAO))),
        @ApiResponse(
            responseCode = "422",
            description =
                "Tentativa de alterar o tipo (código ITEM_TYPE_IMMUTABLE, RN-02) ou categoria"
                    + " inexistente/inativa (código CATEGORY_INVALID, RN-05).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "type-immutable", value = JSON_TIPO_IMUTAVEL)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @PutMapping("/{id}")
  public ResponseEntity<ItemCatalogoResponse> atualizar(
      @PathVariable UUID id, @Valid @RequestBody ItemCatalogoRequest request) {
    var item =
        itemCatalogoService.atualizar(
            id,
            request.versao() == null ? -1 : request.versao(),
            request.tipo(),
            request.codigo(),
            request.nome(),
            request.descricao(),
            request.unidade(),
            new BigDecimal(request.preco()),
            request.categoriaId());
    return ResponseEntity.ok(ItemCatalogoResponse.from(item));
  }

  @Operation(
      summary = "Inativa ou reativa um item do catálogo",
      description =
          "Alterna a situação do item (RN-10): inativo sai da listagem padrão, mas continua"
              + " consultável e vinculado ao histórico. A operação é idempotente e exige a"
              + " `versao` lida (RNF-BE-28).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Situação alterada.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "item-inativado", value = JSON_ITEM_VERSAO_1))),
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
            description = "Item inexistente na empresa ativa (código ITEM_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_ITEM_NAO_ENCONTRADO))),
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
  public ResponseEntity<ItemCatalogoResponse> alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody SituacaoItemCatalogoRequest request) {
    var item = itemCatalogoService.alterarSituacao(id, request.versao(), request.ativo());
    return ResponseEntity.ok(ItemCatalogoResponse.from(item));
  }

  @Operation(
      summary = "Exclui um item do catálogo",
      description =
          "Exclui o item apenas se ele NÃO possuir vínculo (venda ou compra), verificado pelos"
              + " verificadores registrados (RN-09, RNF-097). Com vínculo, 422 ITEM_HAS_HISTORY"
              + " — o item deve ser inativado.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Item excluído (sem vínculos)."),
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
            description = "Item inexistente na empresa ativa (código ITEM_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_ITEM_NAO_ENCONTRADO))),
        @ApiResponse(
            responseCode = "422",
            description =
                "Item com histórico (vendas ou compras) não pode ser excluído (código"
                    + " ITEM_HAS_HISTORY, RN-09).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "item-has-history", value = JSON_ITEM_COM_HISTORICO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    itemCatalogoService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
