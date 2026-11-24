package br.com.meunegocio360.core.cliente.api;

import br.com.meunegocio360.core.cliente.application.ClienteService;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Listagem paginada de clientes, no formato padrão de paginação (RNF-BE-13).")
public record ClientePageResponse(
    @Schema(description = "Clientes da página atual.") List<ClienteResponse> content,
    @Schema(description = "Número da página (0-based).", example = "0") int page,
    @Schema(description = "Tamanho da página (padrão 20, máximo 100).", example = "20") int size,
    @Schema(description = "Total de registros correspondentes aos filtros.", example = "1")
        long totalElements,
    @Schema(description = "Total de páginas.", example = "1") int totalPages) {

  public static ClientePageResponse from(ClienteService.ClientePageData data) {
    return new ClientePageResponse(
        data.content().stream().map(ClienteResponse::from).toList(),
        data.page(),
        data.size(),
        data.totalElements(),
        data.totalPages());
  }
}
