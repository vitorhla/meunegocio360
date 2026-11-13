// ============================================================
// RequireAuth — guarda de rota protegida (RNF-FE-13)
// ============================================================
import { Navigate, useLocation } from 'react-router';
import { useAuth } from '../../features/auth/index.js';
import LoadingSpinner from './LoadingSpinner.jsx';

/**
 * Componente de guarda que redireciona para /login quando
 * o usuário não está autenticado.
 *
 * Preserva a rota de origem (state.from) para redirecionamento
 * pós-login, com proteção contra redirecionamento aberto.
 */
export default function RequireAuth({ children }) {
  const { status } = useAuth();
  const location = useLocation();

  if (status === 'carregando') {
    return <LoadingSpinner text="Verificando sessão…" />;
  }

  if (status === 'erro') {
    // Falha de rede na restauração: o AuthProvider já mostra a tela
    // de erro. Se chegar aqui, renderiza os filhos? Não — melhor
    // deixar o AuthProvider gerenciar o estado.
    return <LoadingSpinner text="Verificando sessão…" />;
  }

  if (status === 'anonimo') {
    // Proteção contra redirecionamento aberto: só caminhos internos
    const from = location.pathname + location.search;
    const safeFrom = from.startsWith('/') && !from.startsWith('//') ? from : '/';

    return <Navigate to="/login" state={{ from: safeFrom }} replace />;
  }

  return children;
}
