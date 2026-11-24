package br.com.meunegocio360.core.fornecedor.domain;

import java.util.UUID;

/**
 * Ponto de extensão que decide se um fornecedor pode ser excluído (RF-BE-004, RN-03 e RN-04;
 * RNF-097).
 *
 * <p>A feature {@code fornecedor} NÃO conhece os vínculos (pedidos de compra, contas a pagar): cada
 * requisito que criar um vínculo registra aqui a sua implementação como Bean, SEM alterar o código
 * de exclusão. Para vínculos em outro serviço (ex.: compras no serviço Suprimentos), a
 * implementação consulta a API daquele serviço (RNF-092). Nesta fase não há implementações.
 */
public interface VerificadorVinculoFornecedor {

  /**
   * @return {@code true} se o fornecedor possui vínculo (pedido de compra ou conta a pagar) que
   *     impeça a exclusão
   */
  boolean possuiVinculo(UUID fornecedorId);
}
