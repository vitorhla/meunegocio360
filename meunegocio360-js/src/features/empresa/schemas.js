// ============================================================
// Schema Zod da empresa (RNF-FE-20)
// Validação do lado do cliente espelhando o backend.
// Campos cnpj/telefone validam DÍGITOS (RNF-FE-28).
// ============================================================
import { z } from 'zod';
import { isValidCnpj } from '../../shared/lib/cnpj.js';
import { isValidTelefone } from '../../shared/lib/telefone.js';

/**
 * Schema de validação do formulário de cadastro/edição da empresa.
 * CNPJ e telefone são validados com apenas dígitos.
 */
export const empresaSchema = z.object({
  razaoSocial: z
    .string()
    .trim()
    .min(3, 'Informe a razão social.')
    .max(150, 'A razão social deve ter no máximo 150 caracteres.'),
  nomeFantasia: z
    .string()
    .trim()
    .max(150, 'O nome fantasia deve ter no máximo 150 caracteres.')
    .optional()
    .or(z.literal('')),
  cnpj: z
    .string()
    .min(1, 'Informe o CNPJ.')
    .refine((v) => v.length === 14, 'Informe o CNPJ.')
    .refine(isValidCnpj, 'CNPJ inválido.'),
  email: z
    .string()
    .trim()
    .max(160, 'O e-mail deve ter no máximo 160 caracteres.')
    .email('Informe um e-mail válido.')
    .optional()
    .or(z.literal('')),
  telefone: z
    .string()
    .optional()
    .or(z.literal(''))
    .refine(
      (v) => !v || isValidTelefone(v),
      'Informe um telefone válido com DDD.',
    ),
  fusoHorario: z
    .string()
    .min(1, 'Selecione o fuso horário.'),
  moeda: z
    .literal('BRL'),
});

export default empresaSchema;
