// ============================================================
// ModalCategoria — modal de criação/renomeação de categoria
// (RF-FE-005, /catalogo/categorias). Erro de nome duplicado
// (CATEGORY_NAME_DUPLICATE) exibido no campo, por code
// (RNF-FE-14).
// ============================================================
import { useState } from 'react';
import { Modal, Form, Button, Spinner } from 'react-bootstrap';
import { useCategoriaAcoes } from '../hooks/useCategoriaAcoes.js';

/**
 * @param {object}  props
 * @param {boolean} props.mostrar
 * @param {'criar'|'renomear'} props.modo
 * @param {object}  [props.categoria] — categoria (modo renomear)
 * @param {function} props.onSucesso — (mensagem: string) => void
 * @param {function} props.onFechar
 */
export default function ModalCategoria({
  mostrar,
  modo,
  categoria,
  onSucesso,
  onFechar,
}) {
  const { criar, renomear } = useCategoriaAcoes();
  const [nome, setNome] = useState(categoria?.nome || '');
  const [erroCampo, setErroCampo] = useState(null);

  const busy = criar.isPending || renomear.isPending;
  const isRenomear = modo === 'renomear';

  async function salvar() {
    const n = nome.trim();
    if (n.length < 2) {
      setErroCampo('Informe o nome da categoria.');
      return;
    }
    if (n.length > 80) {
      setErroCampo('O nome deve ter no máximo 80 caracteres.');
      return;
    }
    setErroCampo(null);
    try {
      if (isRenomear) {
        await renomear.mutateAsync({
          id: categoria.id,
          nome: n,
          versao: categoria.versao,
        });
        onSucesso('Categoria renomeada com sucesso.');
      } else {
        await criar.mutateAsync({ nome: n });
        onSucesso('Categoria criada com sucesso.');
      }
      onFechar();
    } catch (err) {
      // Tratamento por CODE (RNF-FE-14)
      if (err.code === 'CATEGORY_NAME_DUPLICATE') {
        setErroCampo('Já existe uma categoria com este nome.');
      } else if (err.code === 'VERSION_CONFLICT') {
        setErroCampo(
          'A categoria foi alterada por outra sessão. Feche o modal e tente novamente.',
        );
      } else if (err.code === 'COMPANY_NOT_ACTIVE') {
        setErroCampo('Cadastre ou ative a empresa para continuar.');
      } else {
        setErroCampo(
          'Não foi possível salvar a categoria. Verifique sua conexão e tente novamente.',
        );
      }
    }
  }

  return (
    <Modal show={mostrar} onHide={busy ? undefined : onFechar} centered>
      <Modal.Header closeButton>
        <Modal.Title as="h2">
          {isRenomear ? 'Renomear categoria' : 'Nova categoria'}
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        <Form.Group controlId="modal-categoria-nome">
          <Form.Label>Nome</Form.Label>
          <Form.Control
            type="text"
            maxLength={80}
            autoFocus
            value={nome}
            onChange={(e) => {
              setNome(e.target.value);
              if (erroCampo) setErroCampo(null);
            }}
            isInvalid={!!erroCampo}
            aria-describedby={
              erroCampo ? 'modal-categoria-nome-feedback' : undefined
            }
            disabled={busy}
          />
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="modal-categoria-nome-feedback"
          >
            {erroCampo}
          </Form.Control.Feedback>
        </Form.Group>
      </Modal.Body>
      <Modal.Footer>
        <Button
          variant="outline-dark"
          onClick={onFechar}
          disabled={busy}
        >
          Cancelar
        </Button>
        <Button
          variant="primary"
          onClick={salvar}
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
          {busy ? 'Salvando…' : 'Salvar'}
        </Button>
      </Modal.Footer>
    </Modal>
  );
}
