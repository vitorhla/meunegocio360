// ============================================================
// useUrlFilters — filtros e paginação refletidos na URL
// (RNF-FE-27: busca, filtros e página na query string)
// ============================================================
import { useCallback } from 'react';
import { useSearchParams } from 'react-router';

/**
 * Guarda os filtros de uma listagem (e a página) na query string.
 * Valores iguais ao padrão NÃO aparecem na URL. Mudar um filtro
 * recomeça a paginação (página 0).
 *
 * @param {object} defaults — valores padrão dos filtros (sem `page`).
 *   DEVE ser um objeto estável (constante de módulo) para que os
 *   callbacks mantenha identidade estável.
 * @returns {{
 *   filters: object,
 *   page: number,
 *   setFilter: (name: string, value: string) => void,
 *   setPage: (page: number) => void,
 *   clearFilters: () => void,
 *   isFiltered: boolean,
 * }}
 */
export default function useUrlFilters(defaults = {}) {
  const [searchParams, setSearchParams] = useSearchParams();

  const filters = {};
  for (const [name, def] of Object.entries(defaults)) {
    filters[name] = searchParams.get(name) ?? def;
  }

  const pageRaw = parseInt(searchParams.get('page') ?? '0', 10);
  const page = Number.isNaN(pageRaw) || pageRaw < 0 ? 0 : pageRaw;

  const setFilter = useCallback(
    (name, value) => {
      setSearchParams(
        (prev) => {
          const sp = new URLSearchParams(prev);
          const def = defaults[name];
          if (value === undefined || value === '' || value === def) {
            sp.delete(name);
          } else {
            sp.set(name, value);
          }
          // Filtro novo recomeça na primeira página
          sp.delete('page');
          return sp;
        },
        { replace: true },
      );
    },
    [setSearchParams, defaults],
  );

  const setPage = useCallback(
    (p) => {
      const n = Math.max(0, p);
      setSearchParams(
        (prev) => {
          const sp = new URLSearchParams(prev);
          if (n === 0) sp.delete('page');
          else sp.set('page', String(n));
          return sp;
        },
        { replace: false },
      );
    },
    [setSearchParams],
  );

  const clearFilters = useCallback(
    () => setSearchParams(() => new URLSearchParams(), { replace: true }),
    [setSearchParams],
  );

  const isFiltered = Object.entries(defaults).some(
    ([name, def]) => (searchParams.get(name) ?? def) !== def,
  );

  return { filters, page, setFilter, setPage, clearFilters, isFiltered };
}
