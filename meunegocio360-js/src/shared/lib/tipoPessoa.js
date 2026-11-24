// ============================================================
// Tipo de pessoa — constante centralizada e helpers de documento
// (RN-02 RF-BE-003/004). Perfis/tipos NUNCA como texto solto
// nos componentes.
// ============================================================
import {
  maskCnpj,
  maskCpf,
  isValidCnpj,
  isValidCpf,
} from './cnpj.js';

export const TipoPessoa = {
  FISICA: 'FISICA',
  JURIDICA: 'JURIDICA',
};

/** Máximo de dígitos do documento para o tipo (RN-02). */
export function maxDigitosDocumento(tipo) {
  return tipo === TipoPessoa.JURIDICA ? 14 : 11;
}

/** Máscara de CPF (física) ou CNPJ (jurídica). */
export function maskDocumento(tipo, d) {
  return tipo === TipoPessoa.JURIDICA ? maskCnpj(d) : maskCpf(d);
}

/** Validação pelos dígitos verificadores, conforme o tipo (RN-02). */
export function isValidDocumento(tipo, d) {
  return tipo === TipoPessoa.JURIDICA ? isValidCnpj(d) : isValidCpf(d);
}

/** Rótulo do documento: "CPF" ou "CNPJ". */
export function rotuloDocumento(tipo) {
  return tipo === TipoPessoa.JURIDICA ? 'CNPJ' : 'CPF';
}

/** Rótulo do tipo de pessoa. */
export function rotuloTipoPessoa(tipo) {
  return tipo === TipoPessoa.JURIDICA ? 'Pessoa jurídica' : 'Pessoa física';
}
