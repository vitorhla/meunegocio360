// ============================================================
// API da empresa (RNF-FE-03)
// ============================================================
import { apiClient } from '../../../shared/lib/http.js';

/**
 * Consulta a empresa ativa.
 * @returns {Promise<object>} 200 objeto empresa | 404 COMPANY_NOT_REGISTERED
 */
export function getEmpresa() {
  return apiClient('/empresa');
}

/**
 * Cadastra a empresa (ADMIN).
 * @param {object} payload
 * @returns {Promise<object>}
 */
export function cadastrarEmpresa(payload) {
  return apiClient('/empresa', {
    method: 'POST',
    body: payload,
  });
}

/**
 * Atualiza os dados da empresa (ADMIN).
 * @param {string} id
 * @param {object} payload
 * @returns {Promise<object>}
 */
export function atualizarEmpresa(id, payload) {
  return apiClient(`/empresa/${id}`, {
    method: 'PUT',
    body: payload,
  });
}

/**
 * Altera a situação da empresa (ADMIN).
 * @param {string} id
 * @param {boolean} ativa
 * @param {number} versao
 * @returns {Promise<object>}
 */
export function mudarSituacao(id, ativa, versao) {
  return apiClient(`/empresa/${id}/situacao`, {
    method: 'PATCH',
    body: { ativa, versao },
  });
}
