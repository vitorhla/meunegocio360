package br.com.meunegocio360.core.empresa.api;

import br.com.meunegocio360.core.empresa.application.EmpresaService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Tag(
    name = "Empresa",
    description =
        "Cadastro e consulta da empresa (RF-BE-002). A empresa é o primeiro registro de dados"
            + " do sistema e o contexto de todos os demais módulos (RNF-096): a partir de"
            + " RF-BE-003, operações de negócio sem empresa ativa retornam 422 com code"
            + " COMPANY_NOT_ACTIVE. Modelo de empresa única ativa (RN-02); a empresa é"
            + " inativada, nunca excluída (RN-04). Somente ADMIN escreve (RN-03); qualquer"
            + " perfil autenticado consulta.")
@RestController
@RequestMapping("/api/v1/empresa")
public class EmpresaController {

  private static final String JSON_EMPRESA =
      """
      {
        "id": "0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10",
        "razaoSocial": "Comércio Exemplo Ltda",
        "nomeFantasia": "Exemplo Informática",
        "cnpj": "11222333000181",
        "email": "contato@exemplo.com.br",
        "telefone": "81999998888",
        "fusoHorario": "America/Sao_Paulo",
        "moeda": "BRL",
        "ativa": true,
        "criadoEm": "2026-11-18T14:30:00Z",
        "atualizadoEm": "2026-11-18T14:30:00Z",
        "versao": 0
      }
      """;

  private static final String JSON_EMPRESA_VERSAO_1 =
      """
      {
        "id": "0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10",
        "razaoSocial": "Comércio Exemplo Ltda",
        "nomeFantasia": "Exemplo Informática",
        "cnpj": "11222333000181",
        "email": "contato@exemplo.com.br",
        "telefone": "81999998888",
        "fusoHorario": "America/Sao_Paulo",
        "moeda": "BRL",
        "ativa": true,
        "criadoEm": "2026-11-18T14:30:00Z",
        "atualizadoEm": "2026-11-19T10:00:00Z",
        "versao": 1
      }
      """;

  private static final String JSON_VALIDACAO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/validacao",
        "title": "Dados inválidos",
        "status": 400,
        "detail": "Um ou mais campos são inválidos.",
        "instance": "/api/v1/empresa",
        "code": "VALIDATION_ERROR",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10",
        "errors": [
          { "field": "cnpj", "message": "deve ser um CNPJ válido (14 dígitos, sem pontuação)" }
        ]
      }
      """;

  private static final String JSON_NAO_AUTENTICADO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/nao-autorizado",
        "title": "Não autenticado",
        "status": 401,
        "detail": "Token de acesso ausente, inválido ou expirado.",
        "instance": "/api/v1/empresa",
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
        "instance": "/api/v1/empresa",
        "code": "ACCESS_DENIED",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_EMPRESA_NAO_CADASTRADA =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/empresa-nao-cadastrada",
        "title": "Empresa não cadastrada",
        "status": 404,
        "detail": "Nenhuma empresa ativa cadastrada.",
        "instance": "/api/v1/empresa",
        "code": "COMPANY_NOT_REGISTERED",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_CNPJ_DUPLICADO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/empresa-cnpj-duplicado",
        "title": "CNPJ já cadastrado",
        "status": 409,
        "detail": "CNPJ já cadastrado em outra empresa.",
        "instance": "/api/v1/empresa",
        "code": "COMPANY_CNPJ_DUPLICATE",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private static final String JSON_EMPRESA_JA_ATIVA =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/empresa-ja-ativa",
        "title": "Já existe empresa ativa",
        "status": 409,
        "detail": "Já existe uma empresa ativa.",
        "instance": "/api/v1/empresa",
        "code": "COMPANY_ALREADY_ACTIVE",
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
        "instance": "/api/v1/empresa/{id}",
        "code": "VERSION_CONFLICT",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private final EmpresaService empresaService;

  public EmpresaController(EmpresaService empresaService) {
    this.empresaService = empresaService;
  }

  @Operation(
      summary = "Consulta a empresa ativa",
      description =
          "Retorna a empresa ativa (no máximo uma por instalação, RN-02). Retorna 404 com code"
              + " COMPANY_NOT_REGISTERED quando não existe empresa ativa — a interface usa isso"
              + " para iniciar o primeiro cadastro. Qualquer perfil autenticado pode consultar"
              + " (RN-03).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Empresa ativa encontrada.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "empresa-ativa", value = JSON_EMPRESA))),
        @ApiResponse(
            responseCode = "401",
            description = "Token ausente, inválido ou expirado (código UNAUTHORIZED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "unauthorized", value = JSON_NAO_AUTENTICADO))),
        @ApiResponse(
            responseCode = "404",
            description = "Não existe empresa ativa (código COMPANY_NOT_REGISTERED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "company-not-registered",
                            value = JSON_EMPRESA_NAO_CADASTRADA)))
      })
  @GetMapping
  public ResponseEntity<EmpresaResponse> consultar() {
    return ResponseEntity.ok(EmpresaResponse.from(empresaService.consultarAtiva()));
  }

  @Operation(
      summary = "Cadastra a empresa (ADMIN)",
      description =
          "Cria o primeiro registro de dados do sistema: a empresa, já ativa, com fuso"
              + " America/Sao_Paulo e moeda BRL por padrão (RN-05). Campos de texto são"
              + " recortados (trim) e os opcionais aceitam vazio (RN-06). Retorna 201 com"
              + " Location e o objeto da empresa.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "201",
            description = "Empresa criada, já ativa, com header Location.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "empresa-criada", value = JSON_EMPRESA))),
        @ApiResponse(
            responseCode = "400",
            description =
                "Campos inválidos (ex.: CNPJ com dígito verificador errado, razão social"
                    + " curta) (código VALIDATION_ERROR).",
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
            description = "Perfil sem permissão — somente ADMIN (código ACCESS_DENIED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "access-denied", value = JSON_SEM_PERMISSAO))),
        @ApiResponse(
            responseCode = "409",
            description =
                "Já existe empresa ativa (código COMPANY_ALREADY_ACTIVE, RN-02) ou CNPJ já"
                    + " cadastrado (código COMPANY_CNPJ_DUPLICATE, RN-01).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "company-already-active",
                            value = JSON_EMPRESA_JA_ATIVA)))
      })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PostMapping
  public ResponseEntity<EmpresaResponse> cadastrar(@Valid @RequestBody EmpresaRequest request) {
    var empresa =
        empresaService.cadastrar(
            request.razaoSocial(),
            request.nomeFantasia(),
            request.cnpj(),
            request.email(),
            request.telefone(),
            request.fusoHorario(),
            request.moeda());
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(empresa.id())
            .toUri();
    return ResponseEntity.created(location).body(EmpresaResponse.from(empresa));
  }

  @Operation(
      summary = "Edita a empresa (ADMIN)",
      description =
          "Atualiza os dados da empresa informando a `versao` lida (controle otimista,"
              + " RNF-BE-28). O CNPJ pode ser alterado, respeitando a unicidade (RN-01). A"
              + " situação (ativa/inativa) NÃO é alterada aqui: use PATCH /{id}/situacao.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Empresa atualizada (versao incrementada).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "empresa-atualizada",
                            value = JSON_EMPRESA_VERSAO_1))),
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
            description = "Perfil sem permissão — somente ADMIN (código ACCESS_DENIED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "access-denied", value = JSON_SEM_PERMISSAO))),
        @ApiResponse(
            responseCode = "404",
            description = "Empresa não encontrada (código COMPANY_NOT_REGISTERED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "company-not-registered",
                            value = JSON_EMPRESA_NAO_CADASTRADA))),
        @ApiResponse(
            responseCode = "409",
            description =
                "Versão desatualizada (código VERSION_CONFLICT), CNPJ já cadastrado (código"
                    + " COMPANY_CNPJ_DUPLICATE) ou já existe empresa ativa (código"
                    + " COMPANY_ALREADY_ACTIVE).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "version-conflict", value = JSON_CONFLITO_VERSAO)))
      })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<EmpresaResponse> atualizar(
      @PathVariable UUID id, @Valid @RequestBody EmpresaRequest request) {
    var empresa =
        empresaService.atualizar(
            id,
            request.versao() == null ? -1 : request.versao(),
            request.razaoSocial(),
            request.nomeFantasia(),
            request.cnpj(),
            request.email(),
            request.telefone(),
            request.fusoHorario(),
            request.moeda());
    return ResponseEntity.ok(EmpresaResponse.from(empresa));
  }

  @Operation(
      summary = "Inativa ou reativa a empresa (ADMIN)",
      description =
          "Alterna a situação da empresa (RN-02, RN-04). Inativa a empresa ativa quando"
              + " `ativa = false`; reativa quando `ativa = true`, exceto se já existir outra"
              + " empresa ativa (409 COMPANY_ALREADY_ACTIVE). Enquanto não há empresa ativa,"
              + " nenhum registro de negócio pode ser criado (422 COMPANY_NOT_ACTIVE).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Situação alterada.",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples =
                        @ExampleObject(name = "empresa-inativada", value = JSON_EMPRESA_VERSAO_1))),
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
            description = "Perfil sem permissão — somente ADMIN (código ACCESS_DENIED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples = @ExampleObject(name = "access-denied", value = JSON_SEM_PERMISSAO))),
        @ApiResponse(
            responseCode = "404",
            description = "Empresa não encontrada (código COMPANY_NOT_REGISTERED).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "company-not-registered",
                            value = JSON_EMPRESA_NAO_CADASTRADA))),
        @ApiResponse(
            responseCode = "409",
            description =
                "Versão desatualizada (código VERSION_CONFLICT) ou reativação com outra empresa"
                    + " ativa (código COMPANY_ALREADY_ACTIVE).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
                    examples =
                        @ExampleObject(
                            name = "company-already-active",
                            value = JSON_EMPRESA_JA_ATIVA)))
      })
  @PreAuthorize("hasAuthority('ADMIN')")
  @PatchMapping("/{id}/situacao")
  public ResponseEntity<EmpresaResponse> alterarSituacao(
      @PathVariable UUID id, @Valid @RequestBody SituacaoRequest request) {
    var empresa = empresaService.alterarSituacao(id, request.versao(), request.ativa());
    return ResponseEntity.ok(EmpresaResponse.from(empresa));
  }
}
