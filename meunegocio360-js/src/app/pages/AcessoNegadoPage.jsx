// ============================================================
// AcessoNegadoPage — página 403 para perfil sem permissão (RNF-FE-16)
// ============================================================
import { Link } from 'react-router';
import usePageTitle from '../../shared/lib/usePageTitle.js';

export default function AcessoNegadoPage() {
  usePageTitle('Acesso negado');

  return (
    <div className="min-vh-100 d-flex flex-column align-items-center justify-content-center px-3 text-center">
      <h1 className="fs-2 mb-3">Acesso negado</h1>
      <p className="text-secondary mb-4">
        Seu perfil não tem permissão para acessar esta tela.
      </p>
      <Link to="/" className="btn btn-primary">
        Ir para o início
      </Link>
    </div>
  );
}
