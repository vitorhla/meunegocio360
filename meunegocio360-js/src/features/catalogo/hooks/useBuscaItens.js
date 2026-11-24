// ============================================================
// useBuscaItens — busca de itens do catálogo (RNF-FE-27)
// Somente ATIVOS, até 10 resultados, debounce de 400 ms,
// a partir de 2 caracteres. O filtro opcional por tipo é
// repassado à API (RN-08: estoque consulta com PRODUTO).
// ============================================================
import { useQuery } from '@tanstack/react-query';
import { listarItens } from '../api/catalogoApi.js';
import useDebounce from '../../../shared/lib/useDebounce.js';

export const QUERY_KEY_BUSCA_ITENS = ['itens-catalogo', 'busca'];
const DEBOUNCE_MS = 400; // RNF-FE-27
const TAMANHO_BUSCA = 10;

/**
 * Busca até 10 itens ATIVOS por nome ou código.
 * @param {string} texto
 * @param {string} [tipo] — PRODUTO | SERVICO (opcional)
 * @returns {Promise<object[]>}
 */
export function buscarItensAtivos(texto, tipo) {
  return listarItens({
    q: texto,
    situacao: 'ATIVO',
    size: TAMANHO_BUSCA,
    tipo,
  }).then((pagina) => pagina.content);
}

/**
 * Hook de busca de itens com debounce.
 * Desabilitado abaixo de 2 caracteres.
 *
 * @param {string} q
 * @param {string} [tipo] — PRODUTO | SERVICO (opcional)
 */
export function useBuscaItens(q = '', tipo) {
  const texto = useDebounce((q || '').trim(), DEBOUNCE_MS);
  return useQuery({
    queryKey: [...QUERY_KEY_BUSCA_ITENS, tipo || '', texto],
    queryFn: () => buscarItensAtivos(texto, tipo),
    enabled: texto.length >= 2,
    retry: false,
  });
}
