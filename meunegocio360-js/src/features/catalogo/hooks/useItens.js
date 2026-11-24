// ============================================================
// useItens — listagem paginada do catálogo
// (RNF-FE-04, RNF-054: paginação e filtros no servidor)
// ============================================================
import { useQuery } from '@tanstack/react-query';
import { listarItens } from '../api/catalogoApi.js';

/** Tamanho padrão de página (RNF-BE-13). */
export const TAMANHO_PAGINA = 20;

/**
 * Lista itens do catálogo com busca e filtros combinados
 * (tipo, categoria, situação) e página.
 *
 * @param {object}   params
 * @param {string}   [params.q]          — nome ou código
 * @param {string}   [params.tipo]       — PRODUTO | SERVICO
 * @param {string}   [params.categoriaId]
 * @param {string}   [params.situacao]   — ATIVO | INATIVO | TODOS
 * @param {number}   [params.page]       — a partir de 0
 * @param {number}   [params.size]
 */
export function useItens({ q, tipo, categoriaId, situacao, page, size = TAMANHO_PAGINA } = {}) {
  return useQuery({
    queryKey: [
      'itens-catalogo',
      {
        q: q || '',
        tipo: tipo || '',
        categoriaId: categoriaId || '',
        situacao: situacao || 'ATIVO',
        page,
        size,
      },
    ],
    queryFn: () =>
      listarItens({ q, tipo, categoriaId, situacao, page, size }),
    retry: false,
  });
}
