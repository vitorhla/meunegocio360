package br.com.meunegocio360.core.catalogo.domain;

/**
 * Tipo do item de catálogo (RF-BE-005, RN-02). {@code PRODUTO} é item físico (padrão de unidade
 * {@code UN}); {@code SERVICO} não possui estoque (padrão de unidade {@code H}). O tipo é imutável
 * após a criação (RN-02).
 */
public enum TipoItem {
  PRODUTO,
  SERVICO
}
