package br.com.meunegocio360.core.empresa.api;

import br.com.meunegocio360.core.shared.validation.Cnpj;
import br.com.meunegocio360.core.shared.validation.FusoHorario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(
    description =
        "Dados da empresa para cadastro e edição (RF-BE-002). CNPJ e telefone somente com"
            + " dígitos — a máscara é responsabilidade do frontend (RNF-BE-27).",
    example =
        "{\"razaoSocial\":\"Comércio Exemplo Ltda\",\"nomeFantasia\":\"Exemplo Informática\","
            + "\"cnpj\":\"11222333000181\",\"email\":\"contato@exemplo.com.br\","
            + "\"telefone\":\"81999998888\",\"fusoHorario\":\"America/Sao_Paulo\","
            + "\"moeda\":\"BRL\"}")
public record EmpresaRequest(
    @Schema(
            description = "Razão social. Obrigatório, 3 a 150 caracteres.",
            example = "Comércio Exemplo Ltda")
        @NotBlank(message = "é obrigatório")
        @Size(min = 3, max = 150, message = "deve ter entre 3 e 150 caracteres")
        String razaoSocial,
    @Schema(
            description = "Nome fantasia. Opcional, até 150 caracteres.",
            example = "Exemplo Informática")
        @Size(max = 150, message = "deve ter no máximo 150 caracteres")
        String nomeFantasia,
    @Schema(
            description =
                "CNPJ com os 14 dígitos (sem pontuação), validado pelos dígitos verificadores."
                    + " Obrigatório e único entre todas as empresas (RN-01).",
            example = "11222333000181")
        @Cnpj
        String cnpj,
    @Schema(
            description = "E-mail de contato. Opcional, até 160 caracteres.",
            example = "contato@exemplo.com.br")
        @Email(message = "deve ser um e-mail válido")
        @Size(max = 160, message = "deve ter no máximo 160 caracteres")
        String email,
    @Schema(
            description = "Telefone com 10 ou 11 dígitos (sem formatação). Opcional.",
            example = "81999998888")
        @Pattern(regexp = "\\d{10}|\\d{11}", message = "deve conter 10 ou 11 dígitos")
        String telefone,
    @Schema(
            description =
                "Fuso horário (identificador IANA válido). Opcional; padrão"
                    + " America/Sao_Paulo (RN-05).",
            example = "America/Sao_Paulo")
        @Size(max = 50, message = "deve ter no máximo 50 caracteres")
        @FusoHorario
        String fusoHorario,
    @Schema(
            description = "Moeda. Nesta fase somente BRL (RN-05). Opcional; padrão BRL.",
            example = "BRL")
        @Pattern(regexp = "BRL", message = "nesta fase somente BRL é aceito")
        String moeda,
    @Schema(
            description =
                "Versão atual da empresa (controle otimista, RNF-BE-28). Obrigatória na edição"
                    + " (PUT); ignorada no cadastro (POST). Ausente na edição, a API responde 409"
                    + " VERSION_CONFLICT.",
            example = "0")
        Long versao) {}
