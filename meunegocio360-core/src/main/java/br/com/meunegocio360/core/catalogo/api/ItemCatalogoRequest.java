package br.com.meunegocio360.core.catalogo.api;

import br.com.meunegocio360.core.catalogo.domain.TipoItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(
    description =
        "Dados do item de catálogo para cadastro e edição (RF-BE-005). Produtos (PRODUTO) e"
            + " serviços (SERVICO) coexistem no mesmo catálogo (RN-02); o tipo é imutável após a"
            + " criação. O preço é decimal em string com até 2 casas e não negativo (RN-03,"
            + " RNF-033). A categoria é opcional (RN-04).",
    example =
        "{\"tipo\":\"PRODUTO\",\"codigo\":\"MSE-01\",\"nome\":\"Mouse sem fio\",\"descricao\":"
            + "\"Mouse óptico 1200 dpi\",\"unidade\":\"UN\",\"preco\":\"89.90\",\"categoriaId\":"
            + "\"2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3\"}")
public record ItemCatalogoRequest(
    @Schema(
            description =
                "Tipo do item: PRODUTO (físico) ou SERVICO (RN-02). Na edição (PUT) deve ser"
                    + " igual ao tipo atual — caso contrário, 422 ITEM_TYPE_IMMUTABLE.",
            example = "PRODUTO")
        @NotNull(message = "é obrigatório")
        TipoItem tipo,
    @Schema(
            description =
                "Código do item. Opcional, até 40 caracteres; único por empresa quando"
                    + " informado (RN-04).",
            example = "MSE-01")
        @Size(max = 40, message = "deve ter no máximo 40 caracteres")
        String codigo,
    @Schema(
            description = "Nome do item. Obrigatório, 2 a 150 caracteres (RN-04).",
            example = "Mouse sem fio")
        @NotBlank(message = "é obrigatório")
        @Size(min = 2, max = 150, message = "deve ter entre 2 e 150 caracteres")
        String nome,
    @Schema(
            description = "Descrição do item. Opcional, até 500 caracteres (RN-04).",
            example = "Mouse óptico 1200 dpi")
        @Size(max = 500, message = "deve ter no máximo 500 caracteres")
        String descricao,
    @Schema(
            description =
                "Unidade de medida: 1 a 6 caracteres maiúsculos (RN-04). Opcional; padrão UN"
                    + " para PRODUTO e H para SERVICO.",
            example = "UN")
        @Pattern(regexp = "[A-Z]{1,6}", message = "deve ter de 1 a 6 caracteres maiúsculos")
        String unidade,
    @Schema(
            description =
                "Preço de venda: número não negativo em string com até 2 casas decimais (RN-03,"
                    + " RNF-033). Obrigatório; 0 é permitido. Negativo ou com mais de 2 casas"
                    + " retorna 400.",
            example = "89.90")
        @NotBlank(message = "é obrigatório")
        @Pattern(
            regexp = "^\\d{1,10}(\\.\\d{1,2})?$",
            message = "deve ser um número não negativo com no máximo 2 casas decimais (ex.: 89.90)")
        String preco,
    @Schema(
            description =
                "Id da categoria (RN-04/05). Opcional; quando informada, deve existir na"
                    + " empresa ativa e estar ativa — caso contrário, 422 CATEGORY_INVALID.",
            example = "2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3")
        UUID categoriaId,
    @Schema(
            description =
                "Versão atual do item (controle otimista, RNF-BE-28). Obrigatória na edição"
                    + " (PUT); ignorada no cadastro (POST). Ausente na edição, a API responde 409"
                    + " VERSION_CONFLICT.",
            example = "0")
        Long versao) {}
