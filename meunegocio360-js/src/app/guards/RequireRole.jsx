// ============================================================
// RequireRole — guarda de perfil (RNF-FE-16, RNF-FE-13)
// Renderiza children apenas se o perfil do usuário estiver
// na lista de perfis permitidos. Caso contrário, exibe
// AcessoNegadoPage SEM renderizar children (nenhuma requisição
// de dados da rota).
// Perfil lido de useAuth (NUNCA localStorage).
// ============================================================
import { useAuth } from '../../features/auth/index.js';
import AcessoNegadoPage from '../pages/AcessoNegadoPage.jsx';

/**
 * Guarda que restringe a rota a um ou mais perfis.
 *
 * @param {object}   props
 * @param {string[]} props.roles  — perfis permitidos
 * @param {React.ReactNode} props.children
 */
export default function RequireRole({ roles, children }) {
  const { usuario } = useAuth();

  const perfil = usuario?.perfil;

  if (!perfil || !roles.includes(perfil)) {
    return <AcessoNegadoPage />;
  }

  return children;
}
