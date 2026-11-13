// ============================================================
// LoginPage — tela /login
// ============================================================
import { Navigate } from 'react-router';
import { useAuth } from '../hooks/AuthContext.jsx';
import LoginForm from '../components/LoginForm.jsx';

export default function LoginPage() {
  const { status } = useAuth();

  // Se já está autenticado, redireciona para /
  if (status === 'autenticado') {
    return <Navigate to="/" replace />;
  }

  return (
    <main className="min-vh-100 d-flex align-items-center justify-content-center px-3">
      <div style={{ width: '100%' }}>
        <LoginForm />
      </div>
    </main>
  );
}
