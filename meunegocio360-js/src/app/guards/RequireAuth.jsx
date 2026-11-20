// ============================================================
// RequireAuth — guarda de rota protegida (RNF-FE-13)
// Rota de layout: renderiza <Outlet/> quando autenticado.
// MOVIDO de shared/ui/ — shared não importa de features/ (RNF-FE-01).
// ============================================================
import { Navigate, Outlet, useLocation } from 'react-router';
import { useAuth } from '../../features/auth/index.js';
import LoadingSpinner from '../../shared/ui/LoadingSpinner.jsx';

/**
 * Guarda de autenticação como rota de layout.
 * 'carregando'/'erro' -> LoadingSpinner
 * 'anonimo' -> Navigate /login com state.from seguro
 * 'autenticado' -> <Outlet/>
 */
export default function RequireAuth() {
  const { status } = useAuth();
  const location = useLocation();

  if (status === 'carregando') {
    return <LoadingSpinner text="Verificando sessão…" />;
  }

  if (status === 'erro') {
    return <LoadingSpinner text="Verificando sessão…" />;
  }

  if (status === 'anonimo') {
    // Proteção contra redirecionamento aberto: só caminhos internos
    const from = location.pathname + location.search;
    const safeFrom = from.startsWith('/') && !from.startsWith('//') ? from : '/';

    return <Navigate to="/login" state={{ from: safeFrom }} replace />;
  }

  return <Outlet />;
}
