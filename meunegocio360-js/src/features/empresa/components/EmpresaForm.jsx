// ============================================================
// EmpresaForm — formulário de cadastro/edição da empresa
// RHF + zodResolver + tratamento de erro por CODE (RNF-FE-14)
// ============================================================
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Form, Button, Spinner } from 'react-bootstrap';
import { empresaSchema } from '../schemas.js';
import { useEmpresaAcoes } from '../index.js';
import { FUSOS_BR, FUSO_PADRAO } from '../../../shared/lib/fusos.js';
import CnpjInput from '../../../shared/ui/CnpjInput.jsx';
import TelefoneInput from '../../../shared/ui/TelefoneInput.jsx';
import ErrorAlert from '../../../shared/ui/ErrorAlert.jsx';

/**
 * Formulário de cadastro ou edição da empresa.
 *
 * @param {object}    props
 * @param {'cadastro'|'edicao'} props.modo
 * @param {object}    [props.empresaInicial]  — dados atuais (edicao)
 * @param {function}  props.onSucesso(dadosEmpresa)
 * @param {function}  [props.onRecarregar]    — callback para recarregar dados
 * @param {function}  [props.onCancelar]      — callback para cancelar edição
 */
export default function EmpresaForm({
  modo,
  empresaInicial,
  onSucesso,
  onRecarregar,
  onCancelar,
}) {
  const isEdicao = modo === 'edicao';

  // { message, acao } — acao: 'recarregar' | 'reenviar' | null (decisão por
  // código, nunca por texto da mensagem — RNF-FE-14)
  const [serverError, setServerError] = useState(null);

  const { cadastrar, atualizar } = useEmpresaAcoes();

  const {
    register,
    handleSubmit,
    setError,
    setFocus,
    control,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(empresaSchema),
    defaultValues: isEdicao
      ? {
          razaoSocial: empresaInicial?.razaoSocial || '',
          nomeFantasia: empresaInicial?.nomeFantasia || '',
          cnpj: empresaInicial?.cnpj || '',
          email: empresaInicial?.email || '',
          telefone: empresaInicial?.telefone || '',
          fusoHorario: empresaInicial?.fusoHorario || FUSO_PADRAO,
          moeda: empresaInicial?.moeda || 'BRL',
        }
      : {
          razaoSocial: '',
          nomeFantasia: '',
          cnpj: '',
          email: '',
          telefone: '',
          fusoHorario: FUSO_PADRAO,
          moeda: 'BRL',
        },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
  });

  const isSaving = isSubmitting;

  /**
   * Monta o payload, removendo opcionais vazios (undefined).
   * CNPJ e telefone já são apenas dígitos.
   */
  function montarPayload(data) {
    const payload = { ...data };
    // Opcionais vazios são omitidos
    if (!payload.nomeFantasia) delete payload.nomeFantasia;
    if (!payload.email) delete payload.email;
    if (!payload.telefone) delete payload.telefone;

    if (isEdicao && empresaInicial) {
      payload.versao = empresaInicial.versao;
    }

    return payload;
  }

  async function onSubmit(data) {
    setServerError(null);

    try {
      const payload = montarPayload(data);

      let dadosEmpresa;
      if (isEdicao) {
        dadosEmpresa = await atualizar.mutateAsync({ id: empresaInicial.id, payload });
      } else {
        dadosEmpresa = await cadastrar.mutateAsync(payload);
      }

      onSucesso(dadosEmpresa);
    } catch (err) {
      // Tratamento por CODE (RNF-FE-14)
      if (err.code === 'VALIDATION_ERROR' && err.errors?.length) {
        for (const fe of err.errors) {
          setError(fe.field, { message: fe.message });
        }
        // Foco no primeiro campo inválido (timeout: o estado de erros ainda
        // não foi processado no submit assíncrono)
        const primeiro = err.errors[0].field;
        if (primeiro) {
          setTimeout(() => {
            try {
              setFocus(primeiro);
            } catch {
              // Campo pode não existir no formulário — ignora
            }
          }, 100);
        }
        return;
      }

      if (err.code === 'COMPANY_CNPJ_DUPLICATE') {
        setError('cnpj', {
          message: 'Já existe uma empresa cadastrada com este CNPJ.',
        });
        setTimeout(() => {
          try {
            setFocus('cnpj');
          } catch {
            // ignora
          }
        }, 100);
        return;
      }

      if (err.code === 'COMPANY_ALREADY_ACTIVE') {
        setServerError({
          message: 'Já existe uma empresa ativa. Inative-a antes de cadastrar ou reativar outra.',
          acao: null,
        });
        return;
      }

      if (err.code === 'VERSION_CONFLICT') {
        setServerError({
          message: 'A empresa foi alterada por outra sessão. Recarregue os dados para continuar.',
          acao: 'recarregar',
        });
        // O formulário MANTÉM o que foi digitado até a escolha
        return;
      }

      if (err.code === 'ACCESS_DENIED') {
        setServerError({
          message: 'Você não tem permissão para alterar os dados da empresa.',
          acao: null,
        });
        return;
      }

      // Rede / 5xx
      setServerError({
        message: 'Não foi possível salvar os dados. Verifique sua conexão e tente novamente.',
        acao: 'reenviar',
      });
    }
  }

  const submitLabel = isEdicao ? 'Salvar' : 'Cadastrar';

  return (
    <>
      {serverError && (
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
        {/* Razão social */}
        <Form.Group className="mb-3" controlId="empresa-razao-social">
          <Form.Label>Razão social</Form.Label>
          <Form.Control
            type="text"
            autoComplete="organization"
            isInvalid={!!errors.razaoSocial}
            aria-describedby={errors.razaoSocial ? 'empresa-razao-social-feedback' : undefined}
            {...register('razaoSocial')}
            disabled={isSaving}
          />
          <Form.Control.Feedback type="invalid" role="alert" id="empresa-razao-social-feedback">
            {errors.razaoSocial?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Nome fantasia */}
        <Form.Group className="mb-3" controlId="empresa-nome-fantasia">
          <Form.Label>Nome fantasia</Form.Label>
          <Form.Control
            type="text"
            isInvalid={!!errors.nomeFantasia}
            aria-describedby={errors.nomeFantasia ? 'empresa-nome-fantasia-feedback' : undefined}
            {...register('nomeFantasia')}
            disabled={isSaving}
          />
          <Form.Control.Feedback type="invalid" role="alert" id="empresa-nome-fantasia-feedback">
            {errors.nomeFantasia?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* CNPJ */}
        <Form.Group className="mb-3" controlId="empresa-cnpj">
          <Form.Label>CNPJ</Form.Label>
          <CnpjInput
            control={control}
            name="cnpj"
            id="empresa-cnpj"
            isInvalid={!!errors.cnpj}
            describedBy={errors.cnpj ? 'empresa-cnpj-feedback' : undefined}
            disabled={isSaving}
          />
          <Form.Control.Feedback type="invalid" role="alert" id="empresa-cnpj-feedback">
            {errors.cnpj?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* E-mail */}
        <Form.Group className="mb-3" controlId="empresa-email">
          <Form.Label>E-mail</Form.Label>
          <Form.Control
            type="email"
            autoComplete="email"
            isInvalid={!!errors.email}
            aria-describedby={errors.email ? 'empresa-email-feedback' : undefined}
            {...register('email')}
            disabled={isSaving}
          />
          <Form.Control.Feedback type="invalid" role="alert" id="empresa-email-feedback">
            {errors.email?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Telefone */}
        <Form.Group className="mb-3" controlId="empresa-telefone">
          <Form.Label>Telefone</Form.Label>
          <TelefoneInput
            control={control}
            name="telefone"
            id="empresa-telefone"
            isInvalid={!!errors.telefone}
            describedBy={errors.telefone ? 'empresa-telefone-feedback' : undefined}
            disabled={isSaving}
          />
          <Form.Control.Feedback type="invalid" role="alert" id="empresa-telefone-feedback">
            {errors.telefone?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Fuso horário */}
        <Form.Group className="mb-3" controlId="empresa-fuso">
          <Form.Label>Fuso horário</Form.Label>
          <Form.Select
            isInvalid={!!errors.fusoHorario}
            aria-describedby={errors.fusoHorario ? 'empresa-fuso-feedback' : undefined}
            {...register('fusoHorario')}
            disabled={isSaving}
          >
            {FUSOS_BR.map((f) => (
              <option key={f.id} value={f.id}>
                {f.label}
              </option>
            ))}
          </Form.Select>
          <Form.Control.Feedback type="invalid" role="alert" id="empresa-fuso-feedback">
            {errors.fusoHorario?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Moeda */}
        <Form.Group className="mb-3" controlId="empresa-moeda">
          <Form.Label>Moeda</Form.Label>
          <Form.Select
            disabled
            {...register('moeda')}
          >
            <option value="BRL">BRL</option>
          </Form.Select>
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
