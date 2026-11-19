package br.com.meunegocio360.core.empresa.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "Altera a situação da empresa — inativa (ativa = false) ou reativa (ativa = true)"
            + " (RF-BE-002, RN-02 e RN-04).",
    example = "{\"ativa\":false,\"versao\":0}")
public record SituacaoRequest(
    @Schema(description = "Situação desejada: true (ativa) ou false (inativa).", example = "false")
        boolean ativa,
    @Schema(description = "Versão atual da empresa (controle otimista, RNF-BE-28).", example = "0")
        long versao) {}
