// ============================================================
// StatusBadge — selo de situação ativo/inativo
// A situação é comunicada por TEXTO, não apenas cor (RNF-060).
// ============================================================
import { Badge } from 'react-bootstrap';

/**
 * Selo de situação.
 *
 * @param {object} props
 * @param {boolean} props.ativo
 */
export default function StatusBadge({ ativo }) {
  return (
    <Badge className={ativo ? 'text-bg-success' : 'text-bg-secondary'}>
      {ativo ? 'Ativo' : 'Inativo'}
    </Badge>
  );
}
