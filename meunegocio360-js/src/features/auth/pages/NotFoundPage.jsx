// ============================================================
// NotFoundPage — página 404
// ============================================================
import { Link } from 'react-router';

export default function NotFoundPage() {
  return (
    <main className="min-vh-100 d-flex flex-column align-items-center justify-content-center px-3 text-center">
      <h1
        className="display-4 fw-bold"
        style={{ fontFamily: "'Barlow Condensed', sans-serif" }}
      >
        404
      </h1>
      <p className="fs-5 text-secondary mb-4">Página não encontrada.</p>
      <Link to="/" className="btn btn-primary" style={{ minHeight: 44 }}>
        Voltar para o início
      </Link>
    </main>
  );
}
