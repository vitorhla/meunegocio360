// ============================================================
// Schemas Zod para autenticação (RNF-FE-20)
// ============================================================
import { z } from 'zod';

/**
 * Schema de validação do formulário de login.
 * Regras espelhadas da validação do servidor (RNF-020).
 */
export const loginSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, 'Informe seu e-mail.')
    .max(160, 'O e-mail deve ter no máximo 160 caracteres.')
    .email('Informe um e-mail válido.'),
  senha: z
    .string()
    .min(1, 'Informe sua senha.')
    .min(8, 'A senha deve ter entre 8 e 72 caracteres.')
    .max(72, 'A senha deve ter entre 8 e 72 caracteres.'),
});

export default loginSchema;
