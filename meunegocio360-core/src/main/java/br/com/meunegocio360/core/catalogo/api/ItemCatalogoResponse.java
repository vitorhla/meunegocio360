package br.com.meunegocio360.core.catalogo.api;

import br.com.meunegocio360.core.catalogo.application.ItemCatalogoService;
import br.com.meunegocio360.core.catalogo.domain.TipoItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(
    description =
        "Item do catálogo de produtos e serviços (RF-BE-005). O preço é devolvido como string"
            + " (RNF-033); a coluna estoque_minimo existe no banco mas NENHUMA API a expõe nesta"
            + " fase (RN-07).",
    example =
        "{\"id\":\"9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44\",\"tipo\":\"PRODUTO\",\"codigo\":"
            + "\"NOTE-I5-16\",\"nome\":\"Notebook 15\\\" i5 16 GB\",\"descricao\":"
            + "\"Notebook para uso profissional\",\"unidade\":\"UN\",\"preco\":\"3499.90\","
            + "\"categoria\":{\"id\":\"2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3\","
            + "\"nome\":\"Informática\"},\"ativo\":true,\"criadoEm\":\"2026-11-18T14:30:00Z\","
            + "\"atualizadoEm\":\"2026-11-18T14:30:00Z\",\"versao\":0}")
public record ItemCatalogoResponse(
    @Schema(
            description = "Identificador único do item.",
            example = "9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44")
        UUID id,
    @Schema(description = "Tipo do item (RN-02).", example = "PRODUTO") TipoItem tipo,
    @Schema(description = "Código do item. Opcional (RN-04).", example = "NOTE-I5-16")
        String codigo,
    @Schema(description = "Nome do item (RN-04).", example = "Notebook 15\" i5 16 GB") String nome,
    @Schema(
            description = "Descrição do item. Opcional (RN-04).",
            example = "Notebook para uso profissional")
        String descricao,
    @Schema(description = "Unidade de medida (RN-04).", example = "UN") String unidade,
    @Schema(
            description = "Preço de venda como string, com 2 casas decimais (RN-03, RNF-033).",
            example = "3499.90")
        String preco,
    @Schema(
            description =
                "Categoria do item, quando associada (RN-04); null para item sem categoria.",
            example = "{\"id\":\"2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3\",\"nome\":\"Informática\"}")
        CategoriaRef categoria,
    @Schema(
            description =
                "Situação: true (ativo) ou false (inativo). Inativos não aparecem na listagem"
                    + " padrão.",
            example = "true")
        boolean ativo,
    @Schema(description = "Data de criação (UTC).", example = "2026-11-18T14:30:00Z")
        Instant criadoEm,
    @Schema(description = "Data da última atualização (UTC).", example = "2026-11-18T14:30:00Z")
        Instant atualizadoEm,
    @Schema(description = "Versão para controle otimista (RNF-BE-28).", example = "0")
        long versao) {

  @Schema(
      description = "Referência da categoria associada ao item.",
      example = "{\"id\":\"2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3\",\"nome\":\"Informática\"}")
  public record CategoriaRef(
      @Schema(description = "Id da categoria.", example = "2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3")
          UUID id,
      @Schema(description = "Nome da categoria.", example = "Informática") String nome) {}

  public static ItemCatalogoResponse from(ItemCatalogoService.ItemData data) {
    CategoriaRef categoria =
        data.categoriaId() == null
            ? null
            : new CategoriaRef(data.categoriaId(), data.categoriaNome());
    return new ItemCatalogoResponse(
        data.id(),
        data.tipo(),
        data.codigo(),
        data.nome(),
        data.descricao(),
        data.unidade(),
        data.preco().setScale(2).toPlainString(),
        categoria,
        data.ativo(),
        data.criadoEm(),
        data.atualizadoEm(),
        data.versao());
  }
}
