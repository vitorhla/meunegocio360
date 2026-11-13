// ============================================================
// HomePage — página inicial protegida (provisória)
// ============================================================
import { Button } from 'react-bootstrap';
import { useAuth } from '../hooks/AuthContext.jsx';

export default function HomePage() {
  const { usuario, logout } = useAuth();

  return (
    <main className="min-vh-100 d-flex flex-column">
      {/* Cabeçalho provisório */}
      <header className="bg-primary text-dark py-3 px-4 d-flex align-items-center justify-content-between">
        <span
          className="fw-semibold"
          style={{ fontFamily: "'Barlow Condensed', sans-serif", fontSize: '1.25rem' }}
        >
          MeuNegócio360
        </span>
        <span className="small">{usuario?.perfil}</span>
      </header>

      <div className="container mt-5">
        <h1 className="fs-2 mb-3">Olá, {usuario?.nome || 'Usuário'}!</h1>
        <p className="text-secondary mb-4">
          Você está autenticado como <strong>{usuario?.email}</strong>.
        </p>

        <Button
          variant="outline-dark"
          onClick={logout}
          style={{ minHeight: 44, minWidth: 120 }}
        >
          Sair
        </Button>
      </div>
    </main>
  );
}
