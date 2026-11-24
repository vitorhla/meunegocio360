// ============================================================
// useBuscaFornecedores — busca de fornecedores (RNF-FE-27)
// Somente ATIVOS, até 10 resultados, debounce de 400 ms.
// Mesma lógica de busca usada pelo FornecedorSelect e pela
// lista de fornecedores (RF-FE-004, seção "Seletor").
// ============================================================
import { useQuery } from '@tanstack/react-query';
import { listarFornecedores } from '../api/fornecedoresApi.js';
import useDebounce from '../../../shared/lib/useDebounce.js';

export const QUERY_KEY_BUSCA_FORNECEDORES = ['fornecedores', 'busca'];
const DEBOUNCE_MS = 400; // RNF-FE-27
const TAMANHO_BUSCA = 10;

/**
 * Busca até 10 fornecedores ATIVOS por nome ou documento.
 * @param {string} texto
 * @returns {Promise<object[]>}
 */
export function buscarFornecedoresAtivos(texto) {
  return listarFornecedores({
    q: texto,
    situacao: 'ATIVO',
    size: TAMANHO_BUSCA,
  }).then((pagina) => pagina.content);
}

/**
 * Hook de busca de fornecedores com debounce.
 * Desabilitado abaixo de 2 caracteres.
 *
 * @param {string} q
 */
export function useBuscaFornecedores(q = '') {
  const texto = useDebounce((q || '').trim(), DEBOUNCE_MS);
  return useQuery({
    queryKey: [...QUERY_KEY_BUSCA_FORNECEDORES, texto],
    queryFn: () => buscarFornecedoresAtivos(texto),
    enabled: texto.length >= 2,
    retry: false,
  });
}
