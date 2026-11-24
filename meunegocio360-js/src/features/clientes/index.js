// ============================================================
// API pública da feature clientes (RNF-FE-01)
// Telas futuras importam SOMENTE daqui (RNF-FE-01).
// ============================================================
export { default as ClienteSelect } from './components/ClienteSelect.jsx';
export { useBuscaClientes, buscarClientesAtivos } from './hooks/useBuscaClientes.js';
export { useClientes } from './hooks/useClientes.js';
export { useClienteAcoes } from './hooks/useClienteAcoes.js';
