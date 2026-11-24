// ============================================================
// PessoaForm — formulário genérico de pessoa (física/jurídica)
// para entidades com tipoPessoa, nome/razão social, CPF/CNPJ
// com máscara, e-mail e telefone (RF-FE-003 e RF-FE-004).
// RHF + zodResolver + tratamento de erro por CODE (RNF-FE-14).
// Documento e telefone vão à API somente com dígitos
// (RNF-FE-28).
// ============================================================
import { useState } from 'react';
import { Link } from 'react-router';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Form, Button, Spinner, Alert } from 'react-bootstrap';
import { pessoaSchema } from '../lib/pessoaSchema.js';
import {
  TipoPessoa,
  maxDigitosDocumento,
  rotuloDocumento,
} from '../lib/tipoPessoa.js';
import DocumentoInput from './DocumentoInput.jsx';
import TelefoneInput from './TelefoneInput.jsx';
import ErrorAlert from './ErrorAlert.jsx';

/**
 * Formulário de cadastro ou edição de uma pessoa
 * (cliente ou fornecedor).
 *
 * @param {object}    props
 * @param {'cadastro'|'edicao'} props.modo
 * @param {object}    [props.registroInicial] — dados atuais (edicao);
 *   inclui `versao` quando a API usa controle de versão
 * @param {string}    [props.entidade='cliente'] — rótulo da entidade
 *   nas mensagens ('cliente' | 'fornecedor')
 * @param {string}    props.codigoDocumentoDuplicado — code do problem+json
 *   de documento já cadastrado (CUSTOMER_DOCUMENT_DUPLICATE,
 *   SUPPLIER_DOCUMENT_DUPLICATE, ...)
 * @param {function}  props.onSubmit — (payload) => Promise<registro salvo>
 * @param {function}  props.onSucesso — (salvo) => void
 * @param {function}  [props.onRecarregar] — recarregar dados (edicao,
 *   em VERSION_CONFLICT)
 * @param {function}  [props.onCancelar]   — cancelar (edicao)
 */
export default function PessoaForm({
  modo,
  registroInicial = null,
  entidade = 'cliente',
  codigoDocumentoDuplicado = 'CUSTOMER_DOCUMENT_DUPLICATE',
  onSubmit,
  onSucesso,
  onRecarregar,
  onCancelar,
}) {
  const isEdicao = modo === 'edicao';

  // { message, acao } — acao: 'recarregar' | 'reenviar' | 'empresa' | null
  // (decisão por código, nunca por texto — RNF-FE-14)
  const [serverError, setServerError] = useState(null);
  const [avisoDocumento, setAvisoDocumento] = useState(null);

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
    resolver: zodResolver(pessoaSchema),
    defaultValues: isEdicao
      ? {
          tipoPessoa: registroInicial?.tipoPessoa || TipoPessoa.FISICA,
          nome: registroInicial?.nome || '',
          documento: registroInicial?.documento || '',
          email: registroInicial?.email || '',
          telefone: registroInicial?.telefone || '',
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
    isEdicao ? registroInicial?.tipoPessoa || TipoPessoa.FISICA : '',
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
   * (RNF-FE-28); opcionais vazios são omitidos; em edição
   * inclui a `versao` do registro.
   */
  function montarPayload(data) {
    const payload = {
      tipoPessoa: data.tipoPessoa,
      nome: data.nome.trim(),
      documento: data.documento,
    };
    if (data.email) payload.email = data.email.trim();
    if (data.telefone) payload.telefone = data.telefone;
    if (isEdicao && registroInicial) payload.versao = registroInicial.versao;
    return payload;
  }

  async function onSubmitHandler(data) {
    setServerError(null);

    try {
      const salvo = await onSubmit(montarPayload(data));
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

      if (err.code === codigoDocumentoDuplicado) {
        setError('documento', {
          message: `Já existe um ${entidade} com este documento. Ele pode estar inativo; busque por ele e reative.`,
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
          message: `O ${entidade} foi alterado por outra sessão. Recarregue os dados para continuar.`,
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
          message: `Você não tem permissão para cadastrar ${entidade}s.`,
          acao: null,
        });
        return;
      }

      // Rede / 5xx
      setServerError({
        message: `Não foi possível salvar o ${entidade}. Verifique sua conexão e tente novamente.`,
        acao: 'reenviar',
      });
    }
  }

  const submitLabel = isEdicao ? 'Salvar' : 'Cadastrar';
  const rotuloNome =
    tipoPessoaAtual === TipoPessoa.JURIDICA ? 'Razão social' : 'Nome';
  const rotuloDoc = tipoPessoaAtual
    ? rotuloDocumento(tipoPessoaAtual)
    : 'CPF ou CNPJ';

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
                    handleSubmit(onSubmitHandler)();
                  }
                : undefined
          }
        />
      )}

      <Form onSubmit={handleSubmit(onSubmitHandler)} noValidate>
        {/* Tipo de pessoa */}
        <fieldset className="mb-3">
          <legend className="form-label fs-6">Tipo de pessoa</legend>
          <div className="d-flex gap-4">
            <Form.Check
              type="radio"
              id="pessoa-tipo-fisica"
              label="Pessoa física"
              name="tipoPessoa"
              value={TipoPessoa.FISICA}
              isInvalid={!!errors.tipoPessoa}
              aria-describedby={
                errors.tipoPessoa ? 'pessoa-tipo-feedback' : undefined
              }
              disabled={isSaving}
              {...registroTipo}
            />
            <Form.Check
              type="radio"
              id="pessoa-tipo-juridica"
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
            id="pessoa-tipo-feedback"
          >
            {errors.tipoPessoa?.message}
          </Form.Control.Feedback>
        </fieldset>

        {/* Nome (ou razão social) */}
        <Form.Group className="mb-3" controlId="pessoa-nome">
          <Form.Label>{rotuloNome}</Form.Label>
          <Form.Control
            type="text"
            autoComplete="off"
            isInvalid={!!errors.nome}
            aria-describedby={errors.nome ? 'pessoa-nome-feedback' : undefined}
            disabled={isSaving}
            {...register('nome')}
          />
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="pessoa-nome-feedback"
          >
            {errors.nome?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* CPF ou CNPJ */}
        <Form.Group className="mb-3" controlId="pessoa-documento">
          <Form.Label>{rotuloDoc}</Form.Label>
          <DocumentoInput
            control={control}
            name="documento"
            id="pessoa-documento"
            tipoPessoa={tipoPessoaAtual || TipoPessoa.FISICA}
            isInvalid={!!errors.documento}
            describedBy={
              errors.documento ? 'pessoa-documento-feedback' : undefined
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
            id="pessoa-documento-feedback"
          >
            {errors.documento?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* E-mail */}
        <Form.Group className="mb-3" controlId="pessoa-email">
          <Form.Label>E-mail</Form.Label>
          <Form.Control
            type="email"
            autoComplete="email"
            isInvalid={!!errors.email}
            aria-describedby={errors.email ? 'pessoa-email-feedback' : undefined}
            disabled={isSaving}
            {...register('email')}
          />
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="pessoa-email-feedback"
          >
            {errors.email?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Telefone */}
        <Form.Group className="mb-3" controlId="pessoa-telefone">
          <Form.Label>Telefone</Form.Label>
          <TelefoneInput
            control={control}
            name="telefone"
            id="pessoa-telefone"
            isInvalid={!!errors.telefone}
            describedBy={
              errors.telefone ? 'pessoa-telefone-feedback' : undefined
            }
            disabled={isSaving}
          />
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="pessoa-telefone-feedback"
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
