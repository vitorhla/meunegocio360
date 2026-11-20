// ============================================================
// Utilitários de telefone — máscara e validação (RNF-FE-28)
// ============================================================

import { somenteDigitos } from './cnpj.js';

export { somenteDigitos };

/**
 * Aplica máscara de telefone progressiva:
 *   <=2: "(XX"
 *   <=6: "(XX) XXXX"
 *   >6:  "(XX) XXXXX-XXXX" (funciona para 10 e 11 dígitos)
 * @param {string} d — apenas dígitos
 * @returns {string}
 */
export function maskTelefone(d) {
  const digts = somenteDigitos(d).slice(0, 11);
  if (digts.length <= 2) return `(${digts}`;
  if (digts.length <= 6) return `(${digts.slice(0, 2)}) ${digts.slice(2)}`;
  if (digts.length <= 10) return `(${digts.slice(0, 2)}) ${digts.slice(2, 6)}-${digts.slice(6)}`;
  return `(${digts.slice(0, 2)}) ${digts.slice(2, 7)}-${digts.slice(7)}`;
}

/**
 * Valida um telefone: 10 ou 11 dígitos.
 * @param {string} d — apenas dígitos
 * @returns {boolean}
 */
export function isValidTelefone(d) {
  if (!d || !/^\d{10,11}$/.test(d)) return false;
  return true;
}
