// ============================================================
// ClienteForm — formulário de cadastro/edição de cliente
// RHF + zodResolver + tratamento de erro por CODE (RNF-FE-14)
// ============================================================
import { useState } from 'react';
import { Link } from 'react-router';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Form, Button, Spinner, Alert } from 'react-bootstrap';
import { clienteSchema } from '../schemas.js';
import { useClienteAcoes } from '../hooks/useClienteAcoes.js';
import {
  TipoPessoa,
  maxDigitosDocumento,
  rotuloDocumento,
} from '../../../shared/lib/tipoPessoa.js';
import DocumentoInput from '../../../shared/ui/DocumentoInput.jsx';
import TelefoneInput from '../../../shared/ui/TelefoneInput.jsx';
import ErrorAlert from '../../../shared/ui/ErrorAlert.jsx';

/**
 * Formulário de cadastro ou edição de cliente.
 *
 * @param {object}    props
 * @param {'cadastro'|'edicao'} props.modo
 * @param {object}    [props.clienteInicial] — dados atuais (edicao)
 * @param {function}  props.onSucesso
 * @param {function}  [props.onRecarregar]   — recarregar (edicao)
 * @param {function}  [props.onCancelar]     — cancelar (edicao)
 */
export default function ClienteForm({
  modo,
  clienteInicial,
  onSucesso,
  onRecarregar,
  onCancelar,
}) {
  const isEdicao = modo === 'edicao';

  // { message, acao } — acao: 'recarregar' | 'reenviar' | 'empresa' | null
  // (decisão por código, nunca por texto — RNF-FE-14)
  const [serverError, setServerError] = useState(null);
  const [avisoDocumento, setAvisoDocumento] = useState(null);

  const { cadastrar, atualizar } = useClienteAcoes();

  const {
    register,
    handleSubmit,
    setError,
    setFocus,
    setValue,
    getValues,
    trigger,
    control,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(clienteSchema),
    defaultValues: isEdicao
      ? {
          tipoPessoa: clienteInicial?.tipoPessoa || TipoPessoa.FISICA,
          nome: clienteInicial?.nome || '',
          documento: clienteInicial?.documento || '',
          email: clienteInicial?.email || '',
          telefone: clienteInicial?.telefone || '',
        }
      : {
          tipoPessoa: '',
          nome: '',
          documento: '',
          email: '',
          telefone: '',
        },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
  });

  // Estado local do tipo de pessoa, atualizado no onChange dos
  // rádios (o RHF guarda o mesmo valor no form state).
  const [tipoPessoaAtual, setTipoPessoaAtual] = useState(
    isEdicao ? clienteInicial?.tipoPessoa || TipoPessoa.FISICA : '',
  );
  const isSaving = isSubmitting;

  /**
   * Ao trocar o tipo de pessoa, o documento é revalidado e
   * limpo se o tamanho for incompatível, com aviso
   * (RF-FE-003, seção Formulário).
   */
  function aoMudarTipo(e) {
    const novoTipo = e.target.value;
    setTipoPessoaAtual(novoTipo);
    setAvisoDocumento(null);
    const doc = getValues('documento') || '';
    const max = maxDigitosDocumento(novoTipo);
    if (doc.length > max) {
      setValue('documento', '', { shouldValidate: true, shouldDirty: true });
      setAvisoDocumento(
        `Documento limpo: ${rotuloDocumento(novoTipo)} tem comprimento diferente do informado.`,
      );
    } else if (doc.length > 0) {
      trigger('documento');
    }
  }

  /**
   * Monta o payload: documento e telefone SOMENTE dígitos
   * (RNF-FE-28); opcionais vazios são omitidos.
   */
  function montarPayload(data) {
    const payload = {
      tipoPessoa: data.tipoPessoa,
      nome: data.nome.trim(),
      documento: data.documento,
    };
    if (data.email) payload.email = data.email.trim();
    if (data.telefone) payload.telefone = data.telefone;
    if (isEdicao && clienteInicial) payload.versao = clienteInicial.versao;
    return payload;
  }

  async function onSubmit(data) {
    setServerError(null);

    try {
      const payload = montarPayload(data);
      const salvo = isEdicao
        ? await atualizar.mutateAsync({ id: clienteInicial.id, payload })
        : await cadastrar.mutateAsync(payload);
      onSucesso(salvo);
    } catch (err) {
      if (err.code === 'VALIDATION_ERROR' && err.errors?.length) {
        for (const fe of err.errors) {
          setError(fe.field, { message: fe.message });
        }
        const primeiro = err.errors[0]?.field;
        if (primeiro) {
          // timeout: o estado de erros ainda não foi processado
          // no submit assíncrono
          setTimeout(() => {
            try {
              setFocus(primeiro);
            } catch {
              // campo pode não existir no formulário — ignora
            }
          }, 100);
        }
        return;
      }

      if (err.code === 'CUSTOMER_DOCUMENT_DUPLICATE') {
        setError('documento', {
          message:
            'Já existe um cliente com este documento. Ele pode estar inativo; busque por ele e reative.',
        });
        setTimeout(() => {
          try {
            setFocus('documento');
          } catch {
            // ignora
          }
        }, 100);
        return;
      }

      if (err.code === 'VERSION_CONFLICT') {
        setServerError({
          message:
            'O cliente foi alterado por outra sessão. Recarregue os dados para continuar.',
          acao: 'recarregar',
        });
        // O formulário MANTÉM o que foi digitado até a escolha
        return;
      }

      if (err.code === 'COMPANY_NOT_ACTIVE') {
        setServerError({
          message: 'Cadastre ou ative a empresa para continuar.',
          acao: 'empresa',
        });
        return;
      }

      if (err.code === 'ACCESS_DENIED') {
        setServerError({
          message: 'Você não tem permissão para cadastrar clientes.',
          acao: null,
        });
        return;
      }

      // Rede / 5xx
      setServerError({
        message:
          'Não foi possível salvar o cliente. Verifique sua conexão e tente novamente.',
        acao: 'reenviar',
      });
    }
  }

  const submitLabel = isEdicao ? 'Salvar' : 'Cadastrar';
  const rotuloNome =
    tipoPessoaAtual === TipoPessoa.JURIDICA ? 'Razão social' : 'Nome';
  const rotuloDoc = tipoPessoaAtual ? rotuloDocumento(tipoPessoaAtual) : 'CPF ou CNPJ';

  const registroTipo = register('tipoPessoa', { onChange: aoMudarTipo });

  return (
    <>
      {serverError && serverError.acao === 'empresa' && (
        <Alert variant="warning" role="alert" className="py-2 small mb-3">
          {serverError.message}{' '}
          <Link to="/empresa">Ir para a tela da empresa</Link>
        </Alert>
      )}
      {serverError && serverError.acao !== 'empresa' && (
        <ErrorAlert
          variant="danger"
          message={serverError.message}
          actionLabel={
            serverError.acao === 'recarregar'
              ? 'Recarregar dados'
              : serverError.acao === 'reenviar'
                ? 'Tentar novamente'
                : undefined
          }
          onAction={
            serverError.acao === 'recarregar' && onRecarregar
              ? onRecarregar
              : serverError.acao === 'reenviar'
                ? () => {
                    setServerError(null);
                    handleSubmit(onSubmit)();
                  }
                : undefined
          }
        />
      )}

      <Form onSubmit={handleSubmit(onSubmit)} noValidate>
        {/* Tipo de pessoa */}
        <fieldset className="mb-3">
          <legend className="form-label fs-6">Tipo de pessoa</legend>
          <div className="d-flex gap-4">
            <Form.Check
              type="radio"
              id="cliente-tipo-fisica"
              label="Pessoa física"
              name="tipoPessoa"
              value={TipoPessoa.FISICA}
              isInvalid={!!errors.tipoPessoa}
              aria-describedby={
                errors.tipoPessoa ? 'cliente-tipo-feedback' : undefined
              }
              disabled={isSaving}
              {...registroTipo}
            />
            <Form.Check
              type="radio"
              id="cliente-tipo-juridica"
              label="Pessoa jurídica"
              name="tipoPessoa"
              value={TipoPessoa.JURIDICA}
              disabled={isSaving}
              {...registroTipo}
            />
          </div>
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="cliente-tipo-feedback"
          >
            {errors.tipoPessoa?.message}
          </Form.Control.Feedback>
        </fieldset>

        {/* Nome (ou razão social) */}
        <Form.Group className="mb-3" controlId="cliente-nome">
          <Form.Label>{rotuloNome}</Form.Label>
          <Form.Control
            type="text"
            autoComplete="off"
            isInvalid={!!errors.nome}
            aria-describedby={
              errors.nome ? 'cliente-nome-feedback' : undefined
            }
            disabled={isSaving}
            {...register('nome')}
          />
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="cliente-nome-feedback"
          >
            {errors.nome?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* CPF ou CNPJ */}
        <Form.Group className="mb-3" controlId="cliente-documento">
          <Form.Label>{rotuloDoc}</Form.Label>
          <DocumentoInput
            control={control}
            name="documento"
            id="cliente-documento"
            tipoPessoa={tipoPessoaAtual || TipoPessoa.FISICA}
            isInvalid={!!errors.documento}
            describedBy={
              errors.documento ? 'cliente-documento-feedback' : undefined
            }
            disabled={isSaving}
          />
          {avisoDocumento && (
            <Form.Text className="d-block" role="status">
              {avisoDocumento}
            </Form.Text>
          )}
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="cliente-documento-feedback"
          >
            {errors.documento?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* E-mail */}
        <Form.Group className="mb-3" controlId="cliente-email">
          <Form.Label>E-mail</Form.Label>
          <Form.Control
            type="email"
            autoComplete="email"
            isInvalid={!!errors.email}
            aria-describedby={
              errors.email ? 'cliente-email-feedback' : undefined
            }
            disabled={isSaving}
            {...register('email')}
          />
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="cliente-email-feedback"
          >
            {errors.email?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Telefone */}
        <Form.Group className="mb-3" controlId="cliente-telefone">
          <Form.Label>Telefone</Form.Label>
          <TelefoneInput
            control={control}
            name="telefone"
            id="cliente-telefone"
            isInvalid={!!errors.telefone}
            describedBy={
              errors.telefone ? 'cliente-telefone-feedback' : undefined
            }
            disabled={isSaving}
          />
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="cliente-telefone-feedback"
          >
            {errors.telefone?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Botões */}
        <div className="d-flex gap-2">
          <Button
            type="submit"
            variant="primary"
            className="d-inline-flex align-items-center gap-2"
            disabled={isSaving}
          >
            {isSaving && (
              <Spinner
                as="span"
                animation="border"
                size="sm"
                role="status"
                aria-hidden="true"
              />
            )}
            {isSaving ? (isEdicao ? 'Salvando…' : 'Cadastrando…') : submitLabel}
          </Button>

          {isEdicao && onCancelar && (
            <Button
              type="button"
              variant="outline-dark"
              onClick={onCancelar}
              disabled={isSaving}
            >
              Cancelar
            </Button>
          )}
        </div>
      </Form>
    </>
  );
}
