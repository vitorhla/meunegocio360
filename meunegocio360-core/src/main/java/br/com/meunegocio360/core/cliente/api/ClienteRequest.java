package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.shared.domain.TipoPessoa;
import br.com.meunegocio360.core.shared.validation.Documento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@ClienteDocumentoValido
@Schema(
    description =
        "Dados do cliente para cadastro e edição (RF-BE-003). O documento (CPF ou CNPJ) pode"
            + " vir com a máscara; ele é armazenado somente com dígitos (RN-02). Telefone e e-mail"
            + " são opcionais (RN-04).",
    example =
        "{\"tipoPessoa\":\"FISICA\",\"nome\":\"Maria da Silva\",\"documento\":[CPF_REDACTED],"
            + "\"email\":\"[EMAIL_REDACTED]\",\"telefone\":\"[CPF_REDACTED]\"}")
public record ClienteRequest(
    @Schema(
            description =
                "Tipo da pessoa: FISICA (CPF, 11 dígitos) ou JURIDICA (CNPJ, 14 dígitos) (RN-02).",
            example = "FISICA")
        @NotNull(message = "é obrigatório")
        TipoPessoa tipoPessoa,
    @Schema(
            description = "Nome do cliente. Obrigatório, 2 a 150 caracteres (RN-04).",
            example = "Maria da Silva")
        @NotBlank(message = "é obrigatório")
        @Size(min = 2, max = 150, message = "deve ter entre 2 e 150 caracteres")
        String nome,
    @Schema(
            description =
                "CPF (11 dígitos) ou CNPJ (14 dígitos), com ou sem máscara, validado pelos"
                    + " dígitos verificadores (RN-02, RNF-BE-27). Obrigatório; o tamanho deve ser"
                    + " compatível com o tipoPessoa e é único por empresa, ativos e inativos"
                    + " (RN-03).",
            example = "[CPF_REDACTED]")
        @NotNull(message = "é obrigatório")
        @Documento
        String documento,
    @Schema(
            description = "E-mail de contato. Opcional, até 160 caracteres (RN-04).",
            example = "[EMAIL_REDACTED]")
        @Email(message = "deve ser um e-mail válido")
        @Size(max = 160, message = "deve ter no máximo 160 caracteres")
        String email,
    @Schema(
            description = "Telefone com 10 ou 11 dígitos (sem formatação). Opcional (RN-04).",
            example = "[CPF_REDACTED]")
        @Pattern(regexp = "\\d{10}|\\d{11}", message = "deve conter 10 ou 11 dígitos")
        String telefone,
    @Schema(
            description =
                "Versão atual do cliente (controle otimista, RNF-BE-28). Obrigatória na edição"
                    + " (PUT); ignorada no cadastro (POST). Ausente na edição, a API responde 409"
                    + " VERSION_CONFLICT.",
            example = "0")
        Long versao) {}
