// ============================================================
// ClienteSelect — seletor público de clientes (RNF-FE-01)
// Reutiliza o combobox genérico (shared/ui) e a busca
// compartilhada de clientes. Só oferece clientes ATIVOS
// (RN-07 RF-BE-003).
// ============================================================
import SearchSelect from '../../../shared/ui/SearchSelect.jsx';
import {
  QUERY_KEY_BUSCA_CLIENTES,
  buscarClientesAtivos,
} from '../hooks/useBuscaClientes.js';
import { maskDocumento } from '../../../shared/lib/tipoPessoa.js';

/**
 * Seletor de cliente por nome ou documento.
 *
 * @param {object}   props
 * @param {object}   [props.value]  — cliente selecionado
 * @param {function} [props.onChange] — recebe o item
 *   ({ id, nome, documento, ... })
 * @param {string}   [props.label='Cliente']
 * @param {boolean}  [props.invalid]
 * @param {boolean}  [props.disabled]
 */
export default function ClienteSelect({
  value,
  onChange,
  label = 'Cliente',
  invalid,
  disabled,
}) {
  return (
    <SearchSelect
      label={label}
      placeholder="Buscar por nome ou documento"
      queryKeyPrefix={QUERY_KEY_BUSCA_CLIENTES}
      fetchSuggestions={buscarClientesAtivos}
      displayValue={(c) => c.nome}
      renderItem={(c) =>
        `${c.nome} — ${maskDocumento(c.tipoPessoa, c.documento)}`
      }
      emptyMessage="Nenhum cliente encontrado"
      value={value}
      onChange={onChange}
      invalid={invalid}
      disabled={disabled}
    />
  );
}
