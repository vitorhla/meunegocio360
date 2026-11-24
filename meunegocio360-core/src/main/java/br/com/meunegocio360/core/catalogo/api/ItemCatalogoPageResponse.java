package br.com.meunegocio360.core.catalogo.api;

import br.com.meunegocio360.core.catalogo.application.ItemCatalogoService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    description =
        "Listagem paginada de itens de catálogo, no formato padrão de paginação (RNF-BE-13).")
public record ItemCatalogoPageResponse(
    @Schema(description = "Itens da página atual.") List<ItemCatalogoResponse> content,
    @Schema(description = "Número da página (0-based).", example = "0") int page,
    @Schema(description = "Tamanho da página (padrão 20, máximo 100).", example = "20") int size,
    @Schema(description = "Total de registros correspondentes aos filtros.", example = "1")
        long totalElements,
    @Schema(description = "Total de páginas.", example = "1") int totalPages) {

  public static ItemCatalogoPageResponse from(ItemCatalogoService.ItemPageData data) {
    return new ItemCatalogoPageResponse(
        data.content().stream().map(ItemCatalogoResponse::from).toList(),
        data.page(),
        data.size(),
        data.totalElements(),
        data.totalPages());
  }
}
