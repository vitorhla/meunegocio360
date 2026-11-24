package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.cliente.application.ClienteService;
import br.com.meunegocio360.core.shared.domain.TipoPessoa;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(
    description = "Cliente da empresa ativa (RF-BE-003). O documento é armazenado só com dígitos.",
    example =
        "{\"id\":\"6c1f0a52-3b7e-4f0e-9a3e-2f7a1d5b8c90\",\"tipoPessoa\":\"FISICA\","
            + "\"nome\":\"Maria da Silva\",\"documento\":\"[CPF_REDACTED]\","
            + "\"email\":\"[EMAIL_REDACTED]\",\"telefone\":\"[CPF_REDACTED]\",\"ativo\":true,"
            + "\"criadoEm\":\"2026-11-18T14:30:00Z\",\"atualizadoEm\":\"2026-11-18T14:30:00Z\","
            + "\"versao\":0}")
public record ClienteResponse(
    @Schema(
            description = "Identificador único do cliente.",
            example = "6c1f0a52-3b7e-4f0e-9a3e-2f7a1d5b8c90")
        UUID id,
    @Schema(description = "Tipo da pessoa (RN-02).", example = "FISICA") TipoPessoa tipoPessoa,
    @Schema(description = "Nome do cliente.", example = "Maria da Silva") String nome,
    @Schema(description = "CPF ou CNPJ, somente dígitos (RN-02).", example = "[CPF_REDACTED]")
        String documento,
    @Schema(description = "E-mail de contato. Opcional (RN-04).", example = "[EMAIL_REDACTED]")
        String email,
    @Schema(
            description = "Telefone (10 ou 11 dígitos). Opcional (RN-04).",
            example = "[CPF_REDACTED]")
        String telefone,
    @Schema(
            description =
                "Situação: true (ativo) ou false (inativo). Inativos não aparecem na listagem padrão (RN-07).",
            example = "true")
        boolean ativo,
    @Schema(description = "Data de criação (UTC).", example = "2026-11-18T14:30:00Z")
        Instant criadoEm,
    @Schema(description = "Data da última atualização (UTC).", example = "2026-11-18T14:30:00Z")
        Instant atualizadoEm,
    @Schema(description = "Versão para controle otimista (RNF-BE-28).", example = "0")
        long versao) {

  public static ClienteResponse from(ClienteService.ClienteData data) {
    return new ClienteResponse(
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
