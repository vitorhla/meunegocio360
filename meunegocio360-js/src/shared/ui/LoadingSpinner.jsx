// ============================================================
// LoadingSpinner — indicador de carregamento acessível
// ============================================================
import { Spinner } from 'react-bootstrap';

export default function LoadingSpinner({ text = 'Carregando…' }) {
  return (
    <main className="min-vh-100 d-flex flex-column align-items-center justify-content-center">
      <Spinner animation="border" role="status" variant="primary">
        <span className="visually-hidden">{text}</span>
      </Spinner>
      {text && <p className="mt-2 text-secondary small">{text}</p>}
    </main>
  );
}
