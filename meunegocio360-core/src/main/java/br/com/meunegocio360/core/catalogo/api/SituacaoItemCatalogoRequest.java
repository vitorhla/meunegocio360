package br.com.meunegocio360.core.catalogo.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "Altera a situação do item — inativa (ativo = false) ou reativa (ativo = true)"
            + " (RF-BE-005, RN-10). A operação é idempotente.",
    example = "{\"ativo\":false,\"versao\":0}")
public record SituacaoItemCatalogoRequest(
    @Schema(description = "Situação desejada: true (ativo) ou false (inativo).", example = "false")
        boolean ativo,
    @Schema(description = "Versão atual do item (controle otimista, RNF-BE-28).", example = "0")
        long versao) {}
