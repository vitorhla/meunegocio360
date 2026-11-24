package br.com.meunegocio360.core.fornecedor.api;

import br.com.meunegocio360.core.fornecedor.application.FornecedorService;
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
    name = "Fornecedores",
    description =
        "Cadastro de fornecedores da empresa ativa (RF-BE-004): listagem com busca por nome ou"
            + " documento, consulta, cadastro, edição, inativação/reativação e exclusão"
            + " condicionada (RN-01 a RN-05). Somente ADMIN e OPERADOR_FINANCEIRO (matriz de"
            + " autorização); OPERADOR_COMERCIAL recebe 403 (RNF-BE-47).")
@RestController
@RequestMapping("/api/v1/fornecedores")
public class FornecedorController {

  private static final String JSON_FORNECEDOR =
      """
      {
        "id": "b7d21c44-9a0e-4f1b-8c6d-5e2a7f3d0b19",
        "tipoPessoa": "JURIDICA",
        "nome": "Distribuidora Alfa Ltda",
        "documento": "[CARTAO_CREDITO_REDACTED]",
        "email": "[EMAIL_REDACTED]",
        "telefone": "[TELEFONE_BR_REDACTED]",
        "ativo": true,
        "criadoEm": "2026-11-19T14:30:00Z",
        "atualizadoEm": "2026-11-19T14:30:00Z",
        "versao": 0
      }
      """;

  private static final String JSON_FORNECEDOR_VERSAO_1 =
      """
      {
        "id": "b7d21c44-9a0e-4f1b-8c6d-5e2a7f3d0b19",
        "tipoPessoa": "JURIDICA",
        "nome": "Distribuidora Alfa Ltda",
        "documento": "[CARTAO_CREDITO_REDACTED]",
        "email": "[EMAIL_REDACTED]",
        "telefone": "[TELEFONE_BR_REDACTED]",
        "ativo": true,
        "criadoEm": "2026-11-19T14:30:00Z",
        "atualizadoEm": "2026-11-19T15:10:00Z",
        "versao": 1
      }
      """;

  private static final String JSON_FORNECEDORES_PAGINA =
      """
      {
        "content": [
          {
            "id": "b7d21c44-9a0e-4f1b-8c6d-5e2a7f3d0b19",
            "tipoPessoa": "JURIDICA",
            "nome": "Distribuidora Alfa Ltda",
            "documento": "[CARTAO_CREDITO_REDACTED]",
            "email": "[EMAIL_REDACTED]",
            "telefone": "[TELEFONE_BR_REDACTED]",
            "ativo": true,
            "criadoEm": "2026-11-19T14:30:00Z",
            "atualizadoEm": "2026-11-19T14:30:00Z",
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
        "instance": "/api/v1/fornecedores",
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
        "instance": "/api/v1/fornecedores",
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
        "instance": "/api/v1/fornecedores",
        "code": "VALIDATION_ERROR",
        "errors": [
          {
            "field": "documento",
            "message": "deve ter 14 dígitos (CNPJ) para o tipoPessoa informado"
          }
        ],
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_FORNECEDOR_NAO_ENCONTRADO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/fornecedor-nao-encontrado",
        "title": "Fornecedor não encontrado",
        "status": 404,
        "detail": "Fornecedor não encontrado.",
        "instance": "/api/v1/fornecedores/b7d21c44-9a0e-4f1b-8c6d-5e2a7f3d0b19",
        "code": "SUPPLIER_NOT_FOUND",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_DOCUMENTO_DUPLICADO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/fornecedor-documento-duplicado",
        "title": "Documento já cadastrado",
        "status": 409,
        "detail": "Fornecedor com este documento já existe (pode estar inativo).",
        "instance": "/api/v1/fornecedores",
        "code": "SUPPLIER_DOCUMENT_DUPLICATE",
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
        "instance": "/api/v1/fornecedores/b7d21c44-9a0e-4f1b-8c6d-5e2a7f3d0b19",
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
        "instance": "/api/v1/fornecedores",
        "code": "COMPANY_NOT_ACTIVE",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_FORNECEDOR_COM_HISTORICO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/fornecedor-com-historico",
        "title": "Fornecedor não pode ser excluído",
        "status": 422,
        "detail": "O fornecedor possui pedidos de compra ou contas a pagar vinculados e não pode ser excluído. Inative o fornecedor.",
        "instance": "/api/v1/fornecedores/b7d21c44-9a0e-4f1b-8c6d-5e2a7f3d0b19",
        "code": "SUPPLIER_HAS_HISTORY",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private final FornecedorService fornecedorService;

  public FornecedorController(FornecedorService fornecedorService) {
    this.fornecedorService = fornecedorService;
  }

  @Operation(
      summary = "Lista fornecedores da empresa ativa",
      description =
          "Listagem paginada (RNF-BE-13) com busca livre `q` por nome (contém, sem distinguir"
              + " maiúsculas e acentos, RF-BE-003 RN-08) e/ou por prefixo de documento (dígitos,"
              + " máscara ignorada) e filtro `situacao` (padrão ATIVO; RF-BE-003 RN-07). `sort`"
              + " aceita somente `nome` (padrão) ou `criadoEm`, com direção opcional"
              + " `asc`/`desc` (RNF-BE-16).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Fornecedores da página atual.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "pagina", value = JSON_FORNECEDORES_PAGINA))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Parâmetro de consulta inválido (ex.: sort fora de nome/criadoEm, situacao fora"
                    + " de ATIVO/INATIVO/TODOS, size acima de 100) (código VALIDATION_ERROR).",
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
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_FINANCEIRO')")
  @GetMapping
  public ResponseEntity<FornecedorPageResponse> listar(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "ATIVO") String situacao,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "nome") String sort) {
    return ResponseEntity.ok(
        FornecedorPageResponse.from(fornecedorService.listar(q, situacao, page, size, sort)));
  }

  @Operation(
      summary = "Consulta um fornecedor",
      description =
          "Consulta um fornecedor da empresa ativa (RF-BE-003 RN-01). Fornecedores de outras"
              + " empresas — ativas ou não — retornam 404 (escopo da empresa ativa, RNF-BE-26)."
              + " Fornecedores inativos continuam consultáveis (RF-BE-003 RN-07).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Fornecedor encontrado.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "fornecedor", value = JSON_FORNECEDOR))),
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
            description = "Fornecedor inexistente na empresa ativa (código SUPPLIER_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_FORNECEDOR_NAO_ENCONTRADO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_FINANCEIRO')")
  @GetMapping("/{id}")
  public ResponseEntity<FornecedorResponse> consultar(@PathVariable UUID id) {
    return ResponseEntity.ok(FornecedorResponse.from(fornecedorService.consultar(id)));
  }

  @Operation(
      summary = "Cadastra um fornecedor",
      description =
          "Cria um fornecedor ativo na empresa ativa (RF-BE-003 RN-01 a RN-04). O documento"
              + " (CPF/CNPJ) pode vir com a máscara e é armazenado somente com dígitos, validado"
              + " pelos dígitos verificadores (RF-BE-003 RN-02, RNF-BE-27). O mesmo CNPJ PODE"
              + " existir como cliente e como fornecedor: a unicidade é por entidade (RN-02)."
              + " Retorna 201 com Location. Sem empresa ativa, 422 COMPANY_NOT_ACTIVE (RNF-096).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Fornecedor criado, já ativo, com header Location.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "fornecedor-criado", value = JSON_FORNECEDOR))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Campos inválidos (ex.: documento com dígito verificador errado, tamanho"
                    + " incompatível com o tipoPessoa) (código VALIDATION_ERROR).",
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
            description =
                "Documento já cadastrado na empresa, em fornecedor ativo ou inativo (código"
                    + " SUPPLIER_DOCUMENT_DUPLICATE, RN-02).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "document-duplicate",
                            value = JSON_DOCUMENTO_DUPLICADO))),
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
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_FINANCEIRO')")
  @PostMapping
  public ResponseEntity<FornecedorResponse> cadastrar(
      @Valid @RequestBody FornecedorRequest request) {
    var fornecedor =
        fornecedorService.cadastrar(
            request.tipoPessoa(),
            request.nome(),
            request.documento(),
            request.email(),
            request.telefone());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(fornecedor.id())
            .toUri();
    return ResponseEntity.created(location).body(FornecedorResponse.from(fornecedor));
  }

  @Operation(
      summary = "Edita um fornecedor",
      description =
          "Atualiza os dados do fornecedor informando a `versao` lida (controle otimista,"
              + " RF-BE-003 RN-09, RNF-BE-28). O `tipoPessoa` e o documento podem ser corrigidos,"
              + " respeitando a validação (RF-BE-003 RN-02) e a unicidade por empresa (RN-02).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Fornecedor atualizado (versao incrementada).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "fornecedor-atualizado",
                            value = JSON_FORNECEDOR_VERSAO_1))),
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
            description = "Fornecedor inexistente na empresa ativa (código SUPPLIER_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "not-found",
                            value = JSON_FORNECEDOR_NAO_ENCONTRADO))),
        @ApiResponse(
            responseCode = "409",
            description =
                "Versão desatualizada (código VERSION_CONFLICT) ou documento já cadastrado"
                    + " (código SUPPLIER_DOCUMENT_DUPLICATE, RN-02).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "version-conflict", value = JSON_CONFLITO_VERSAO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_FINANCEIRO')")
  @PutMapping("/{id}")
  public ResponseEntity<FornecedorResponse> atualizar(
      @PathVariable UUID id, @Valid @RequestBody FornecedorRequest request) {
    var fornecedor =
        fornecedorService.atualizar(
            id,
            request.versao() == null ? -1 : request.versao(),
            request.tipoPessoa(),
            request.nome(),
            request.documento(),
            request.email(),
            request.telefone());
    return ResponseEntity.ok(FornecedorResponse.from(fornecedor));
  }

  @Operation(
      summary = "Inativa ou reativa um fornecedor",
      description =
          "Alterna a situação do fornecedor (RF-BE-003 RN-07): inativo sai da listagem padrão, mas"
              + " continua consultável e vinculado ao histórico. A operação é idempotente e exige"
              + " a `versao` lida (RNF-BE-28).")
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
                            name = "fornecedor-inativado",
                            value = JSON_FORNECEDOR_VERSAO_1))),
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
            description = "Fornecedor inexistente na empresa ativa (código SUPPLIER_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "not-found",
                            value = JSON_FORNECEDOR_NAO_ENCONTRADO))),
        @ApiResponse(
            responseCode = "409",
            description = "Versão desatualizada (código VERSION_CONFLICT).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "version-conflict", value = JSON_CONFLITO_VERSAO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_FINANCEIRO')")
  @PatchMapping("/{id}/situacao")
  public ResponseEntity<FornecedorResponse> alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody SituacaoFornecedorRequest request) {
    var fornecedor = fornecedorService.alterarSituacao(id, request.versao(), request.ativo());
    return ResponseEntity.ok(FornecedorResponse.from(fornecedor));
  }

  @Operation(
      summary = "Exclui um fornecedor",
      description =
          "Exclui o fornecedor apenas se ele NÃO possuir vínculo (pedido de compra ou conta a"
              + " pagar), verificado pelos verificadores registrados (RN-03 e RN-04, RNF-097). Com"
              + " vínculo, 422 SUPPLIER_HAS_HISTORY — o fornecedor deve ser inativado.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Fornecedor excluído (sem vínculos)."),
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
            description = "Fornecedor inexistente na empresa ativa (código SUPPLIER_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "not-found",
                            value = JSON_FORNECEDOR_NAO_ENCONTRADO))),
        @ApiResponse(
            responseCode = "422",
            description =
                "Fornecedor com histórico (pedidos de compra ou contas a pagar) não pode ser"
                    + " excluído (código SUPPLIER_HAS_HISTORY, RN-03).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "supplier-has-history",
                            value = JSON_FORNECEDOR_COM_HISTORICO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_FINANCEIRO')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    fornecedorService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
