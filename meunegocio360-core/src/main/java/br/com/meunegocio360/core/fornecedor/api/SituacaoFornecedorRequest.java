package br.com.meunegocio360.core.fornecedor.api;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    description =
        "Altera a situação do fornecedor — inativa (ativo = false) ou reativa (ativo = true)"
            + " (RF-BE-004, RF-BE-003 RN-07). A operação é idempotente.",
    example = "{\"ativo\":false,\"versao\":0}")
public record SituacaoFornecedorRequest(
    @Schema(description = "Situação desejada: true (ativo) ou false (inativo).", example = "false")
        boolean ativo,
    @Schema(
            description = "Versão atual do fornecedor (controle otimista, RNF-BE-28).",
            example = "0")
        long versao) {}
