package br.com.meunegocio360.core.empresa.api;

import br.com.meunegocio360.core.empresa.application.EmpresaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(
    name = "Empresa",
    description =
        "Listagem das empresas (extensão da RF-BE-002). Permite à interface exibir as"
            + " empresas inativas quando não há empresa ativa, para que o ADMIN reative a"
            + " desejada (a empresa é inativada, nunca excluída, RN-04).")
@RestController
@RequestMapping("/api/v1/empresas")
public class EmpresasController {

  private static final String JSON_EMPRESAS =
      """
      [
        {
          "id": "0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10",
          "razaoSocial": "Comércio Exemplo Ltda",
          "nomeFantasia": "Exemplo Informática",
          "cnpj": "11222333000181",
          "email": "contato@exemplo.com.br",
          "telefone": "81987654321",
          "fusoHorario": "America/Sao_Paulo",
          "moeda": "BRL",
          "ativa": true,
          "criadoEm": "2026-11-18T14:30:00Z",
          "atualizadoEm": "2026-11-18T14:30:00Z",
          "versao": 0
        },
        {
          "id": "7c2e9b11-3d4a-4f5b-8e6c-9a0b1c2d3e4f",
          "razaoSocial": "Antigo Comércio S.A.",
          "nomeFantasia": null,
          "cnpj": "99888777000161",
          "email": null,
          "telefone": null,
          "fusoHorario": "America/Sao_Paulo",
          "moeda": "BRL",
          "ativa": false,
          "criadoEm": "2026-11-02T10:00:00Z",
          "atualizadoEm": "2026-11-10T09:00:00Z",
          "versao": 3
        }
      ]
      """;

  private static final String JSON_NAO_AUTENTICADO =
      """
      {
        "type": "https://meunegocio360.com.br/problemas/nao-autorizado",
        "title": "Não autenticado",
        "status": 401,
        "detail": "Token de acesso ausente, inválido ou expirado.",
        "instance": "/api/v1/empresas",
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
        "instance": "/api/v1/empresas",
        "code": "ACCESS_DENIED",
        "traceId": "9f1c2e7a-4b6e-4c1d-8a2f-5f6a2d1f9a10"
      }
      """;

  private final EmpresaService empresaService;

  public EmpresasController(EmpresaService empresaService) {
    this.empresaService = empresaService;
  }

  @Operation(
      summary = "Lista todas as empresas (ADMIN)",
      description =
          "Lista todas as empresas cadastradas, ativas e inativas, com o campo `ativa` de cada"
              + " uma. Ordenada por data de criação (mais antiga primeiro). Sem paginação:"
              + " o número de empresas por instalação é pequeno (RN-02 limita a uma ativa).")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Empresas encontradas (vazio quando nenhuma existe).",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(name = "empresas", value = JSON_EMPRESAS))),
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
                    examples = @ExampleObject(name = "access-denied", value = JSON_SEM_PERMISSAO)))
      })
  @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping
  public ResponseEntity<List<EmpresaResponse>> listar() {
    List<EmpresaResponse> empresas =
        empresaService.listar().stream().map(EmpresaResponse::from).toList();
    return ResponseEntity.ok(empresas);
  }
}
