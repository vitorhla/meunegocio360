package br.com.meunegocio360.core.empresa.api;

import br.com.meunegocio360.core.empresa.application.EmpresaService.EmpresaData;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Empresa (RF-BE-002).")
public record EmpresaResponse(
    @Schema(
            description = "Identificador único da empresa.",
            example = "0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10")
        UUID id,
    @Schema(description = "Razão social.", example = "Comércio Exemplo Ltda") String razaoSocial,
    @Schema(description = "Nome fantasia. Opcional.", example = "Exemplo Informática")
        String nomeFantasia,
    @Schema(description = "CNPJ somente com os 14 dígitos.", example = "11222333000181")
        String cnpj,
    @Schema(description = "E-mail de contato. Opcional.", example = "contato@exemplo.com.br")
        String email,
    @Schema(description = "Telefone. Opcional.", example = "81999998888") String telefone,
    @Schema(description = "Fuso horário (identificador IANA).", example = "America/Sao_Paulo")
        String fusoHorario,
    @Schema(description = "Moeda.", example = "BRL") String moeda,
    @Schema(description = "Indica se a empresa está ativa.", example = "true") boolean ativa,
    @Schema(description = "Data de criação (UTC, ISO 8601).", example = "2026-11-18T14:30:00Z")
        Instant criadoEm,
    @Schema(
            description = "Data da última atualização (UTC, ISO 8601).",
            example = "2026-11-18T14:30:00Z")
        Instant atualizadoEm,
    @Schema(
            description = "Versão para controle otimista nas atualizações (RNF-BE-28).",
            example = "0")
        long versao) {

  public static EmpresaResponse from(EmpresaData data) {
    return new EmpresaResponse(
        data.id(),
        data.razaoSocial(),
        data.nomeFantasia(),
        data.cnpj(),
        data.email(),
        data.telefone(),
        data.fusoHorario(),
        data.moeda(),
        data.ativa(),
        data.criadoEm(),
        data.atualizadoEm(),
        data.versao());
  }
}
