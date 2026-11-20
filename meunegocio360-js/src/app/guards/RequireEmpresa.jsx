// ============================================================
// RequireEmpresa — guarda de empresa ativa
// Renderiza children apenas quando a empresa está 'ativa'.
// Sem flash de conteúdo (nada de negócio renderiza antes de
// a empresa resolver).
// ============================================================
import { Navigate } from 'react-router';
import { useAuth } from '../../features/auth/index.js';
import { useEmpresa } from '../../features/empresa/index.js';
import { Perfis } from '../../shared/lib/perfis.js';
import LoadingSpinner from '../../shared/ui/LoadingSpinner.jsx';
import ErrorScreen from '../../shared/ui/ErrorScreen.jsx';
import AguardandoEmpresaPage from '../pages/AguardandoEmpresaPage.jsx';

/**
 * Guarda que exige empresa ativa para acessar as rotas protegidas.
 *
 * Estados:
 * - 'carregando' -> LoadingSpinner
 * - 'erro'       -> ErrorScreen com onRetry=refetch
 * - 'ausente'    -> ADMIN: redirect /empresa/cadastro
 *                   demais: AguardandoEmpresaPage
 * - 'ativa'      -> children
 *
 * @param {object} props
 * @param {React.ReactNode} props.children
 */
export default function RequireEmpresa({ children }) {
  const { status, erro, refetch } = useEmpresa();
  const { usuario } = useAuth();

  if (status === 'carregando') {
    return <LoadingSpinner text="Carregando empresa…" />;
  }

  if (status === 'erro') {
    return (
      <ErrorScreen
        message={erro?.detail || 'Não foi possível carregar os dados da empresa.'}
        onRetry={refetch}
      />
    );
  }

  if (status === 'ausente') {
    if (usuario?.perfil === Perfis.ADMIN) {
      return <Navigate to="/empresa/cadastro" replace />;
    }
    return <AguardandoEmpresaPage />;
  }

  // 'ativa'
  return children;
}
