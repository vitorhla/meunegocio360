// ============================================================
// TipoBadge — selo de tipo do item de catálogo.
// Usa TEXTO ("Produto"/"Serviço"), não apenas cor (RNF-060).
// ============================================================
import { rotuloTipoItem, TipoItem } from '../lib/tipoItem.js';

/**
 * @param {object} props
 * @param {string} props.tipo — PRODUTO | SERVICO
 */
export default function TipoBadge({ tipo }) {
  const classe =
    tipo === TipoItem.SERVICO ? 'text-bg-primary' : 'text-bg-light border';
  return (
    <span className={`badge ${classe}`} title={rotuloTipoItem(tipo)}>
      {rotuloTipoItem(tipo)}
    </span>
  );
}
