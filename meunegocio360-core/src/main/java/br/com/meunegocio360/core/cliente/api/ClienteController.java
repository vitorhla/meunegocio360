package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.cliente.application.ClienteService;
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
    name = "Clientes",
    description =
        "Cadastro de clientes da empresa ativa (RF-BE-003): listagem com busca por nome ou"
            + " documento, consulta, cadastro, edição, inativação/reativação e exclusão"
            + " condicionada (RN-01 a RN-09). Somente ADMIN e OPERADOR_COMERCIAL (matriz de"
            + " autorização); OPERADOR_FINANCEIRO recebe 403 (RNF-BE-47).")
@RestController
@RequestMapping("/api/v1/clientes")
public class ClienteController {

  private static final String JSON_CLIENTE =
      """
      {
        "id": "6c1f0a52-3b7e-4f0e-9a3e-2f7a1d5b8c90",
        "tipoPessoa": "FISICA",
        "nome": "Maria da Silva",
        "documento": "[CPF_REDACTED]",
        "email": "[EMAIL_REDACTED]",
        "telefone": "[CPF_REDACTED]",
        "ativo": true,
        "criadoEm": "2026-11-18T14:30:00Z",
        "atualizadoEm": "2026-11-18T14:30:00Z",
        "versao": 0
      }
      """;

  private static final String JSON_CLIENTE_VERSAO_1 =
      """
      {
        "id": "6c1f0a52-3b7e-4f0e-9a3e-2f7a1d5b8c90",
        "tipoPessoa": "FISICA",
        "nome": "Maria da Silva",
        "documento": "[CPF_REDACTED]",
        "email": "[EMAIL_REDACTED]",
        "telefone": "[CPF_REDACTED]",
        "ativo": true,
        "criadoEm": "2026-11-18T14:30:00Z",
        "atualizadoEm": "2026-11-18T15:10:00Z",
        "versao": 1
      }
      """;

  private static final String JSON_CLIENTES_PAGINA =
      """
      {
        "content": [
          {
            "id": "6c1f0a52-3b7e-4f0e-9a3e-2f7a1d5b8c90",
            "tipoPessoa": "FISICA",
            "nome": "Maria da Silva",
            "documento": "[CPF_REDACTED]",
            "email": "[EMAIL_REDACTED]",
            "telefone": "[CPF_REDACTED]",
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
        "instance": "/api/v1/clientes",
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
        "instance": "/api/v1/clientes",
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
        "instance": "/api/v1/clientes",
        "code": "VALIDATION_ERROR",
        "errors": [
          {
            "field": "documento",
            "message": "deve ter 11 dígitos (CPF) para o tipoPessoa informado"
          }
        ],
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_CLIENTE_NAO_ENCONTRADO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/cliente-nao-encontrado",
        "title": "Cliente não encontrado",
        "status": 404,
        "detail": "Cliente não encontrado.",
        "instance": "/api/v1/clientes/6c1f0a52-3b7e-4f0e-9a3e-2f7a1d5b8c90",
        "code": "CUSTOMER_NOT_FOUND",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_DOCUMENTO_DUPLICADO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/cliente-documento-duplicado",
        "title": "Documento já cadastrado",
        "status": 409,
        "detail": "Cliente com este documento já existe (pode estar inativo).",
        "instance": "/api/v1/clientes",
        "code": "CUSTOMER_DOCUMENT_DUPLICATE",
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
        "instance": "/api/v1/clientes/6c1f0a52-3b7e-4f0e-9a3e-2f7a1d5b8c90",
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
        "instance": "/api/v1/clientes",
        "code": "COMPANY_NOT_ACTIVE",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_CLIENTE_COM_HISTORICO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/cliente-com-historico",
        "title": "Cliente não pode ser excluído",
        "status": 422,
        "detail": "O cliente possui vendas ou ordens de serviço vinculadas e não pode ser excluído. Inative o cliente.",
        "instance": "/api/v1/clientes/6c1f0a52-3b7e-4f0e-9a3e-2f7a1d5b8c90",
        "code": "CUSTOMER_HAS_HISTORY",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private final ClienteService clienteService;

  public ClienteController(ClienteService clienteService) {
    this.clienteService = clienteService;
  }

  @Operation(
      summary = "Lista clientes da empresa ativa",
      description =
          "Listagem paginada (RNF-BE-13) com busca livre `q` por nome (contém, sem distinguir"
              + " maiúsculas e acentos, RN-08) e/ou por prefixo de documento (dígitos, máscara"
              + " ignorada) e filtro `situacao` (padrão ATIVO; RN-07). `sort` aceita somente"
              + " `nome` (padrão) ou `criadoEm`, com direção opcional `asc`/`desc` (RNF-BE-16).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Clientes da página atual.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "pagina", value = JSON_CLIENTES_PAGINA))),
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
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @GetMapping
  public ResponseEntity<ClientePageResponse> listar(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "ATIVO") String situacao,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "nome") String sort) {
    return ResponseEntity.ok(
        ClientePageResponse.from(clienteService.listar(q, situacao, page, size, sort)));
  }

  @Operation(
      summary = "Consulta um cliente",
      description =
          "Consulta um cliente da empresa ativa (RN-01). Clientes de outras empresas — ativas"
              + " ou não — retornam 404 (escopo da empresa ativa, RNF-BE-26). Clientes inativos"
              + " continuam consultáveis (RN-07).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cliente encontrado.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "cliente", value = JSON_CLIENTE))),
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
            description = "Cliente inexistente na empresa ativa (código CUSTOMER_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_CLIENTE_NAO_ENCONTRADO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @GetMapping("/{id}")
  public ResponseEntity<ClienteResponse> consultar(@PathVariable UUID id) {
    return ResponseEntity.ok(ClienteResponse.from(clienteService.consultar(id)));
  }

  @Operation(
      summary = "Cadastra um cliente",
      description =
          "Cria um cliente ativo na empresa ativa (RN-01 a RN-04). O documento (CPF/CNPJ) pode"
              + " vir com a máscara e é armazenado somente com dígitos, validado pelos dígitos"
              + " verificadores (RN-02, RNF-BE-27). Retorna 201 com Location. Sem empresa ativa,"
              + " 422 COMPANY_NOT_ACTIVE (RNF-096).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Cliente criado, já ativo, com header Location.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "cliente-criado", value = JSON_CLIENTE))),
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
                "Documento já cadastrado na empresa, em cliente ativo ou inativo (código"
                    + " CUSTOMER_DOCUMENT_DUPLICATE, RN-03).",
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
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @PostMapping
  public ResponseEntity<ClienteResponse> cadastrar(@Valid @RequestBody ClienteRequest request) {
    var cliente =
        clienteService.cadastrar(
            request.tipoPessoa(),
            request.nome(),
            request.documento(),
            request.email(),
            request.telefone());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(cliente.id())
            .toUri();
    return ResponseEntity.created(location).body(ClienteResponse.from(cliente));
  }

  @Operation(
      summary = "Edita um cliente",
      description =
          "Atualiza os dados do cliente informando a `versao` lida (controle otimista, RN-09,"
              + " RNF-BE-28). O `tipoPessoa` e o documento podem ser corrigidos, respeitando a"
              + " validação (RN-02) e a unicidade por empresa (RN-03).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cliente atualizado (versao incrementada).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "cliente-atualizado",
                            value = JSON_CLIENTE_VERSAO_1))),
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
            description = "Cliente inexistente na empresa ativa (código CUSTOMER_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_CLIENTE_NAO_ENCONTRADO))),
        @ApiResponse(
            responseCode = "409",
            description =
                "Versão desatualizada (código VERSION_CONFLICT) ou documento já cadastrado"
                    + " (código CUSTOMER_DOCUMENT_DUPLICATE, RN-03).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "version-conflict", value = JSON_CONFLITO_VERSAO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @PutMapping("/{id}")
  public ResponseEntity<ClienteResponse> atualizar(
      @PathVariable UUID id, @Valid @RequestBody ClienteRequest request) {
    var cliente =
        clienteService.atualizar(
            id,
            request.versao() == null ? -1 : request.versao(),
            request.tipoPessoa(),
            request.nome(),
            request.documento(),
            request.email(),
            request.telefone());
    return ResponseEntity.ok(ClienteResponse.from(cliente));
  }

  @Operation(
      summary = "Inativa ou reativa um cliente",
      description =
          "Alterna a situação do cliente (RN-07): inativo sai da listagem padrão, mas continua"
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
                        @ExampleObject(name = "cliente-inativado", value = JSON_CLIENTE_VERSAO_1))),
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
            description = "Cliente inexistente na empresa ativa (código CUSTOMER_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_CLIENTE_NAO_ENCONTRADO))),
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
  public ResponseEntity<ClienteResponse> alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody SituacaoClienteRequest request) {
    var cliente = clienteService.alterarSituacao(id, request.versao(), request.ativo());
    return ResponseEntity.ok(ClienteResponse.from(cliente));
  }

  @Operation(
      summary = "Exclui um cliente",
      description =
          "Exclui o cliente apenas se ele NÃO possuir vínculo (venda ou OS), verificado pelos"
              + " verificadores registrados (RN-05 e RN-06, RNF-097). Com vínculo, 422"
              + " CUSTOMER_HAS_HISTORY — o cliente deve ser inativado.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "204", description = "Cliente excluído (sem vínculos)."),
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
            description = "Cliente inexistente na empresa ativa (código CUSTOMER_NOT_FOUND).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "not-found", value = JSON_CLIENTE_NAO_ENCONTRADO))),
        @ApiResponse(
            responseCode = "422",
            description =
                "Cliente com histórico (vendas ou ordens de serviço) não pode ser excluído"
                    + " (código CUSTOMER_HAS_HISTORY, RN-05).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "customer-has-history",
                            value = JSON_CLIENTE_COM_HISTORICO)))
      })
  @PreAuthorize("hasAnyAuthority('ADMIN','OPERADOR_COMERCIAL')")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> excluir(@PathVariable UUID id) {
    clienteService.excluir(id);
    return ResponseEntity.noContent().build();
  }
}
