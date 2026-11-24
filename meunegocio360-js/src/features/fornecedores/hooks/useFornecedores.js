// ============================================================
// useFornecedores — listagem paginada de fornecedores
// (RNF-FE-04, RNF-054: paginação no servidor)
// ============================================================
import { useQuery } from '@tanstack/react-query';
import { listarFornecedores } from '../api/fornecedoresApi.js';

/** Tamanho padrão de página (RNF-BE-13). */
export const TAMANHO_PAGINA = 20;

/**
 * Lista fornecedores com busca, filtro de situação e página.
 *
 * @param {object}   params
 * @param {string}   [params.q]        — nome ou documento
 * @param {string}   [params.situacao] — ATIVO | INATIVO | TODOS
 * @param {number}   [params.page]     — a partir de 0
 * @param {number}   [params.size]
 */
export function useFornecedores({ q, situacao, page, size = TAMANHO_PAGINA } = {}) {
  return useQuery({
    queryKey: [
      'fornecedores',
      { q: q || '', situacao: situacao || 'ATIVO', page, size },
    ],
    queryFn: () => listarFornecedores({ q, situacao, page, size }),
    retry: false,
  });
}
