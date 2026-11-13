// ============================================================
// Cliente HTTP único (RNF-FE-10, RNF-FE-11, RNF-FE-12, RNF-FE-15)
// ============================================================

/**
 * Erro tipado para respostas application/problem+json (RFC 9457).
 */
export class ApiError extends Error {
  /**
   * @param {number} status       Código HTTP
   * @param {string} code         Código estável (ex.: INVALID_CREDENTIALS)
   * @param {string} detail       Mensagem legível
   * @param {Array<{field:string,message:string}>} [errors] Erros por campo
   * @param {number|null} [retryAfter] Segundos do header Retry-After
   */
  constructor(status, code, detail, errors = [], retryAfter = null) {
    super(detail);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
    this.detail = detail;
    this.errors = errors;
    this.retryAfter = retryAfter;
  }
}

/** Gera um UUID v4 para X-Request-Id */
function generateRequestId() {
  // crypto.randomUUID está disponível em todos os navegadores modernos
  return crypto.randomUUID();
}

/**
 * Referência para o token de acesso atual (armazenado apenas em memória).
 * @type {{ current: string|null }}
 */
export const accessTokenRef = { current: null };

/**
 * Callback chamado quando a sessão deve ser encerrada (renovação falhou).
 * Definido pelo AuthProvider.
 * @type {{ current: ((message:string, from:string|null) => void)|null }}
 */
export const onSessionExpired = { current: null };

// --- Single-flight para renovação ---
let refreshPromise = null;

/**
 * Tenta renovar o token de acesso via /auth/refresh.
 * Usa single-flight: se várias chamadas pedem refresh ao mesmo tempo,
 * apenas uma requisição é disparada.
 * @returns {Promise<{accessToken: string, usuario: object, expiresIn: number}>}
 */
async function doRefresh() {
  if (refreshPromise) return refreshPromise;

  refreshPromise = (async () => {
    const baseUrl = getBaseUrl();
    const res = await fetch(`${baseUrl}/auth/refresh`, {
      method: 'POST',
      credentials: 'same-origin',
      headers: { 'X-Request-Id': generateRequestId() },
    });

    if (!res.ok) {
      // A rota de refresh NUNCA entra no fluxo de renovar-e-repetir
      if (res.status === 401) {
        const body = await parseProblemJson(res);
        throw new ApiError(
          res.status,
          body.code || 'INVALID_REFRESH_TOKEN',
          body.detail || 'Sessão expirada.',
        );
      }
      throw new ApiError(
        res.status,
        'REFRESH_FAILED',
        'Não foi possível renovar a sessão.',
      );
    }

    const data = await res.json();
    accessTokenRef.current = data.accessToken;
    return data;
  })();

  // Limpa a promise depois que resolve/rejeita
  refreshPromise.finally(() => {
    refreshPromise = null;
  });

  return refreshPromise;
}

/** Lê a URL base da API */
function getBaseUrl() {
  return import.meta.env.VITE_API_BASE_URL || '/api/v1';
}

/** Tenta extrair o corpo problem+json */
async function parseProblemJson(response) {
  try {
    const ct = response.headers.get('content-type') || '';
    if (ct.includes('application/problem+json')) {
      return await response.json();
    }
    return {};
  } catch {
    return {};
  }
}

/**
 * Cliente HTTP baseado em fetch.
 *
 * @param {string}  path               Caminho relativo à base URL (ex.: "/auth/login")
 * @param {object}  [options]
 * @param {string}  [options.method]   Método HTTP (padrão "GET")
 * @param {object}  [options.body]     Corpo (objeto convertido para JSON)
 * @param {object}  [options.headers]  Cabeçalhos adicionais
 * @param {number}  [options.timeout]  Timeout em ms (padrão 15 s)
 * @param {boolean} [options.auth]     Se true, injeta Authorization (padrão true)
 * @returns {Promise<any>}
 */
export async function apiClient(path, options = {}) {
  const { method = 'GET', body, headers = {}, timeout = 15000, auth = true } = options;

  const url = `${getBaseUrl()}${path}`;
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  const requestHeaders = {
    'Content-Type': 'application/json',
    'X-Request-Id': generateRequestId(),
    ...headers,
  };

  if (auth && accessTokenRef.current) {
    requestHeaders['Authorization'] = `Bearer ${accessTokenRef.current}`;
  }

  // Se for /auth/refresh ou /auth/logout com cookie, não definimos Content-Type
  // para refresh/logout pois não têm body; mas se body existir, mantemos.
  if (!body) {
    delete requestHeaders['Content-Type'];
  }

  try {
    const res = await fetch(url, {
      method,
      headers: requestHeaders,
      body: body ? JSON.stringify(body) : undefined,
      signal: controller.signal,
      credentials: 'same-origin',
    });

    clearTimeout(timeoutId);

    // ---- Tratamento de 401 em chamadas autenticadas (RNF-FE-11) ----
    // A rota /auth/refresh NUNCA entra neste fluxo (evitar laço infinito)
    if (
      res.status === 401 &&
      auth &&
      !path.startsWith('/auth/refresh') &&
      accessTokenRef.current
    ) {
      try {
        const refreshData = await doRefresh();
        // Repetir a requisição original com o novo token
        requestHeaders['Authorization'] = `Bearer ${refreshData.accessToken}`;
        if (!body) delete requestHeaders['Content-Type'];

        const retryRes = await fetch(url, {
          method,
          headers: requestHeaders,
          body: body ? JSON.stringify(body) : undefined,
          signal: AbortSignal.timeout(timeout),
          credentials: 'same-origin',
        });

        if (!retryRes.ok) {
          const errBody = await parseProblemJson(retryRes);
          throw new ApiError(
            retryRes.status,
            errBody.code || 'UNKNOWN_ERROR',
            errBody.detail || retryRes.statusText,
            errBody.errors || [],
            parseRetryAfter(retryRes),
          );
        }

        if (retryRes.status === 204) return null;
        return await retryRes.json();
      } catch (refreshError) {
        // Se a renovação falhar, encerrar sessão
        if (refreshError instanceof ApiError) {
          if (refreshError.code === 'INVALID_REFRESH_TOKEN') {
            const from = window.location.pathname;
            if (onSessionExpired.current) {
              onSessionExpired.current('Sua sessão expirou. Entre novamente.', from);
            }
          }
        }
        throw refreshError;
      }
    }

    // ---- Sucesso ----
    if (res.status === 204) return null;
    if (res.ok) {
      // 200 sem conteúdo
      const text = await res.text();
      return text ? JSON.parse(text) : null;
    }

    // ---- Erro problem+json ----
    const errBody = await parseProblemJson(res);
    throw new ApiError(
      res.status,
      errBody.code || 'UNKNOWN_ERROR',
      errBody.detail || res.statusText,
      errBody.errors || [],
      parseRetryAfter(res),
    );
  } catch (err) {
    clearTimeout(timeoutId);

    if (err instanceof ApiError) throw err;

    // Timeout ou falha de rede
    if (err.name === 'AbortError') {
      throw new ApiError(0, 'NETWORK_ERROR', 'A requisição excedeu o tempo limite.');
    }

    throw new ApiError(0, 'NETWORK_ERROR', 'Não foi possível conectar ao servidor.');
  }
}

function parseRetryAfter(res) {
  const val = res.headers.get('Retry-After');
  if (val) {
    const seconds = parseInt(val, 10);
    return Number.isNaN(seconds) ? null : seconds;
  }
  return null;
}

export default apiClient;
