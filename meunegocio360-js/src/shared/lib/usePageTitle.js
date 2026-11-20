// ============================================================
// usePageTitle — hook para título da página (RNF-FE-24)
// ============================================================

import { useEffect } from 'react';

/**
 * Define document.title por rota e restaura o anterior no unmount.
 * @param {string|null} titulo — título da página (sem sufixo do app).
 *   Se null/vazio, usa apenas "MeuNegócio360".
 */
export default function usePageTitle(titulo) {
  useEffect(() => {
    const anterior = document.title;
    document.title = titulo ? `${titulo} · MeuNegócio360` : 'MeuNegócio360';

    return () => {
      document.title = anterior;
    };
  }, [titulo]);
}
