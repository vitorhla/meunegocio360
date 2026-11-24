package br.com.meunegocio360.core.catalogo.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "Altera a situação da categoria — inativa (ativo = false) ou reativa (ativo = true)"
            + " (RF-BE-005, RN-06). A operação é idempotente.",
    example = "{\"ativo\":false,\"versao\":0}")
public record SituacaoCategoriaRequest(
    @Schema(description = "Situação desejada: true (ativa) ou false (inativa).", example = "false")
        boolean ativo,
    @Schema(
            description = "Versão atual da categoria (controle otimista, RNF-BE-28).",
            example = "0")
        long versao) {}
