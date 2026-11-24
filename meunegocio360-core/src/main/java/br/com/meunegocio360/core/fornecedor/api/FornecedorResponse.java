package br.com.meunegocio360.core.fornecedor.api;

import br.com.meunegocio360.core.fornecedor.application.FornecedorService;
import br.com.meunegocio360.core.shared.domain.TipoPessoa;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(
    description =
        "Fornecedor da empresa ativa (RF-BE-004). O documento é armazenado só com dígitos.",
    example =
        "{\"id\":\"b7d21c44-9a0e-4f1b-8c6d-5e2a7f3d0b19\",\"tipoPessoa\":\"JURIDICA\","
            + "\"nome\":\"Distribuidora Alfa Ltda\",\"documento\":\"11222333000181\","
            + "\"email\":\"[EMAIL_REDACTED]\",\"telefone\":\"1134567890\","
            + "\"ativo\":true,\"criadoEm\":\"2026-11-19T14:30:00Z\","
            + "\"atualizadoEm\":\"2026-11-19T14:30:00Z\",\"versao\":0}")
public record FornecedorResponse(
    @Schema(
            description = "Identificador único do fornecedor.",
            example = "b7d21c44-9a0e-4f1b-8c6d-5e2a7f3d0b19")
        UUID id,
    @Schema(description = "Tipo da pessoa (RN-02).", example = "JURIDICA") TipoPessoa tipoPessoa,
    @Schema(description = "Nome do fornecedor.", example = "Distribuidora Alfa Ltda") String nome,
    @Schema(description = "CPF ou CNPJ, somente dígitos (RN-02).", example = "11222333000181")
        String documento,
    @Schema(description = "E-mail de contato. Opcional.", example = "[EMAIL_REDACTED]")
        String email,
    @Schema(description = "Telefone (10 ou 11 dígitos). Opcional.", example = "1134567890")
        String telefone,
    @Schema(
            description =
                "Situação: true (ativo) ou false (inativo). Inativos não aparecem na listagem padrão.",
            example = "true")
        boolean ativo,
    @Schema(description = "Data de criação (UTC).", example = "2026-11-19T14:30:00Z")
        Instant criadoEm,
    @Schema(description = "Data da última atualização (UTC).", example = "2026-11-19T14:30:00Z")
        Instant atualizadoEm,
    @Schema(description = "Versão para controle otimista (RNF-BE-28).", example = "0")
        long versao) {

  public static FornecedorResponse from(FornecedorService.FornecedorData data) {
    return new FornecedorResponse(
        data.id(),
        data.tipoPessoa(),
        data.nome(),
        data.documento(),
        data.email(),
        data.telefone(),
        data.ativo(),
        data.criadoEm(),
        data.atualizadoEm(),
        data.versao());
  }
}
