// ============================================================
// API de fornecedores (contrato RF-BE-004, RNF-FE-03)
// ============================================================
import { apiClient, buildQueryString } from '../../../shared/lib/http.js';

/**
 * Lista fornecedores paginados.
 * @param {{q?: string, situacao?: string, page?: number, size?: number, sort?: string}} params
 * @returns {Promise<{content: object[], page: number, size: number, totalElements: number, totalPages: number}>}
 */
export function listarFornecedores({ q, situacao, page, size, sort } = {}) {
  return apiClient(
    `/fornecedores${buildQueryString({ q, situacao, page, size, sort })}`,
  );
}

/**
 * Consulta um fornecedor.
 * @param {string} id
 * @returns {Promise<object>} 200 fornecedor | 404 SUPPLIER_NOT_FOUND
 */
export function obterFornecedor(id) {
  return apiClient(`/fornecedores/${id}`);
}

/**
 * Cadastra um fornecedor (documento e telefone somente com dígitos).
 * @param {object} payload
 * @returns {Promise<object>} 201 fornecedor criado
 */
export function cadastrarFornecedor(payload) {
  return apiClient('/fornecedores', { method: 'POST', body: payload });
}

/**
 * Atualiza um fornecedor (todos os campos + versao).
 * @param {string} id
 * @param {object} payload
 * @returns {Promise<object>}
 */
export function atualizarFornecedor(id, payload) {
  return apiClient(`/fornecedores/${id}`, { method: 'PUT', body: payload });
}

/**
 * Inativa ou reativa um fornecedor.
 * @param {string} id
 * @param {{ativo: boolean, versao: number}} body
 * @returns {Promise<object>}
 */
export function mudarSituacaoFornecedor(id, { ativo, versao }) {
  return apiClient(`/fornecedores/${id}/situacao`, {
    method: 'PATCH',
    body: { ativo, versao },
  });
}

/**
 * Exclui um fornecedor (somente sem vínculos).
 * @param {string} id
 * @returns {Promise<null>} 204 | 422 SUPPLIER_HAS_HISTORY
 */
export function excluirFornecedor(id) {
  return apiClient(`/fornecedores/${id}`, { method: 'DELETE' });
}
