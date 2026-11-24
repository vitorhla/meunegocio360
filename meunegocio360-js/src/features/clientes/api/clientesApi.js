// ============================================================
// API de clientes (contrato RF-BE-003, RNF-FE-03)
// ============================================================
import { apiClient, buildQueryString } from '../../../shared/lib/http.js';

/**
 * Lista clientes paginados.
 * @param {{q?: string, situacao?: string, page?: number, size?: number, sort?: string}} params
 * @returns {Promise<{content: object[], page: number, size: number, totalElements: number, totalPages: number}>}
 */
export function listarClientes({ q, situacao, page, size, sort } = {}) {
  return apiClient(
    `/clientes${buildQueryString({ q, situacao, page, size, sort })}`,
  );
}

/**
 * Consulta um cliente.
 * @param {string} id
 * @returns {Promise<object>} 200 cliente | 404 CUSTOMER_NOT_FOUND
 */
export function obterCliente(id) {
  return apiClient(`/clientes/${id}`);
}

/**
 * Cadastra um cliente (documento e telefone somente com dígitos).
 * @param {object} payload
 * @returns {Promise<object>} 201 cliente criado
 */
export function cadastrarCliente(payload) {
  return apiClient('/clientes', { method: 'POST', body: payload });
}

/**
 * Atualiza um cliente (todos os campos + versao).
 * @param {string} id
 * @param {object} payload
 * @returns {Promise<object>}
 */
export function atualizarCliente(id, payload) {
  return apiClient(`/clientes/${id}`, { method: 'PUT', body: payload });
}

/**
 * Inativa ou reativa um cliente.
 * @param {string} id
 * @param {{ativo: boolean, versao: number}} body
 * @returns {Promise<object>}
 */
export function mudarSituacaoCliente(id, { ativo, versao }) {
  return apiClient(`/clientes/${id}/situacao`, {
    method: 'PATCH',
    body: { ativo, versao },
  });
}

/**
 * Exclui um cliente (somente sem vínculos).
 * @param {string} id
 * @returns {Promise<null>} 204 | 422 CUSTOMER_HAS_HISTORY
 */
export function excluirCliente(id) {
  return apiClient(`/clientes/${id}`, { method: 'DELETE' });
}
