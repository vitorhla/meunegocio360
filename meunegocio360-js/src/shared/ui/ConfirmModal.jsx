// ============================================================
// ConfirmModal — modal de confirmação acessível
// ============================================================
import { useCallback, useEffect, useRef } from 'react';
import { Modal, Button, Spinner } from 'react-bootstrap';

/**
 * Modal de confirmação com gerenciamento de foco.
 *
 * Foco vai para o botão Cancelar ao abrir e retorna ao elemento
 * que estava focado antes da abertura ao fechar.
 *
 * @param {object}   props
 * @param {boolean}  props.show
 * @param {string}   props.title
 * @param {string}   props.message
 * @param {string}   props.confirmLabel
 * @param {string}   [props.cancelLabel='Cancelar']
 * @param {string}   [props.variant='primary']
 * @param {boolean}  [props.busy]
 * @param {function} props.onConfirm
 * @param {function} props.onCancel
 */
export default function ConfirmModal({
  show,
  title,
  message,
  confirmLabel,
  cancelLabel = 'Cancelar',
  variant = 'primary',
  busy,
  onConfirm,
  onCancel,
}) {
  const prevFocusRef = useRef(null);
  const cancelRef = useRef(null);

  // Guarda o elemento focado antes de abrir e foca o Cancelar.
  // O setTimeout (após o fade de ~150 ms do react-bootstrap) garante que o
  // foco vá para o botão Cancelar, e não para o container do diálogo.
  useEffect(() => {
    if (show) {
      prevFocusRef.current = document.activeElement;
      const timer = setTimeout(() => {
        cancelRef.current?.focus();
      }, 200);
      return () => clearTimeout(timer);
    }
    return undefined;
  }, [show]);

  // Fecha o modal (apenas se não estiver ocupado) e restaura o foco
  const handleClose = useCallback(() => {
    if (busy) return;
    prevFocusRef.current?.focus();
    onCancel?.();
  }, [busy, onCancel]);

  return (
    <Modal show={show} onHide={handleClose} size="sm" centered>
      <Modal.Header closeButton>
        <Modal.Title as="h5">{title}</Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <p className="mb-0 small">{message}</p>
      </Modal.Body>
      <Modal.Footer>
        <Button
          ref={cancelRef}
          variant="outline-dark"
          onClick={handleClose}
          disabled={busy}
        >
          {cancelLabel}
        </Button>
        <Button
          variant={variant}
          onClick={onConfirm}
          disabled={busy}
          className="d-inline-flex align-items-center gap-2"
        >
          {busy && (
            <Spinner
              as="span"
              animation="border"
              size="sm"
              role="status"
              aria-hidden="true"
            />
          )}
          {confirmLabel}
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
