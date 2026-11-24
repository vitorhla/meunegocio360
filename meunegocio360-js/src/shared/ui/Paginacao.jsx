// ============================================================
// Paginacao — controles de paginação no servidor (RNF-054)
// Alvos com >= 44 px via .paginacao em theme.scss (RNF-FE-23)
// ============================================================
import { Pagination } from 'react-bootstrap';

const MAX_NUMEROS_VISIVEIS = 5;

/**
 * Navegação de páginas de uma listagem paginada no servidor.
 *
 * @param {object}   props
 * @param {number}   props.page          — página atual (a partir de 0)
 * @param {number}   props.size          — itens por página
 * @param {number}   props.totalElements
 * @param {number}   props.totalPages
 * @param {function} props.onChange      — (novaPagina: number) => void
 */
export default function Paginacao({
  page,
  size,
  totalElements,
  totalPages,
  onChange,
}) {
  if (!totalPages || totalPages <= 1) return null;

  // Janela de até 5 números centrada na página atual
  let inicio = Math.min(Math.max(0, page - 2), totalPages - MAX_NUMEROS_VISIVEIS);
  let fim = inicio + MAX_NUMEROS_VISIVEIS;
  if (fim > totalPages) {
    fim = totalPages;
    inicio = Math.max(0, fim - MAX_NUMEROS_VISIVEIS);
  }
  const numeros = Array.from({ length: fim - inicio }, (_, i) => inicio + i);

  const primeiroItem = totalElements === 0 ? 0 : page * size + 1;
  const ultimoItem = Math.min((page + 1) * size, totalElements);

  return (
    <nav aria-label="Paginação" className="paginacao mt-2">
      <div className="d-flex flex-wrap align-items-center justify-content-between gap-2">
        <p className="small text-secondary mb-0" aria-live="polite">
          {totalElements === 0
            ? '0 itens'
            : `${primeiroItem}–${ultimoItem} de ${totalElements} itens`}
        </p>
        <Pagination className="mb-0">
          <Pagination.First
            aria-label="Primeira página"
            disabled={page === 0}
            onClick={() => onChange(0)}
          />
          <Pagination.Prev
            aria-label="Página anterior"
            disabled={page === 0}
            onClick={() => onChange(page - 1)}
          />
          {inicio > 0 && <Pagination.Ellipsis aria-hidden="true" />}
          {numeros.map((n) => (
            <Pagination.Item
              key={n}
              active={n === page}
              aria-label={`Página ${n + 1}`}
              onClick={() => onChange(n)}
            >
              {n + 1}
            </Pagination.Item>
          ))}
          {fim < totalPages && <Pagination.Ellipsis aria-hidden="true" />}
          <Pagination.Next
            aria-label="Próxima página"
            disabled={page >= totalPages - 1}
            onClick={() => onChange(page + 1)}
          />
          <Pagination.Last
            aria-label="Última página"
            disabled={page >= totalPages - 1}
            onClick={() => onChange(totalPages - 1)}
          />
        </Pagination>
      </div>
    </nav>
  );
}
