package br.com.meunegocio360.core.catalogo.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    description =
        "Dados da categoria para cadastro e renomeio (RF-BE-005, RN-06). O nome é único por"
            + " empresa sem distinguir maiúsculas e acentos.",
    example = "{\"nome\":\"Informática\"}")
public record CategoriaRequest(
    @Schema(
            description = "Nome da categoria. Obrigatório, 2 a 80 caracteres (RN-06).",
            example = "Informática")
        @NotBlank(message = "é obrigatório")
        @Size(min = 2, max = 80, message = "deve ter entre 2 e 80 caracteres")
        String nome,
    @Schema(
            description =
                "Versão atual da categoria (controle otimista, RNF-BE-28). Obrigatória no"
                    + " renomeio (PUT); ignorada no cadastro (POST). Ausente na edição, a API"
                    + " responde 409 VERSION_CONFLICT.",
            example = "0")
        Long versao) {}
