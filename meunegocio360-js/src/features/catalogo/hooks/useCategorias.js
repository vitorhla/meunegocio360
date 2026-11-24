// ============================================================
// useCategorias — consultas de categorias (RNF-FE-04)
// A listagem de gestão traz até 100; o seletor de categorias
// ativas (formulário de item e filtro da lista) traz até 200.
// Toda mutação invalida o prefixo ['categorias'], então os
// seletores se atualizam após criar/renomear/inativar.
// ============================================================
import { useQuery } from '@tanstack/react-query';
import { listarCategorias } from '../api/catalogoApi.js';

const TAMANHO_GESTAO = 100;
const TAMANHO_SELETOR = 200;

/**
 * Lista categorias para a gestão (busca + situação).
 *
 * @param {object}   params
 * @param {string}   [params.q]
 * @param {string}   [params.situacao] — ATIVO | INATIVO | TODOS
 */
export function useCategorias({ q, situacao } = {}) {
  return useQuery({
    queryKey: ['categorias', { q: q || '', situacao: situacao || 'ATIVO' }],
    queryFn: () =>
      listarCategorias({ q, situacao, size: TAMANHO_GESTAO }),
    retry: false,
  });
}

/**
 * Categorias ativas (para o select do formulário de item e
 * para o filtro de categoria da lista do catálogo).
 *
 * @returns {import('@tanstack/react-query').UseQueryResult<object[]>}
 */
export function useCategoriasAtivas() {
  return useQuery({
    queryKey: ['categorias', 'ativas'],
    queryFn: () =>
      listarCategorias({ situacao: 'ATIVO', size: TAMANHO_SELETOR }).then(
        (pagina) => pagina.content,
      ),
    retry: false,
  });
}
