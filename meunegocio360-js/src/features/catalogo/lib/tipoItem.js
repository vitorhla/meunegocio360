// ============================================================
// tipoItem — constantes de tipo do item de catálogo (RF-FE-005)
// ============================================================

export const TipoItem = Object.freeze({
  PRODUTO: 'PRODUTO',
  SERVICO: 'SERVICO',
});

/**
 * Rótulo visível do tipo (RNF-060: o selo usa texto, não só cor).
 * @param {string} tipo — PRODUTO | SERVICO
 * @returns {string}
 */
export function rotuloTipoItem(tipo) {
  return tipo === TipoItem.SERVICO ? 'Serviço' : 'Produto';
}

/** Unidade padrão por tipo (RN-04 RF-BE-005). */
export function unidadePadrao(tipo) {
  return tipo === TipoItem.SERVICO ? 'H' : 'UN';
}
