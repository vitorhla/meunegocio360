package br.com.meunegocio360.core.fornecedor.api;

import br.com.meunegocio360.core.fornecedor.application.FornecedorService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    description = "Listagem paginada de fornecedores, no formato padrão de paginação (RNF-BE-13).")
public record FornecedorPageResponse(
    @Schema(description = "Fornecedores da página atual.") List<FornecedorResponse> content,
    @Schema(description = "Número da página (0-based).", example = "0") int page,
    @Schema(description = "Tamanho da página (padrão 20, máximo 100).", example = "20") int size,
    @Schema(description = "Total de registros correspondentes aos filtros.", example = "1")
        long totalElements,
    @Schema(description = "Total de páginas.", example = "1") int totalPages) {

  public static FornecedorPageResponse from(FornecedorService.FornecedorPageData data) {
    return new FornecedorPageResponse(
        data.content().stream().map(FornecedorResponse::from).toList(),
        data.page(),
        data.size(),
        data.totalElements(),
        data.totalPages());
  }
}
