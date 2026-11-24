package br.com.meunegocio360.core.catalogo.api;

import br.com.meunegocio360.core.catalogo.application.CategoriaService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(
    description = "Categoria da empresa ativa (RF-BE-005).",
    example =
        "{\"id\":\"2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3\",\"nome\":\"Informática\",\"ativo\":true,"
            + "\"criadoEm\":\"2026-11-18T14:30:00Z\",\"atualizadoEm\":\"2026-11-18T14:30:00Z\","
            + "\"versao\":0}")
public record CategoriaResponse(
    @Schema(
            description = "Identificador único da categoria.",
            example = "2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3")
        UUID id,
    @Schema(description = "Nome da categoria (RN-06).", example = "Informática") String nome,
    @Schema(
            description =
                "Situação: true (ativa) ou false (inativa). Inativas não aparecem na listagem"
                    + " padrão.",
            example = "true")
        boolean ativo,
    @Schema(description = "Data de criação (UTC).", example = "2026-11-18T14:30:00Z")
        Instant criadoEm,
    @Schema(description = "Data da última atualização (UTC).", example = "2026-11-18T14:30:00Z")
        Instant atualizadoEm,
    @Schema(description = "Versão para controle otimista (RNF-BE-28).", example = "0")
        long versao) {

  public static CategoriaResponse from(CategoriaService.CategoriaData data) {
    return new CategoriaResponse(
        data.id(), data.nome(), data.ativo(), data.criadoEm(), data.atualizadoEm(), data.versao());
  }
}
