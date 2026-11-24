// ============================================================
// API pública da feature catálogo (RNF-FE-01)
// Telas futuras (vendas, compras, estoque) importam SOMENTE
// daqui. Controle de estoque deve usar ItemCatalogoSelect
// com tipo="PRODUTO" (RN-08).
// ============================================================
export { default as ItemCatalogoSelect } from './components/ItemCatalogoSelect.jsx';
export {
  useBuscaItens,
  buscarItensAtivos,
} from './hooks/useBuscaItens.js';
export { useItens } from './hooks/useItens.js';
export { useItemAcoes } from './hooks/useItemAcoes.js';
export { TipoItem } from './lib/tipoItem.js';
