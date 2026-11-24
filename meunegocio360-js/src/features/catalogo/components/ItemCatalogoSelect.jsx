// ============================================================
// ItemCatalogoSelect — seletor público de itens do catálogo
// (RNF-FE-01). Reutiliza o combobox genérico (shared/ui) e a
// busca compartilhada. A prop opcional `tipo` é repassada à
// API (RN-08: telas de estoque DEVEM usar tipo="PRODUTO").
// As sugestões mostram nome, código, tipo e preço.
// ============================================================
import SearchSelect from '../../../shared/ui/SearchSelect.jsx';
import {
  QUERY_KEY_BUSCA_ITENS,
  buscarItensAtivos,
} from '../hooks/useBuscaItens.js';
import { formatarMoedaBR } from '../../../shared/lib/moeda.js';
import { rotuloTipoItem } from '../lib/tipoItem.js';

/**
 * @param {object}   props
 * @param {object}   [props.value]  — item selecionado
 * @param {function} [props.onChange] — recebe o item
 * @param {string}   [props.label='Item do catálogo']
 * @param {string}   [props.tipo]   — PRODUTO | SERVICO (opcional)
 * @param {boolean}  [props.invalid]
 * @param {boolean}  [props.disabled]
 */
export default function ItemCatalogoSelect({
  value,
  onChange,
  label = 'Item do catálogo',
  tipo,
  invalid,
  disabled,
}) {
  return (
    <SearchSelect
      label={label}
      placeholder="Buscar por nome ou código"
      queryKeyPrefix={[...QUERY_KEY_BUSCA_ITENS, tipo || '']}
      fetchSuggestions={(texto) => buscarItensAtivos(texto, tipo)}
      displayValue={(i) => i.nome}
      renderItem={(i) => {
        const partes = [i.nome];
        if (i.codigo) partes.push(`(${i.codigo})`);
        partes.push(rotuloTipoItem(i.tipo));
        partes.push(formatarMoedaBR(i.preco));
        return partes.join(' ');
      }}
      emptyMessage="Nenhum item encontrado"
      value={value}
      onChange={onChange}
      invalid={invalid}
      disabled={disabled}
    />
  );
}
