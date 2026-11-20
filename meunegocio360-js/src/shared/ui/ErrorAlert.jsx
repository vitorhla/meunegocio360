// ============================================================
// ErrorAlert — alerta de erro reutilizável com ação opcional
// ============================================================
import { Alert, Button } from 'react-bootstrap';

/**
 * Alerta de erro com role="alert" e botão de ação opcional.
 *
 * @param {object}   props
 * @param {string}   [props.variant='danger']
 * @param {string}   props.message
 * @param {string}   [props.actionLabel]  — texto do botão de ação
 * @param {function} [props.onAction]     — callback da ação
 */
export default function ErrorAlert({
  variant = 'danger',
  message,
  actionLabel,
  onAction,
}) {
  return (
    <Alert variant={variant} role="alert" className="py-2 small mb-3">
      <div className="d-flex align-items-center justify-content-between gap-2">
        <span>{message}</span>
        {actionLabel && onAction && (
          <Button
            variant="outline-dark"
            size="sm"
            className="text-nowrap"
            onClick={onAction}
          >
            {actionLabel}
          </Button>
        )}
      </div>
    </Alert>
  );
}
