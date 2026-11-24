// ============================================================
// API pública da feature fornecedores (RNF-FE-01)
// Telas futuras (compras, contas a pagar) importam SOMENTE
// daqui (RNF-FE-01).
// ============================================================
export { default as FornecedorSelect } from './components/FornecedorSelect.jsx';
export {
  useBuscaFornecedores,
  buscarFornecedoresAtivos,
} from './hooks/useBuscaFornecedores.js';
export { useFornecedores } from './hooks/useFornecedores.js';
export { useFornecedorAcoes } from './hooks/useFornecedorAcoes.js';
