package br.com.meunegocio360.core.catalogo.api;

import br.com.meunegocio360.core.catalogo.application.CategoriaService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    description = "Listagem paginada de categorias, no formato padrão de paginação (RNF-BE-13).")
public record CategoriaPageResponse(
    @Schema(description = "Categorias da página atual.") List<CategoriaResponse> content,
    @Schema(description = "Número da página (0-based).", example = "0") int page,
    @Schema(description = "Tamanho da página (padrão 20, máximo 100).", example = "20") int size,
    @Schema(description = "Total de registros correspondentes aos filtros.", example = "1")
        long totalElements,
    @Schema(description = "Total de páginas.", example = "1") int totalPages) {

  public static CategoriaPageResponse from(CategoriaService.CategoriaPageData data) {
    return new CategoriaPageResponse(
        data.content().stream().map(CategoriaResponse::from).toList(),
        data.page(),
        data.size(),
        data.totalElements(),
        data.totalPages());
  }
}
