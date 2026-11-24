// ============================================================
// useClientes — listagem paginada de clientes
// (RNF-FE-04, RNF-054: paginação no servidor)
// ============================================================
import { useQuery } from '@tanstack/react-query';
import { listarClientes } from '../api/clientesApi.js';

/** Tamanho padrão de página (RNF-BE-13). */
export const TAMANHO_PAGINA = 20;

/**
 * Lista clientes com busca, filtro de situação e página.
 *
 * @param {object}   params
 * @param {string}   [params.q]        — nome ou documento
 * @param {string}   [params.situacao] — ATIVO | INATIVO | TODOS
 * @param {number}   [params.page]     — a partir de 0
 * @param {number}   [params.size]
 */
export function useClientes({ q, situacao, page, size = TAMANHO_PAGINA } = {}) {
  return useQuery({
    queryKey: [
      'clientes',
      { q: q || '', situacao: situacao || 'ATIVO', page, size },
    ],
    queryFn: () => listarClientes({ q, situacao, page, size }),
    retry: false,
  });
}
