// ============================================================
// useBuscaClientes — busca de clientes (RNF-FE-27)
// Somente ATIVOS, até 10 resultados, debounce de 400 ms.
// Mesma lógica de busca usada pelo ClienteSelect e pela
// lista de clientes (RF-FE-003, seção "Seletor").
// ============================================================
import { useQuery } from '@tanstack/react-query';
import { listarClientes } from '../api/clientesApi.js';
import useDebounce from '../../../shared/lib/useDebounce.js';

export const QUERY_KEY_BUSCA_CLIENTES = ['clientes', 'busca'];
const DEBOUNCE_MS = 400; // RNF-FE-27
const TAMANHO_BUSCA = 10;

/**
 * Busca até 10 clientes ATIVOS por nome ou documento.
 * @param {string} texto
 * @returns {Promise<object[]>}
 */
export function buscarClientesAtivos(texto) {
  return listarClientes({
    q: texto,
    situacao: 'ATIVO',
    size: TAMANHO_BUSCA,
  }).then((pagina) => pagina.content);
}

/**
 * Hook de busca de clientes com debounce.
 * Desabilitado abaixo de 2 caracteres.
 *
 * @param {string} q
 */
export function useBuscaClientes(q = '') {
  const texto = useDebounce((q || '').trim(), DEBOUNCE_MS);
  return useQuery({
    queryKey: [...QUERY_KEY_BUSCA_CLIENTES, texto],
    queryFn: () => buscarClientesAtivos(texto),
    enabled: texto.length >= 2,
    retry: false,
  });
}
