// ============================================================
// pessoaSchema — schema Zod de pessoa (física ou jurídica)
// (RNF-FE-20)
// Usado pelo cadastro/edição de clientes (RF-FE-003) e
// fornecedores (RF-FE-004). Documento e telefone validam
// SOMENTE DÍGITOS (RNF-FE-28).
// ============================================================
import { z } from 'zod';
import {
  TipoPessoa,
  isValidDocumento,
  rotuloDocumento,
} from './tipoPessoa.js';
import { isValidTelefone } from './telefone.js';

export const pessoaSchema = z
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

export default pessoaSchema;
