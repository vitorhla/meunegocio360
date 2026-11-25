// ============================================================
// EmpresaPage — visualização/edição da empresa (rota /empresa)
// Rota com acesso "Autenticado" (tabela de rotas do RF-FE-002):
// a própria página trata 'carregando'/'erro'/'ausente'.
// ADMIN vê botões Editar, Inativar/Reativar.
// Sem empresa ativa: o ADMIN vê as inativas para reativar
// (ou cadastrar nova); os demais perfis veem um aviso.
// Demais perfis: somente leitura.
// ============================================================
import { useState } from 'react';
import { Alert } from 'react-bootstrap';
import { Link } from 'react-router';
import { useQuery } from '@tanstack/react-query';
import { useAuth } from '../../auth/index.js';
import { useEmpresa, useEmpresaAcoes } from '../index.js';
import { Perfis } from '../../../shared/lib/perfis.js';
import usePageTitle from '../../../shared/lib/usePageTitle.js';
import LoadingSpinner from '../../../shared/ui/LoadingSpinner.jsx';
import ErrorScreen from '../../../shared/ui/ErrorScreen.jsx';
import ConfirmModal from '../../../shared/ui/ConfirmModal.jsx';
import ErrorAlert from '../../../shared/ui/ErrorAlert.jsx';
import EmpresaDados from '../components/EmpresaDados.jsx';
import EmpresaForm from '../components/EmpresaForm.jsx';
import EmpresasInativas from '../components/EmpresasInativas.jsx';
import { listarEmpresas } from '../api/empresaApi.js';

export default function EmpresaPage() {
  const { usuario } = useAuth();
  const { status, erro, empresa, temInativa, refetch } = useEmpresa();
  const { mudarSituacao } = useEmpresaAcoes();

  const isAdmin = usuario?.perfil === Perfis.ADMIN;

  // "Aguardando a empresa" só para não-ADMIN sem empresa ativa
  usePageTitle(
    status === 'ausente' && !isAdmin ? 'Aguardando a empresa' : 'Empresa',
  );

  const [modoEdicao, setModoEdicao] = useState(false);
  const [successMessage, setSuccessMessage] = useState(null);
  const [actionError, setActionError] = useState(null);
  const [showInativarModal, setShowInativarModal] = useState(false);
  const [showReativarModal, setShowReativarModal] = useState(false);
  const [empresaReativar, setEmpresaReativar] = useState(null);

  // Lista de empresas inativas: só para ADMIN sem empresa ativa
  const {
    data: empresas,
    isPending: listaPendente,
    isError: listaErro,
    error: listaErr,
    refetch: refetchLista,
  } = useQuery({
    queryKey: ['empresas'],
    queryFn: listarEmpresas,
    enabled: status === 'ausente' && isAdmin,
    retry: false,
  });
  const empresasInativas = (empresas || []).filter((e) => e.ativa === false);

  function voltarLeitura() {
    setModoEdicao(false);
    setActionError(null);
  }

  function handleInativar() {
    setActionError(null);
    mudarSituacao.mutate(
      { id: empresa.id, ativa: false, versao: empresa.versao },
      {
        onSuccess: () => {
          setShowInativarModal(false);
        },
        onError: (err) => {
          if (err.code === 'ACCESS_DENIED') {
            setActionError('Você não tem permissão para alterar os dados da empresa.');
          } else {
            setActionError('Não foi possível alterar a situação da empresa. Tente novamente.');
          }
        },
      },
    );
  }

  function handleReativar() {
    setActionError(null);
    mudarSituacao.mutate(
      { id: empresa.id, ativa: true, versao: empresa.versao },
      {
        onSuccess: () => {
          setShowReativarModal(false);
        },
        onError: (err) => {
          if (err.code === 'COMPANY_ALREADY_ACTIVE') {
            setActionError('Já existe uma empresa ativa. Inative-a antes de cadastrar ou reativar outra.');
          } else if (err.code === 'ACCESS_DENIED') {
            setActionError('Você não tem permissão para alterar os dados da empresa.');
          } else {
            setActionError('Não foi possível alterar a situação da empresa. Tente novamente.');
          }
        },
      },
    );
  }

  // Reativa uma empresa inativa listada (tela "sem empresa ativa", ADMIN)
  function handleReativarInativa(emp) {
    if (!emp) return;
    setActionError(null);
    mudarSituacao.mutate(
      { id: emp.id, ativa: true, versao: emp.versao },
      {
        onSuccess: () => {
          // O cache ['empresa'] é atualizado na mutação: o status vira 'ativa'
          // e a página passa a exibir a empresa reativada.
          setEmpresaReativar(null);
        },
        onError: (err) => {
          if (err.code === 'COMPANY_ALREADY_ACTIVE') {
            setActionError('Já existe uma empresa ativa. Inative-a antes de reativar outra.');
          } else if (err.code === 'VERSION_CONFLICT') {
            setActionError('A lista de empresas está desatualizada. Atualize e tente novamente.');
            refetchLista();
          } else if (err.code === 'ACCESS_DENIED') {
            setActionError('Você não tem permissão para alterar a situação da empresa.');
          } else {
            setActionError('Não foi possível reativar a empresa. Tente novamente.');
          }
        },
      },
    );
  }

  // --- Estados de resolução da empresa (sem RequireEmpresa nesta rota) ---
  if (status === 'carregando') {
    return <LoadingSpinner text="Carregando empresa…" />;
  }

  if (status === 'erro') {
    return (
      <ErrorScreen
        message={erro?.detail || 'Não foi possível carregar os dados da empresa.'}
        onRetry={refetch}
      />
    );
  }

  if (status === 'ausente') {
    // ADMIN sem empresa ativa: lista as inativas para reativar,
    // e mantém a opção de cadastrar uma nova.
    if (isAdmin) {
      return (
        <div className="py-3">
          <h1 className="fs-2 mb-2">Sem empresa ativa</h1>
          <p className="text-secondary mb-4">
            Reative uma das empresas inativas para voltar a usar o sistema ou
            cadastre uma nova.
          </p>

          {listaErro && (
            <ErrorAlert
              variant="danger"
              message={
                listaErr?.detail ||
                'Não foi possível carregar as empresas inativas.'
              }
            />
          )}

          {!listaErro && listaPendente && (
            <LoadingSpinner text="Carregando empresas…" />
          )}

          {!listaErro && !listaPendente && (
            <div>
              {empresasInativas.length > 0 ? (
                <EmpresasInativas
                  empresas={empresasInativas}
                  ocupado={mudarSituacao.isPending}
                  onReativar={setEmpresaReativar}
                />
              ) : (
                <p className="text-secondary">
                  Nenhuma empresa inativa encontrada.
                </p>
              )}

              {actionError && (
                <ErrorAlert variant="danger" message={actionError} />
              )}

              <div className="d-flex gap-2 mt-4">
                <Link to="/empresa/cadastro" className="btn btn-primary">
                  Cadastrar empresa
                </Link>
                <button
                  type="button"
                  className="btn btn-outline-secondary"
                  onClick={() => refetchLista()}
                >
                  Atualizar
                </button>
              </div>
            </div>
          )}

          <ConfirmModal
            show={empresaReativar !== null}
            title="Reativar empresa"
            message={
              empresaReativar
                ? `Reativar a empresa "${empresaReativar.razaoSocial}"?`
                : ''
            }
            confirmLabel="Reativar"
            variant="primary"
            busy={mudarSituacao.isPending}
            onConfirm={() => handleReativarInativa(empresaReativar)}
            onCancel={() => setEmpresaReativar(null)}
          />
        </div>
      );
    }

    return (
      <div className="min-vh-100 d-flex flex-column align-items-center justify-content-center px-3 text-center">
        <h1 className="fs-2 mb-3">
          {temInativa
            ? 'Empresa inativa'
            : 'Aguardando o cadastro da empresa'}
        </h1>
        <p className="text-secondary mb-4">
          {temInativa
            ? 'Há empresa inativa no sistema. Fale com o administrador para reativá-la.'
            : 'A empresa ainda não foi cadastrada. Fale com o administrador para realizar o cadastro inicial.'}
        </p>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => refetch()}
        >
          Tentar novamente
        </button>
      </div>
    );
  }

  // --- Modo edição ---
  if (modoEdicao) {
    return (
      <div>
        <h1 className="fs-2 mb-4">Editar empresa</h1>

        {successMessage && (
          <Alert variant="success" role="alert" className="py-2 small mb-3">
            {successMessage}
          </Alert>
        )}

        {actionError && (
          <ErrorAlert variant="danger" message={actionError} />
        )}

        <div className="row">
          <div className="col-md-8 col-lg-6">
            <EmpresaForm
              modo="edicao"
              empresaInicial={empresa}
              onSucesso={() => {
                setSuccessMessage('Dados da empresa atualizados.');
                setModoEdicao(false);
              }}
              onRecarregar={() => {
                // Recarrega os dados (nova versao) e sai do modo edição
                refetch();
                voltarLeitura();
              }}
              onCancelar={voltarLeitura}
            />
          </div>
        </div>
      </div>
    );
  }

  // --- Modo leitura ---
  return (
    <div>
      <h1 className="fs-2 mb-4">Empresa</h1>

      {successMessage && (
        <Alert variant="success" role="alert" className="py-2 small mb-3">
          {successMessage}
        </Alert>
      )}

      {actionError && (
        <ErrorAlert variant="danger" message={actionError} />
      )}

      <EmpresaDados empresa={empresa} />

      {isAdmin && (
        <div className="d-flex gap-2 mt-4">
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => {
              setActionError(null);
              setSuccessMessage(null);
              setModoEdicao(true);
            }}
          >
            Editar
          </button>

          {empresa?.ativa === false ? (
            <button
              type="button"
              className="btn btn-outline-danger"
              onClick={() => setShowReativarModal(true)}
            >
              Reativar
            </button>
          ) : (
            <button
              type="button"
              className="btn btn-outline-danger"
              onClick={() => setShowInativarModal(true)}
            >
              Inativar
            </button>
          )}
        </div>
      )}

      {/* Modal de inativação */}
      <ConfirmModal
        show={showInativarModal}
        title="Inativar empresa"
        message="Ao inativar a empresa, não será possível cadastrar clientes, fornecedores, produtos nem vendas até que uma empresa seja ativada."
        confirmLabel="Inativar"
        variant="danger"
        busy={mudarSituacao.isPending}
        onConfirm={handleInativar}
        onCancel={() => setShowInativarModal(false)}
      />

      {/* Modal de reativação */}
      <ConfirmModal
        show={showReativarModal}
        title="Reativar empresa"
        message="Reativar a empresa?"
        confirmLabel="Reativar"
        variant="primary"
        busy={mudarSituacao.isPending}
        onConfirm={handleReativar}
        onCancel={() => setShowReativarModal(false)}
      />
    </div>
  );
}
