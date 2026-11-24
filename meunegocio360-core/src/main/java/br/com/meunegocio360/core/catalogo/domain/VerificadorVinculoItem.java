package br.com.meunegocio360.core.catalogo.domain;

import java.util.UUID;

/**
 * Ponto de extensão que decide se um item de catálogo pode ser excluído (RF-BE-005, RN-09;
 * RNF-097).
 *
 * <p>A feature {@code catalogo} NÃO conhece os vínculos (vendas, compras): cada requisito que criar
 * um vínculo registra aqui a sua implementação como Bean, SEM alterar o código de exclusão. Para
 * vínculos em outro serviço (ex.: Suprimentos), a implementação consulta a API daquele serviço
 * (RNF-092). Nesta fase não há implementações.
 */
public interface VerificadorVinculoItem {

  /**
   * @return {@code true} se o item possui vínculo (venda ou compra) que impeça a exclusão
   */
  boolean possuiVinculo(UUID itemId);
}
