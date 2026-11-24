package br.com.meunegocio360.core.cliente.domain;

import java.util.UUID;

/**
 * Ponto de extensão que decide se um cliente pode ser excluído (RF-BE-003, RN-05 e RN-06; RNF-097).
 *
 * <p>A feature {@code cliente} NÃO conhece os vínculos (vendas, ordens de serviço): cada requisito
 * que criar um vínculo registra aqui a sua implementação como Bean, SEM alterar o código de
 * exclusão. Para vínculos em outro serviço (ex.: OS no serviço Serviços), a implementação consulta
 * a API daquele serviço (RNF-092). Nesta fase não há implementações.
 */
public interface VerificadorVinculoCliente {

  /**
   * @return {@code true} se o cliente possui vínculo (venda ou OS) que impeça a exclusão
   */
  boolean possuiVinculo(UUID clienteId);
}
