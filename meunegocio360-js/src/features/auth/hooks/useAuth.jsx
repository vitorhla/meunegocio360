// ============================================================
// AuthProvider — provider de autenticação
// accessToken só em memória (RNF-FE-12)
// ============================================================
import { useCallback, useEffect, useRef, useState } from 'react';
import { apiClient, accessTokenRef, onSessionExpired } from '../../../shared/lib/http.js';
import { AuthContext, AuthStatus } from './AuthContext.jsx';

// Re-exporta para conveniência
export { AuthContext, AuthStatus };

// Singleton de restauração de boot: garante que o segundo mount do
// StrictMode reutilize a mesma promise, evitando duplicar a chamada
// HTTP a /auth/refresh com o mesmo cookie (que é rotativo e seria
// revogado pela primeira, derrubando a sessão — armadilha #4 do
// requisito). Como o módulo é carregado uma vez por página, reload()
// reseta este estado naturalmente.
let bootRestorePromise = null;

/**
 * Encapsula a restauração de sessão no boot com deduplicação.
 * Retorna a mesma promise enquanto uma restauração estiver em andamento.
 */
function restoreSessionBoot() {
  if (bootRestorePromise) return bootRestorePromise;

  bootRestorePromise = (async () => {
    const data = await apiClient('/auth/refresh', {
      method: 'POST',
      auth: false,
      timeout: 10000,
    });
    return data;
  })();

  // Permite que um novo carregamento da página (reload()) tente de novo
  bootRestorePromise.finally(() => {
    bootRestorePromise = null;
  });

  return bootRestorePromise;
}

/**
 * Provider de autenticação.
 *
 * Gerencia:
 * - Restauração de sessão no boot via /auth/refresh
 * - AccessToken apenas em memória (NUNCA localStorage/sessionStorage)
 * - Estados: carregando → autenticado | anônimo | erro
 * - Renovação preventiva (~60 s antes de expiresIn)
 * - Callback para sessão expirada (disparado pelo cliente HTTP)
 */
export function AuthProvider({ children }) {
  const [status, setStatus] = useState(AuthStatus.CARREGANDO);
  const [usuario, setUsuario] = useState(null);
  const [sessionError, setSessionError] = useState(null);
  const refreshTimerRef = useRef(null);
  const scheduleRef = useRef(null);

  // Cancela o timer de renovação preventiva
  const cancelRefreshTimer = useCallback(() => {
    if (refreshTimerRef.current) {
      clearTimeout(refreshTimerRef.current);
      refreshTimerRef.current = null;
    }
  }, []);

  // Agendar refresh preventivo via ref para permitir auto-referência
  const schedulePreemptiveRefresh = useCallback(
    (expiresIn) => {
      cancelRefreshTimer();

      if (!expiresIn || expiresIn < 30) return;

      const msBefore = Math.max(0, (expiresIn - 60) * 1000);

      refreshTimerRef.current = setTimeout(async () => {
        try {
          const data = await apiClient('/auth/refresh', {
            method: 'POST',
            auth: false,
            timeout: 10000,
          });
          accessTokenRef.current = data.accessToken;
          setUsuario(data.usuario);
          if (scheduleRef.current) {
            scheduleRef.current(data.expiresIn);
          }
        } catch {
          // Falha na renovação preventiva não faz nada
        }
      }, msBefore);
    },
    [cancelRefreshTimer],
  );

  // Mantém a referência atualizada sem violar regra de refs no render
  useEffect(() => {
    scheduleRef.current = schedulePreemptiveRefresh;
  }, [schedulePreemptiveRefresh]);

  // Inicia sessão com dados do login/refresh
  const setSession = useCallback(
    (accessToken, usuarioData, expiresIn) => {
      accessTokenRef.current = accessToken;
      setUsuario(usuarioData);
      setSessionError(null);
      setStatus(AuthStatus.AUTENTICADO);
      schedulePreemptiveRefresh(expiresIn);
    },
    [schedulePreemptiveRefresh],
  );

  // Limpa toda a sessão
  const clearSession = useCallback(
    (message) => {
      cancelRefreshTimer();
      accessTokenRef.current = null;
      setUsuario(null);
      setSessionError(message || null);
      setStatus(AuthStatus.ANONIMO);
    },
    [cancelRefreshTimer],
  );

  // Callback para sessão expirada (chamado pelo cliente HTTP)
  const handleSessionExpired = useCallback(
    (message, from) => {
      cancelRefreshTimer();
      accessTokenRef.current = null;
      setUsuario(null);
      setSessionError(message);
      setStatus(AuthStatus.ANONIMO);
      if (from) {
        sessionStorage.setItem('login_redirect_from', from);
      }
    },
    [cancelRefreshTimer],
  );

  // Logout
  const logout = useCallback(async () => {
    cancelRefreshTimer();
    try {
      await apiClient('/auth/logout', {
        method: 'POST',
        auth: false,
        timeout: 5000,
      });
    } catch {
      // Falha de rede no logout é ignorada (item 5 do ciclo de vida)
    }
    accessTokenRef.current = null;
    setUsuario(null);
    setSessionError(null);
    setStatus(AuthStatus.ANONIMO);
  }, [cancelRefreshTimer]);

  // Tentar novamente após falha de rede
  const retryRestore = useCallback(() => {
    setStatus(AuthStatus.CARREGANDO);
    setSessionError(null);
    window.location.reload();
  }, []);

  // Registra o callback de sessão expirada no módulo HTTP
  useEffect(() => {
    onSessionExpired.current = handleSessionExpired;
    return () => {
      onSessionExpired.current = null;
    };
  }, [handleSessionExpired]);

  // Restauração de sessão no boot (usa singleton para evitar duplicar
  // a chamada HTTP no StrictMode — refresh token rotativo, segunda
  // chamada mataria a sessão)
  useEffect(() => {
    let cancelled = false;

    async function restoreEffect() {
      try {
        const data = await restoreSessionBoot();
        if (cancelled) return;
        accessTokenRef.current = data.accessToken;
        setUsuario(data.usuario);
        setSessionError(null);
        setStatus(AuthStatus.AUTENTICADO);
        schedulePreemptiveRefresh(data.expiresIn);
      } catch (err) {
        if (cancelled) return;

        if (err.status === 401) {
          accessTokenRef.current = null;
          setUsuario(null);
          setSessionError(null);
          setStatus(AuthStatus.ANONIMO);
          return;
        }

        setSessionError(
          'Não foi possível conectar ao servidor. Verifique sua conexão e tente novamente.',
        );
        setStatus(AuthStatus.ERRO);
      }
    }

    restoreEffect();

    return () => {
      cancelled = true;
      cancelRefreshTimer();
    };
  }, [cancelRefreshTimer, schedulePreemptiveRefresh]);

  const value = {
    status,
    usuario,
    sessionError,
    setSession,
    clearSession,
    logout,
    retryRestore,
    AuthStatus,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
