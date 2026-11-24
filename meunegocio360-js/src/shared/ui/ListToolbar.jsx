// ============================================================
// ListToolbar — barra de busca com debounce de 400 ms
// (RNF-FE-27) + filtros de listagem (passados como children)
// ============================================================
import { useEffect, useState } from 'react';
import { Form } from 'react-bootstrap';

const DEBOUNCE_MS = 400; // RNF-FE-27

/**
 * Campo de busca (com *label* visível) e controles de filtro
 * dispostos em linha. O texto só é propagado após 400 ms sem
 * digitação, evitando uma requisição por tecla.
 *
 * @param {object}   props
 * @param {string}   props.searchInputId — id do input de busca
 * @param {string}   [props.searchLabel='Busca']
 * @param {string}   [props.searchPlaceholder]
 * @param {string}   props.searchValue   — valor atual (vem da URL)
 * @param {function} props.onSearchChange— (texto: string) => void
 * @param {React.ReactNode} [props.children] — grupos de filtro
 */
export default function ListToolbar({
  searchInputId,
  searchLabel = 'Busca',
  searchPlaceholder,
  searchValue = '',
  onSearchChange,
  children,
}) {
  const [input, setInput] = useState(searchValue);
  const [prevSearchValue, setPrevSearchValue] = useState(searchValue);

  // Sincroniza quando o valor externo muda (ex.: "Limpar filtros") —
  // ajuste de estado durante o render, sem efeito
  if (searchValue !== prevSearchValue) {
    setPrevSearchValue(searchValue);
    setInput(searchValue);
  }

  // Debounce: propaga o texto 400 ms após a última tecla
  useEffect(() => {
    if (input === searchValue) return undefined;
    const timer = setTimeout(() => onSearchChange?.(input), DEBOUNCE_MS);
    return () => clearTimeout(timer);
  }, [input, searchValue, onSearchChange]);

  return (
    <div className="d-flex flex-wrap align-items-end gap-2 mb-3" role="search">
      <div className="list-toolbar-search flex-grow-1">
        <Form.Group controlId={searchInputId} className="mb-0">
          <Form.Label className="small">{searchLabel}</Form.Label>
          <Form.Control
            type="search"
            placeholder={searchPlaceholder}
            value={input}
            onChange={(e) => setInput(e.target.value)}
          />
        </Form.Group>
      </div>
      {children}
    </div>
  );
}
