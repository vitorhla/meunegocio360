// ============================================================
// Schemas Zod do catálogo (RNF-FE-20)
// Espelham as regras do backend (RF-BE-005 RN-02 a RN-04).
// ============================================================
import { z } from 'zod';
import { TipoItem } from './lib/tipoItem.js';

const MAX_PRECO_INT = 10; // 9.999.999.999 (RN-03)

export const itemSchema = z.object({
  tipo: z
    .string()
    .min(1, 'Selecione o tipo.')
    .refine(
      (v) => v === TipoItem.PRODUTO || v === TipoItem.SERVICO,
      'Selecione o tipo.',
    ),
  codigo: z
    .string()
    .trim()
    .max(40, 'O código deve ter no máximo 40 caracteres.')
    .optional()
    .or(z.literal('')),
  nome: z
    .string()
    .trim()
    .min(2, 'Informe o nome.')
    .max(150, 'O nome deve ter no máximo 150 caracteres.'),
  descricao: z
    .string()
    .trim()
    .max(500, 'A descrição deve ter no máximo 500 caracteres.')
    .optional()
    .or(z.literal('')),
  unidade: z
    .string()
    .trim()
    .min(1, 'Informe a unidade.')
    .max(6, 'A unidade deve ter no máximo 6 caracteres.'),
  preco: z
    .string()
    .min(1, 'Informe o preço.')
    .refine((v) => v !== '-' && !v.startsWith('-'), 'O preço não pode ser negativo.')
    .refine(
      (v) => /^\d+(\.\d{1,2})?$/.test(v),
      'Informe um preço válido.',
    )
    .refine((v) => (v.split('.')[0] || '0').length <= MAX_PRECO_INT, 'O preço deve ser no máximo 9.999.999.999,99.'),
  categoriaId: z.string().optional().or(z.literal('')),
});

export default itemSchema;
