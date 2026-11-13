// ============================================================
// API de autenticação (RNF-FE-03)
// ============================================================
import { apiClient } from '../../../shared/lib/http.js';

/**
 * Efetua login.
 * @param {string} email
 * @param {string} senha
 * @returns {Promise<{accessToken: string, expiresIn: number, usuario: object}>}
 */
export function login(email, senha) {
  return apiClient('/auth/login', {
    method: 'POST',
    body: { email, senha },
    auth: false,
  });
}

/**
 * Efetua refresh do token de acesso.
 * Usa o cookie HttpOnly (enviado automaticamente pelo navegador).
 * @returns {Promise<{accessToken: string, expiresIn: number, usuario: object}>}
 */
export function refresh() {
  return apiClient('/auth/refresh', {
    method: 'POST',
    auth: false,
  });
}

/**
 * Efetua logout.
 * @returns {Promise<null>}
 */
export function logout() {
  return apiClient('/auth/logout', {
    method: 'POST',
    auth: false,
  });
}

export default { login, refresh, logout };
