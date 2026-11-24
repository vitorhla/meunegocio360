// ============================================================
// Schema Zod do cliente (RNF-FE-20)
// Validação do lado do cliente espelhando o backend
// (RF-BE-003 RN-02 e RN-04). Documento e telefone validam
// SOMENTE DÍGITOS (RNF-FE-28).
// ============================================================
import { z } from 'zod';
import {
  TipoPessoa,
  isValidDocumento,
  rotuloDocumento,
} from '../../shared/lib/tipoPessoa.js';
import { isValidTelefone } from '../../shared/lib/telefone.js';

export const clienteSchema = z
  .object({
    tipoPessoa: z
      .string()
      .min(1, 'Selecione o tipo de pessoa.')
      .refine(
        (v) => v === TipoPessoa.FISICA || v === TipoPessoa.JURIDICA,
        'Selecione o tipo de pessoa.',
      ),
    nome: z
      .string()
      .trim()
      .min(2, 'Informe o nome.')
      .max(150, 'O nome deve ter no máximo 150 caracteres.'),
    documento: z.string().min(1, 'Informe o documento.'),
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
  })
  .superRefine((data, ctx) => {
    // O documento deve corresponder ao tipo de pessoa:
    // CPF (11) para física, CNPJ (14) para jurídica, com
    // dígitos verificadores válidos (RN-02).
    if (
      data.documento &&
      data.tipoPessoa &&
      !isValidDocumento(data.tipoPessoa, data.documento)
    ) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: ['documento'],
        message: `${rotuloDocumento(data.tipoPessoa)} inválido.`,
      });
    }
  });

export default clienteSchema;
