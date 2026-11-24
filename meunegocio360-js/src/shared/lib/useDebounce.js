// ============================================================
// useDebounce — atrasa a propagação de um valor (RNF-FE-27)
// ============================================================
import { useEffect, useState } from 'react';

/**
 * Retorna o valor apenas após `delay` ms sem mudanças.
 * Base do debounce de busca das listagens e dos seletores
 * (400 ms por padrão, RNF-FE-27).
 *
 * @param {*} value
 * @param {number} [delay=400]
 * @returns {*}
 */
export default function useDebounce(value, delay = 400) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);

  return debounced;
}
