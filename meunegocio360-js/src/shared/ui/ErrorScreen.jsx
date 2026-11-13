// ============================================================
// ErrorScreen — tela de erro para falha de rede/5xx na
// restauração de sessão (RNF-FE-14, ciclo de vida item 1)
// ============================================================
import { Alert, Button } from 'react-bootstrap';

export default function ErrorScreen({ message, onRetry }) {
  return (
    <main className="min-vh-100 d-flex flex-column align-items-center justify-content-center px-3 text-center">
      <Alert variant="danger" className="text-start" style={{ maxWidth: 480 }}>
        <Alert.Heading as="h2" className="fs-5">
          Erro de conexão
        </Alert.Heading>
        <p className="mb-0 small">{message}</p>
      </Alert>
      {onRetry && (
        <Button
          variant="primary"
          onClick={onRetry}
          style={{ minHeight: 44, minWidth: 160 }}
        >
          Tentar novamente
        </Button>
      )}
    </main>
  );
}
