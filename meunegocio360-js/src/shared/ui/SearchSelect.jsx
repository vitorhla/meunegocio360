// ============================================================
// SearchSelect — combobox ARIA genérico com busca por texto
// Padrão WAI-ARIA: role="combobox" + listbox +
// aria-activedescendant. Teclado: setas navegam, Enter seleciona,
// Esc fecha, Tab sai do campo. Busca com debounce de 400 ms e
// a partir de 2 caracteres (RNF-FE-27). Base de ClienteSelect,
// FornecedorSelect e ItemCatalogoSelect (RF-FE-003/004/005).
// ============================================================
import { useEffect, useId, useRef, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Form, Spinner } from 'react-bootstrap';
import useDebounce from '../lib/useDebounce.js';

const DEBOUNCE_MS = 400; // RNF-FE-27
const MIN_CHARS = 2;

/**
 * Campo de seleção com sugestões por busca livre.
 *
 * @param {object}   props
 * @param {string}   props.label            — rótulo visível
 * @param {string}   [props.placeholder]
 * @param {object}   [props.value]          — item selecionado
 * @param {function} props.onChange         — (item) => void
 * @param {Array}    [props.queryKeyPrefix] — prefixo da chave do cache
 * @param {function} props.fetchSuggestions — (texto) => Promise<item[]>
 * @param {function} props.renderItem       — (item) => string (texto da opção)
 * @param {function} [props.displayValue]   — (item) => string (texto do campo)
 * @param {string}   [props.emptyMessage]
 * @param {string}   [props.searchingMessage]
 * @param {string}   [props.errorPrefix]
 * @param {string}   [props.retryLabel]
 * @param {boolean}  [props.invalid]
 * @param {boolean}  [props.disabled]
 * @param {string}   [props.id]
 */
export default function SearchSelect({
  label,
  placeholder,
  value = null,
  onChange,
  queryKeyPrefix = [],
  fetchSuggestions,
  renderItem,
  displayValue,
  emptyMessage = 'Nenhum resultado encontrado',
  searchingMessage = 'Buscando…',
  errorPrefix = 'Erro ao buscar.',
  retryLabel = 'Tentar novamente',
  invalid = false,
  disabled = false,
  id,
}) {
  const autoId = useId();
  const inputId = id || `search-select${autoId}`;
  const listboxId = `${inputId}-listbox`;

  const [query, setQuery] = useState(
    value && displayValue ? displayValue(value) : '',
  );
  const [prevValue, setPrevValue] = useState(value);
  const [open, setOpen] = useState(false);
  const [activeIndex, setActiveIndex] = useState(0);
  const listRef = useRef(null);

  // Sincroniza o texto quando o valor externo muda (ex.: reset) —
  // ajuste de estado durante o render, sem efeito
  if (value !== prevValue) {
    setPrevValue(value);
    setQuery(value && displayValue ? displayValue(value) : '');
  }

  const debouncedTexto = useDebounce(query.trim(), DEBOUNCE_MS);
  const habilitado = debouncedTexto.length >= MIN_CHARS;
  const [prevTexto, setPrevTexto] = useState(debouncedTexto);

  // Opção ativa volta ao primeiro item quando a busca muda
  if (debouncedTexto !== prevTexto) {
    setPrevTexto(debouncedTexto);
    setActiveIndex(0);
  }

  // Dados de servidor no TanStack Query (RNF-FE-04). A chave inclui o
  // texto, então resposta atrasada de busca antiga nunca sobrescreve a
  // busca atual.
  const { data, isPending, isError, refetch } = useQuery({
    queryKey: [...queryKeyPrefix, debouncedTexto],
    queryFn: () => fetchSuggestions(debouncedTexto),
    enabled: habilitado,
    retry: false,
  });

  const items = data || [];
  const loading = isPending && habilitado;

  // Mantém a opção ativa visível dentro da listagem
  useEffect(() => {
    if (!open || !listRef.current) return;
    const el = listRef.current.querySelector(
      `#${inputId}-option-${activeIndex}`,
    );
    el?.scrollIntoView({ block: 'nearest' });
  }, [activeIndex, open, inputId, items.length]);

  function selecionar(item) {
    setOpen(false);
    if (displayValue) setQuery(displayValue(item));
    onChange?.(item);
  }

  function handleInput(e) {
    const v = e.target.value;
    setQuery(v);
    setOpen(v.trim().length >= MIN_CHARS);
  }

  function handleKeyDown(e) {
    if (disabled) return;
    switch (e.key) {
      case 'ArrowDown':
        e.preventDefault();
        if (!open) setOpen(true);
        if (items.length) setActiveIndex((i) => (i + 1) % items.length);
        break;
      case 'ArrowUp':
        e.preventDefault();
        if (!open) setOpen(true);
        if (items.length)
          setActiveIndex((i) => (i - 1 + items.length) % items.length);
        break;
      case 'Enter':
        if (open && items.length && items[activeIndex]) {
          e.preventDefault();
          selecionar(items[activeIndex]);
        }
        break;
      case 'Escape':
        if (open) {
          e.preventDefault();
          setOpen(false);
        }
        break;
      case 'Tab':
        setOpen(false);
        break;
      default:
        break;
    }
  }

  return (
    <div className="search-select">
      <Form.Group controlId={inputId} className="mb-0">
        <Form.Label>{label}</Form.Label>
        <Form.Control
          role="combobox"
          aria-expanded={open}
          aria-controls={listboxId}
          aria-activedescendant={
            open && items[activeIndex] ? `${inputId}-option-${activeIndex}` : undefined
          }
          aria-autocomplete="list"
          value={query}
          onChange={handleInput}
          onKeyDown={handleKeyDown}
          onFocus={() => {
            if (habilitado && (items.length || loading || isError)) {
              setOpen(true);
            }
          }}
          placeholder={placeholder}
          disabled={disabled}
          isInvalid={invalid || undefined}
        />
        {open && (
          <ul
            ref={listRef}
            id={listboxId}
            role="listbox"
            className="search-select-listbox list-group shadow"
          >
            {loading ? (
              <li className="list-group-item d-flex align-items-center gap-2">
                <Spinner
                  animation="border"
                  size="sm"
                  role="status"
                  aria-hidden="true"
                />
                <span className="small">{searchingMessage}</span>
              </li>
            ) : isError ? (
              <li className="list-group-item small">
                {errorPrefix}{' '}
                <button
                  type="button"
                  className="btn btn-sm btn-outline-dark"
                  onClick={() => refetch()}
                >
                  {retryLabel}
                </button>
              </li>
            ) : items.length === 0 ? (
              <li className="list-group-item small text-secondary">
                {emptyMessage}
              </li>
            ) : (
              items.map((item, i) => (
                <li
                  key={item.id}
                  id={`${inputId}-option-${i}`}
                  role="option"
                  aria-selected={i === activeIndex}
                  className={`list-group-item list-group-item-action search-select-option${
                    i === activeIndex ? ' search-select-option-active' : ''
                  }`}
                  onMouseDown={(e) => {
                    e.preventDefault();
                    selecionar(item);
                  }}
                  onMouseEnter={() => setActiveIndex(i)}
                >
                  {renderItem(item)}
                </li>
              ))
            )}
          </ul>
        )}
        {/* Estados de busca anunciados a leitores de tela (RNF-FE-22) */}
        <div aria-live="polite" className="visually-hidden">
          {loading
            ? searchingMessage
            : isError
              ? `${errorPrefix} ${retryLabel}`
              : open && !loading && items.length === 0
                ? emptyMessage
                : ''}
        </div>
      </Form.Group>
    </div>
  );
}
