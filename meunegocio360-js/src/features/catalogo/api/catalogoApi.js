// ============================================================
// API do catálogo (contratos RF-BE-005, RNF-FE-03):
// itens de catálogo (/itens-catalogo) e categorias (/categorias)
// ============================================================
import { apiClient, buildQueryString } from '../../../shared/lib/http.js';

// ------------------------------------------------------------
// Itens de catálogo
// ------------------------------------------------------------

/**
 * Lista itens do catálogo paginados (filtros combinam entre si).
 * @param {{q?: string, tipo?: string, categoriaId?: string, situacao?: string, page?: number, size?: number, sort?: string}} params
 * @returns {Promise<{content: object[], page: number, size: number, totalElements: number, totalPages: number}>}
 */
export function listarItens({ q, tipo, categoriaId, situacao, page, size, sort } = {}) {
  return apiClient(
    `/itens-catalogo${buildQueryString({ q, tipo, categoriaId, situacao, page, size, sort })}`,
  );
}

/**
 * Consulta um item.
 * @param {string} id
 * @returns {Promise<object>} 200 item | 404 ITEM_NOT_FOUND
 */
export function obterItem(id) {
  return apiClient(`/itens-catalogo/${id}`);
}

/**
 * Cadastra um item (preço como decimal em string, RNF-033).
 * @param {object} payload
 * @returns {Promise<object>} 201 item criado
 */
export function cadastrarItem(payload) {
  return apiClient('/itens-catalogo', { method: 'POST', body: payload });
}

/**
 * Atualiza um item (todos os campos + versao; tipo imutável).
 * @param {string} id
 * @param {object} payload
 * @returns {Promise<object>}
 */
export function atualizarItem(id, payload) {
  return apiClient(`/itens-catalogo/${id}`, { method: 'PUT', body: payload });
}

/**
 * Inativa ou reativa um item.
 * @param {string} id
 * @param {{ativo: boolean, versao: number}} body
 * @returns {Promise<object>}
 */
export function mudarSituacaoItem(id, { ativo, versao }) {
  return apiClient(`/itens-catalogo/${id}/situacao`, {
    method: 'PATCH',
    body: { ativo, versao },
  });
}

/**
 * Exclui um item (somente sem vínculos).
 * @param {string} id
 * @returns {Promise<null>} 204 | 422 ITEM_HAS_HISTORY
 */
export function excluirItem(id) {
  return apiClient(`/itens-catalogo/${id}`, { method: 'DELETE' });
}

// ------------------------------------------------------------
// Categorias
// ------------------------------------------------------------

/**
 * Lista categorias paginadas.
 * @param {{q?: string, situacao?: string, page?: number, size?: number, sort?: string}} params
 * @returns {Promise<{content: object[], page: number, size: number, totalElements: number, totalPages: number}>}
 */
export function listarCategorias({ q, situacao, page, size, sort } = {}) {
  return apiClient(
    `/categorias${buildQueryString({ q, situacao, page, size, sort })}`,
  );
}

/**
 * Cria uma categoria.
 * @param {{nome: string}} payload
 * @returns {Promise<object>} 201 categoria criada
 */
export function criarCategoria({ nome }) {
  return apiClient('/categorias', { method: 'POST', body: { nome } });
}

/**
 * Renomeia uma categoria (com versao).
 * @param {string} id
 * @param {{nome: string, versao: number}} body
 * @returns {Promise<object>}
 */
export function renomearCategoria(id, { nome, versao }) {
  return apiClient(`/categorias/${id}`, { method: 'PUT', body: { nome, versao } });
}

/**
 * Inativa ou reativa uma categoria.
 * @param {string} id
 * @param {{ativo: boolean, versao: number}} body
 * @returns {Promise<object>}
 */
export function mudarSituacaoCategoria(id, { ativo, versao }) {
  return apiClient(`/categorias/${id}/situacao`, {
    method: 'PATCH',
    body: { ativo, versao },
  });
}

/**
 * Exclui uma categoria (somente sem itens vinculados).
 * @param {string} id
 * @returns {Promise<null>} 204 | 422 CATEGORY_IN_USE
 */
export function excluirCategoria(id) {
  return apiClient(`/categorias/${id}`, { method: 'DELETE' });
}
