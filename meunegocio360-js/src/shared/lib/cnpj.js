// ============================================================
// Utilitários de documentos (CNPJ e CPF) — máscara e validação
// (RNF-FE-28)
// ============================================================

/**
 * Remove caracteres não-dígitos, mantendo apenas 0-9.
 * @param {string} v
 * @returns {string}
 */
export function somenteDigitos(v) {
  return (v || '').replace(/\D/g, '');
}

/**
 * Aplica máscara de CNPJ progressiva: 00.000.000/0000-00
 * @param {string} d — apenas dígitos
 * @returns {string}
 */
export function maskCnpj(d) {
  const digts = somenteDigitos(d).slice(0, 14);
  if (digts.length <= 2) return digts;
  if (digts.length <= 5) return `${digts.slice(0, 2)}.${digts.slice(2)}`;
  if (digts.length <= 8) return `${digts.slice(0, 2)}.${digts.slice(2, 5)}.${digts.slice(5)}`;
  if (digts.length <= 12) return `${digts.slice(0, 2)}.${digts.slice(2, 5)}.${digts.slice(5, 8)}/${digts.slice(8)}`;
  return `${digts.slice(0, 2)}.${digts.slice(2, 5)}.${digts.slice(5, 8)}/${digts.slice(8, 12)}-${digts.slice(12)}`;
}

/**
 * Calcula o dígito verificador do CNPJ para uma base de 12 ou 13 dígitos.
 * @param {string} base — dígitos da base
 * @param {number[]} pesos — pesos para o cálculo
 * @returns {number} dígito verificador (0-9)
 */
function calcularDigito(base, pesos) {
  const soma = base.split('').reduce((acc, c, i) => acc + parseInt(c, 10) * pesos[i], 0);
  const resto = soma % 11;
  return resto < 2 ? 0 : 11 - resto;
}

/**
 * Valida um CNPJ de 14 dígitos.
 * Rejeita sequência toda igual e dígitos verificadores inválidos.
 * @param {string} d14 — exatamente 14 dígitos
 * @returns {boolean}
 */
export function isValidCnpj(d14) {
  if (!d14 || d14.length !== 14) return false;

  // Rejeita sequência toda igual (11.111.111/1111-11 etc.)
  if (/^(\d)\1{13}$/.test(d14)) return false;

  const base1 = d14.slice(0, 12);
  const dig1 = parseInt(d14[12], 10);
  const dig2 = parseInt(d14[13], 10);

  const pesos1 = [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];
  const pesos2 = [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2];

  if (calcularDigito(base1, pesos1) !== dig1) return false;
  if (calcularDigito(d14.slice(0, 13), pesos2) !== dig2) return false;

  return true;
}

/**
 * Aplica máscara de CPF progressiva: 000.000.000-00
 * @param {string} d — apenas dígitos
 * @returns {string}
 */
export function maskCpf(d) {
  const digts = somenteDigitos(d).slice(0, 11);
  if (digts.length <= 3) return digts;
  if (digts.length <= 6) return `${digts.slice(0, 3)}.${digts.slice(3)}`;
  if (digts.length <= 9)
    return `${digts.slice(0, 3)}.${digts.slice(3, 6)}.${digts.slice(6)}`;
  return `${digts.slice(0, 3)}.${digts.slice(3, 6)}.${digts.slice(6, 9)}-${digts.slice(9)}`;
}

/**
 * Calcula o dígito verificador do CPF para uma base de 9 ou 10 dígitos.
 * @param {string} base — dígitos da base
 * @param {number[]} pesos — pesos para o cálculo
 * @returns {number} dígito verificador (0-9)
 */
function calcularDigitoCpf(base, pesos) {
  const soma = base
    .split('')
    .reduce((acc, c, i) => acc + parseInt(c, 10) * pesos[i], 0);
  const resto = soma % 11;
  return resto < 2 ? 0 : 11 - resto;
}

/**
 * Valida um CPF de 11 dígitos.
 * Rejeita sequência toda igual e dígitos verificadores inválidos.
 * @param {string} d11 — exatamente 11 dígitos
 * @returns {boolean}
 */
export function isValidCpf(d11) {
  if (!d11 || d11.length !== 11) return false;

  // Rejeita sequência toda igual (111.111.111-11 etc.)
  if (/^(\d)\1{10}$/.test(d11)) return false;

  const pesos1 = [10, 9, 8, 7, 6, 5, 4, 3, 2];
  const pesos2 = [11, 10, 9, 8, 7, 6, 5, 4, 3, 2];

  if (calcularDigitoCpf(d11.slice(0, 9), pesos1) !== parseInt(d11[9], 10)) {
    return false;
  }
  if (calcularDigitoCpf(d11.slice(0, 10), pesos2) !== parseInt(d11[10], 10)) {
    return false;
  }

  return true;
}
