package br.com.meunegocio360.core.fornecedor.api;

import br.com.meunegocio360.core.shared.domain.TipoPessoa;
import br.com.meunegocio360.core.shared.validation.Documento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@FornecedorDocumentoValido
@Schema(
    description =
        "Dados do fornecedor para cadastro e edição (RF-BE-004). O documento (CPF ou CNPJ) pode"
            + " vir com a máscara; ele é armazenado somente com dígitos (RF-BE-003 RN-02). Telefone"
            + " e e-mail são opcionais (RF-BE-003 RN-04).",
    example =
        "{\"tipoPessoa\":\"JURIDICA\",\"nome\":\"Distribuidora Alfa Ltda\",\"documento\":"
            + "\"11.222.333/0001-81\",\"email\":\"[EMAIL_REDACTED]\",\"telefone\":\"1134567890\"}")
public record FornecedorRequest(
    @Schema(
            description =
                "Tipo da pessoa: FISICA (CPF, 11 dígitos) ou JURIDICA (CNPJ, 14 dígitos) (RN-02).",
            example = "JURIDICA")
        @NotNull(message = "é obrigatório")
        TipoPessoa tipoPessoa,
    @Schema(
            description = "Nome do fornecedor. Obrigatório, 2 a 150 caracteres (RF-BE-003 RN-04).",
            example = "Distribuidora Alfa Ltda")
        @NotBlank(message = "é obrigatório")
        @Size(min = 2, max = 150, message = "deve ter entre 2 e 150 caracteres")
        String nome,
    @Schema(
            description =
                "CPF (11 dígitos) ou CNPJ (14 dígitos), com ou sem máscara, validado pelos"
                    + " dígitos verificadores (RF-BE-003 RN-02, RNF-BE-27). Obrigatório; o tamanho"
                    + " deve ser compatível com o tipoPessoa e é único por empresa, ativos e"
                    + " inativos (RF-BE-004 RN-02).",
            example = "11.222.333/0001-81")
        @NotNull(message = "é obrigatório")
        @Documento
        String documento,
    @Schema(
            description = "E-mail de contato. Opcional, até 160 caracteres.",
            example = "[EMAIL_REDACTED]")
        @Email(message = "deve ser um e-mail válido")
        @Size(max = 160, message = "deve ter no máximo 160 caracteres")
        String email,
    @Schema(
            description = "Telefone com 10 ou 11 dígitos (sem formatação). Opcional.",
            example = "1134567890")
        @Pattern(regexp = "\\d{10}|\\d{11}", message = "deve conter 10 ou 11 dígitos")
        String telefone,
    @Schema(
            description =
                "Versão atual do fornecedor (controle otimista, RNF-BE-28). Obrigatória na edição"
                    + " (PUT); ignorada no cadastro (POST). Ausente na edição, a API responde 409"
                    + " VERSION_CONFLICT.",
            example = "0")
        Long versao) {}
