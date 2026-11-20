// ============================================================
// HomePage — página inicial protegida (RNF-FE-002)
// ============================================================
import { useEffect, useState } from 'react';
import { Alert } from 'react-bootstrap';
import { useNavigate, useLocation } from 'react-router';
import { useAuth } from '../hooks/AuthContext.jsx';
import usePageTitle from '../../../shared/lib/usePageTitle.js';

export default function HomePage() {
  const { usuario } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  usePageTitle('Início');

  // Flash de sucesso (ex.: cadastro da empresa). O estado da navegação é
  // limpo de imediato (para o flash não reaparecer ao voltar), mas o alerta
  // fica visível por alguns segundos antes de desaparecer.
  const flashInicial = location.state?.flash;
  const [flash, setFlash] = useState(flashInicial);

  useEffect(() => {
    if (flashInicial) {
      // Substitui a entrada para não reaparecer ao navegar de volta
      navigate(location.pathname, { replace: true });
    }
  }, [flashInicial, navigate, location.pathname]);

  useEffect(() => {
    if (!flash) return undefined;
    const timer = setTimeout(() => setFlash(null), 5000);
    return () => clearTimeout(timer);
  }, [flash]);

  return (
    <div>
      {flash && (
        <Alert
          variant={flash.variant || 'success'}
          role="alert"
          dismissible
          className="py-2 small"
          onClose={() => setFlash(null)}
        >
          {flash.message}
        </Alert>
      )}

      <h1 className="fs-2 mb-3">Olá, {usuario?.nome || 'Usuário'}!</h1>
      <p className="text-secondary mb-0">
        Você está autenticado como <strong>{usuario?.email}</strong>.
      </p>
    </div>
  );
}
