// ============================================================
// ItemForm — formulário de cadastro/edição de item do catálogo
// (RF-FE-005). RHF + zodResolver + tratamento de erro por
// CODE (RNF-FE-14). Preço decimal em string (RNF-033).
// Não há campo de estoque mínimo (fora do escopo, RN-07).
// ============================================================
import { useState } from 'react';
import { Link } from 'react-router';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Form, Button, Spinner, Alert } from 'react-bootstrap';
import { itemSchema } from '../schemas.js';
import { useItemAcoes } from '../hooks/useItemAcoes.js';
import { useCategoriasAtivas } from '../hooks/useCategorias.js';
import {
  TipoItem,
  unidadePadrao,
} from '../lib/tipoItem.js';
import { normalizarPreco } from '../../../shared/lib/moeda.js';
import PrecoInput from '../../../shared/ui/PrecoInput.jsx';
import ErrorAlert from '../../../shared/ui/ErrorAlert.jsx';

/**
 * @param {object}    props
 * @param {'cadastro'|'edicao'} props.modo
 * @param {object}    [props.itemInicial] — dados atuais (edicao)
 * @param {function}  props.onSucesso
 * @param {function}  [props.onRecarregar] — recarregar (edicao)
 * @param {function}  [props.onCancelar]   — cancelar (edicao)
 */
export default function ItemForm({
  modo,
  itemInicial,
  onSucesso,
  onRecarregar,
  onCancelar,
}) {
  const isEdicao = modo === 'edicao';

  // { message, acao } — acao: 'recarregar' | 'reenviar' | 'empresa' | null
  const [serverError, setServerError] = useState(null);
  // Na edição o campo Tipo fica desabilitado (tipo imutável, RN-02)
  const [tamanhoDescricao, setTamanhoDescricao] = useState(
    (itemInicial?.descricao || '').length,
  );

  const { cadastrar, atualizar } = useItemAcoes();
  const {
    data: categoriasAtivas,
    refetch: recarregarCategorias,
  } = useCategoriasAtivas();

  const {
    register,
    handleSubmit,
    setError,
    setFocus,
    setValue,
    getValues,
    control,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(itemSchema),
    defaultValues: isEdicao
      ? {
          tipo: itemInicial?.tipo || '',
          codigo: itemInicial?.codigo || '',
          nome: itemInicial?.nome || '',
          descricao: itemInicial?.descricao || '',
          unidade: itemInicial?.unidade || '',
          preco: itemInicial?.preco || '',
          categoriaId: itemInicial?.categoria?.id || '',
        }
      : {
          tipo: '',
          codigo: '',
          nome: '',
          descricao: '',
          unidade: '',
          preco: '',
          categoriaId: '',
        },
    mode: 'onSubmit',
    reValidateMode: 'onChange',
  });

  const isSaving = isSubmitting;
  const categorias = categoriasAtivas || [];

  // Na edição, a categoria atual pode estar inativa (RN-05):
  // ela não aparece na lista de ativas, então é incluída
  // separadamente para o select exibir o valor atual.
  const categoriaAtual = itemInicial?.categoria;
  const categoriaAtualForaDaLista =
    isEdicao &&
    categoriaAtual &&
    !categorias.some((c) => c.id === categoriaAtual.id);

  /**
   * Ao trocar o tipo na criação, a unidade padrão acompanha
   * o novo tipo, desde que o usuário não tenha alterado
   * o campo (RF-FE-005, seção Formulário).
   */
  function aoMudarTipo(e) {
    const novoTipo = e.target.value;
    if (!isEdicao) {
      const unidadeAtual = (getValues('unidade') || '').trim();
      if (!unidadeAtual) {
        setValue('unidade', unidadePadrao(novoTipo));
      }
    }
  }

  /**
   * Monta o payload: unidade em maiúsculas, preço normalizado
   * para decimal em string com 2 casas (RNF-033); opcionais
   * vazios são omitidos; em edição inclui a `versao`.
   */
  function montarPayload(data) {
    const payload = {
      tipo: data.tipo,
      nome: data.nome.trim(),
      unidade: (data.unidade || '').trim().toUpperCase(),
      preco: normalizarPreco(data.preco),
    };
    if (data.codigo) payload.codigo = data.codigo.trim();
    if (data.descricao) payload.descricao = data.descricao.trim();
    if (data.categoriaId) payload.categoriaId = data.categoriaId;
    if (isEdicao && itemInicial) payload.versao = itemInicial.versao;
    return payload;
  }

  function focarCampo(nome) {
    setTimeout(() => {
      try {
        setFocus(nome);
      } catch {
        // campo pode não existir no formulário — ignora
      }
    }, 100);
  }

  async function onSubmit(data) {
    setServerError(null);

    try {
      const payload = montarPayload(data);
      const salvo = isEdicao
        ? await atualizar.mutateAsync({ id: itemInicial.id, payload })
        : await cadastrar.mutateAsync(payload);
      onSucesso(salvo);
    } catch (err) {
      if (err.code === 'VALIDATION_ERROR' && err.errors?.length) {
        for (const fe of err.errors) {
          setError(fe.field, { message: fe.message });
        }
        const primeiro = err.errors[0]?.field;
        if (primeiro) focarCampo(primeiro);
        return;
      }

      if (err.code === 'ITEM_CODE_DUPLICATE') {
        setError('codigo', {
          message: 'Já existe um item com este código.',
        });
        focarCampo('codigo');
        return;
      }

      if (err.code === 'CATEGORY_INVALID') {
        setError('categoriaId', {
          message: 'Selecione uma categoria ativa.',
        });
        focarCampo('categoriaId');
        // Recarrega a lista de categorias ativas (RNF-FE-04)
        recarregarCategorias();
        return;
      }

      if (err.code === 'ITEM_TYPE_IMMUTABLE') {
        setServerError({
          message: 'O tipo do item não pode ser alterado depois de criado.',
          acao: null,
        });
        return;
      }

      if (err.code === 'VERSION_CONFLICT') {
        setServerError({
          message:
            'O item foi alterado por outra sessão. Recarregue os dados para continuar.',
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
          message: 'Você não tem permissão para gerenciar o catálogo.',
          acao: null,
        });
        return;
      }

      // Rede / 5xx
      setServerError({
        message:
          'Não foi possível salvar o item. Verifique sua conexão e tente novamente.',
        acao: 'reenviar',
      });
    }
  }

  const submitLabel = isEdicao ? 'Salvar' : 'Cadastrar';

  const registroTipo = register('tipo', { onChange: aoMudarTipo });

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
        {/* Tipo (desabilitado na edição — RN-02) */}
        <fieldset className="mb-3" disabled={isEdicao || isSaving}>
          <legend className="form-label fs-6">Tipo</legend>
          <div className="d-flex gap-4">
            <Form.Check
              type="radio"
              id="item-tipo-produto"
              label="Produto"
              name="tipo"
              value={TipoItem.PRODUTO}
              isInvalid={!!errors.tipo}
              aria-describedby={errors.tipo ? 'item-tipo-feedback' : undefined}
              {...registroTipo}
            />
            <Form.Check
              type="radio"
              id="item-tipo-servico"
              label="Serviço"
              name="tipo"
              value={TipoItem.SERVICO}
              {...registroTipo}
            />
          </div>
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="item-tipo-feedback"
          >
            {errors.tipo?.message}
          </Form.Control.Feedback>
        </fieldset>

        {/* Código */}
        <Form.Group className="mb-3" controlId="item-codigo">
          <Form.Label>Código</Form.Label>
          <Form.Control
            type="text"
            maxLength={40}
            autoComplete="off"
            isInvalid={!!errors.codigo}
            aria-describedby={errors.codigo ? 'item-codigo-feedback' : undefined}
            disabled={isSaving}
            {...register('codigo')}
          />
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="item-codigo-feedback"
          >
            {errors.codigo?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Nome */}
        <Form.Group className="mb-3" controlId="item-nome">
          <Form.Label>Nome</Form.Label>
          <Form.Control
            type="text"
            autoComplete="off"
            isInvalid={!!errors.nome}
            aria-describedby={errors.nome ? 'item-nome-feedback' : undefined}
            disabled={isSaving}
            {...register('nome')}
          />
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="item-nome-feedback"
          >
            {errors.nome?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Descrição (com contador) */}
        <Form.Group className="mb-3" controlId="item-descricao">
          <Form.Label>Descrição</Form.Label>
          <Form.Control
            as="textarea"
            rows={3}
            isInvalid={!!errors.descricao}
            aria-describedby={[
              errors.descricao ? 'item-descricao-feedback' : '',
              'item-descricao-contador',
            ]
              .filter(Boolean)
              .join(' ')}
            disabled={isSaving}
            {...register('descricao', {
              onChange: (e) => setTamanhoDescricao(e.target.value.length),
            })}
          />
          <Form.Text
            id="item-descricao-contador"
            className="text-end"
          >
            {tamanhoDescricao}/500
          </Form.Text>
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="item-descricao-feedback"
          >
            {errors.descricao?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Categoria (opcionais + "Sem categoria") */}
        <Form.Group className="mb-3" controlId="item-categoria">
          <Form.Label>Categoria</Form.Label>
          <Form.Control
            as="select"
            isInvalid={!!errors.categoriaId}
            aria-describedby={
              errors.categoriaId ? 'item-categoria-feedback' : undefined
            }
            disabled={isSaving}
            {...register('categoriaId')}
          >
            <option value="">Sem categoria</option>
            {categorias.map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
            {categoriaAtualForaDaLista && (
              <option value={categoriaAtual.id}>
                {categoriaAtual.nome} (inativa)
              </option>
            )}
          </Form.Control>
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="item-categoria-feedback"
          >
            {errors.categoriaId?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Unidade (maiúsculas) */}
        <Form.Group className="mb-3" controlId="item-unidade">
          <Form.Label>Unidade</Form.Label>
          <Form.Control
            type="text"
            maxLength={6}
            autoComplete="off"
            className="text-uppercase"
            isInvalid={!!errors.unidade}
            aria-describedby={errors.unidade ? 'item-unidade-feedback' : undefined}
            disabled={isSaving}
            {...register('unidade')}
          />
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="item-unidade-feedback"
          >
            {errors.unidade?.message}
          </Form.Control.Feedback>
        </Form.Group>

        {/* Preço */}
        <Form.Group className="mb-3" controlId="item-preco">
          <Form.Label>Preço (R$)</Form.Label>
          <PrecoInput
            control={control}
            name="preco"
            id="item-preco"
            isInvalid={!!errors.preco}
            describedBy={errors.preco ? 'item-preco-feedback' : undefined}
            disabled={isSaving}
          />
          <Form.Control.Feedback
            type="invalid"
            role="alert"
            id="item-preco-feedback"
          >
            {errors.preco?.message}
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
