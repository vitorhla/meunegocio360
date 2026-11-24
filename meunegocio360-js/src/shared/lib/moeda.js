// ============================================================
// moeda.js — formatação/conversão de moeda BRL SEM float
// (RNF-FE-28, RNF-033: preço é decimal em string)
// ============================================================

const MAX_INT_DIGITOS = 10; // 9.999.999.999 (RN-03 RF-BE-005)

/**
 * Normaliza o texto digitado no campo de preço para decimal
 * em string com ponto, sem padding (ex.: "1.234,5" → "1234.5";
 * "-10,00" → "-10.00"; "89,9" → "89.9"). Mantém o sinal de
 * menos para que o schema possa rejeitar com a mensagem
 * própria. Máximo 9.999.999.999,99.
 *
 * @param {string} texto
 * @returns {string} decimal em string, ou '' se vazio
 */
export function textoParaDecimal(texto) {
  const bruto = (texto || '').trim();
  const negativo = bruto.startsWith('-');
  const limpo = bruto.replace(/[^0-9,]/g, '');
  const [int = '', frac = ''] = limpo.split(',');
  const intL = int.replace(/^0+(?=\d)/, '').slice(0, MAX_INT_DIGITOS);
  const fracL = frac.slice(0, 2);
  if (!intL && !fracL) return negativo ? '-' : '';
  const base = intL || '0';
  return `${negativo ? '-' : ''}${base}${fracL ? `.${fracL}` : ''}`;
}

/**
 * Máscara de exibição BRL a partir do decimal em string
 * (ex.: "1234.5" → "1.234,5"; "-10.00" → "-10,00").
 *
 * @param {string} decimal
 * @returns {string}
 */
export function maskMoedaBR(decimal) {
  const v = String(decimal ?? '');
  if (!v || v === '-') return v;
  const negativo = v.startsWith('-');
  const [int = '0', frac = ''] = v.slice(negativo ? 1 : 0).split('.');
  const intGroup = int.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${negativo ? '-' : ''}${intGroup}${frac ? `,${frac}` : ''}`;
}

/**
 * Normaliza o decimal do formulário para o envio à API:
 * sempre com 2 casas (ex.: "89.9" → "89.90"; "0" → "0.00").
 *
 * @param {string} decimal
 * @returns {string}
 */
export function normalizarPreco(decimal) {
  const v = String(decimal ?? '').replace(/^-/, '');
  if (!v) return '0.00';
  const [int = '0', frac = ''] = v.split('.');
  return `${int}.${frac.slice(0, 2).padEnd(2, '0')}`;
}

/**
 * Formata um decimal em string como moeda BRL exibível
 * (ex.: "1234.5" → "R$ 1.234,50"; "0" → "R$ 0,00").
 *
 * @param {string} [decimal]
 * @returns {string}
 */
export function formatarMoedaBR(decimal) {
  const v = String(decimal ?? '').replace(/^-/, '');
  if (!v) return 'R$ 0,00';
  const [int = '0', frac = ''] = v.split('.');
  const intGroup = int.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `R$ ${intGroup},${frac.slice(0, 2).padEnd(2, '0')}`;
}
