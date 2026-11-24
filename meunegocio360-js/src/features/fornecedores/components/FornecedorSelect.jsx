// ============================================================
// FornecedorSelect — seletor público de fornecedores
// (RNF-FE-01). Reutiliza o combobox genérico (shared/ui) e a
// busca compartilhada de fornecedores. Só oferece
// fornecedores ATIVOS (RN-07 RF-BE-004).
// ============================================================
import SearchSelect from '../../../shared/ui/SearchSelect.jsx';
import {
  QUERY_KEY_BUSCA_FORNECEDORES,
  buscarFornecedoresAtivos,
} from '../hooks/useBuscaFornecedores.js';
import { maskDocumento } from '../../../shared/lib/tipoPessoa.js';

/**
 * Seletor de fornecedor por nome ou documento.
 *
 * @param {object}   props
 * @param {object}   [props.value]  — fornecedor selecionado
 * @param {function} [props.onChange] — recebe o item
 *   ({ id, nome, documento, ... })
 * @param {string}   [props.label='Fornecedor']
 * @param {boolean}  [props.invalid]
 * @param {boolean}  [props.disabled]
 */
export default function FornecedorSelect({
  value,
  onChange,
  label = 'Fornecedor',
  invalid,
  disabled,
}) {
  return (
    <SearchSelect
      label={label}
      placeholder="Buscar por nome ou documento"
      queryKeyPrefix={QUERY_KEY_BUSCA_FORNECEDORES}
      fetchSuggestions={buscarFornecedoresAtivos}
      displayValue={(f) => f.nome}
      renderItem={(f) =>
        `${f.nome} — ${maskDocumento(f.tipoPessoa, f.documento)}`
      }
      emptyMessage="Nenhum fornecedor encontrado"
      value={value}
      onChange={onChange}
      invalid={invalid}
      disabled={disabled}
    />
  );
}
