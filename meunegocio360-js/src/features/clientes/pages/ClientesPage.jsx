// ============================================================
// ClientesPage — lista de clientes (rota /clientes, RF-FE-003)
// Busca, filtro de situação e paginação na URL (RNF-FE-27).
// Estados: carregamento, vazio (com/sem filtro), erro, sucesso
// (RNF-062).
// ============================================================
import { useEffect, useState } from 'react';
import { Alert, Form, Button } from 'react-bootstrap';
import { Link, useLocation } from 'react-router';
import { useClientes } from '../hooks/useClientes.js';
import { useClienteAcoes } from '../hooks/useClienteAcoes.js';
import usePageTitle from '../../../shared/lib/usePageTitle.js';
import useUrlFilters from '../../../shared/lib/useUrlFilters.js';
import { maskDocumento } from '../../../shared/lib/tipoPessoa.js';
import { maskTelefone } from '../../../shared/lib/telefone.js';
import ListToolbar from '../../../shared/ui/ListToolbar.jsx';
import Paginacao from '../../../shared/ui/Paginacao.jsx';
import StatusBadge from '../../../shared/ui/StatusBadge.jsx';
import LoadingSpinner from '../../../shared/ui/LoadingSpinner.jsx';
import ErrorScreen from '../../../shared/ui/ErrorScreen.jsx';
import ConfirmModal from '../../../shared/ui/ConfirmModal.jsx';
import ErrorAlert from '../../../shared/ui/ErrorAlert.jsx';

const FILTROS_PADRAO = { q: '', situacao: 'ATIVO' };

export default function ClientesPage() {
  usePageTitle('Clientes');
  const location = useLocation();

  const { filters, page, setFilter, setPage, clearFilters, isFiltered } =
    useUrlFilters(FILTROS_PADRAO);
  const { data, isPending, isError, error, refetch } = useClientes({
    q: filters.q,
    situacao: filters.situacao,
    page,
  });
  const { mudarSituacao, excluir } = useClienteAcoes();

  const [sucesso, setSucesso] = useState(location.state?.sucesso || null);
  const [erroAcao, setErroAcao] = useState(null);
  // { tipo: 'inativar' | 'excluir', cliente } — reativar é direto
  const [acaoPendente, setAcaoPendente] = useState(null);
  const [erroModal, setErroModal] = useState(null);

  // Página que ficou vazia (ex.: após excluir o último item)
  // volta para a primeira página
  useEffect(() => {
    if (data && data.content.length === 0 && page > 0 && data.totalElements > 0) {
      setPage(0);
    }
  }, [data, page, setPage]);

  function abrirAcao(tipo, cliente) {
    setErroModal(null);
    setAcaoPendente({ tipo, cliente });
  }

  function fecharModal() {
    if (mudarSituacao.isPending || excluir.isPending) return;
    setAcaoPendente(null);
    setErroModal(null);
  }

  async function confirmarAcao() {
    const { tipo, cliente } = acaoPendente;
    try {
      if (tipo === 'inativar') {
        await mudarSituacao.mutateAsync({
          id: cliente.id,
          ativo: false,
          versao: cliente.versao,
        });
        setSucesso('Cliente inativado.');
      } else if (tipo === 'reativar') {
        await mudarSituacao.mutateAsync({
          id: cliente.id,
          ativo: true,
          versao: cliente.versao,
        });
        setSucesso('Cliente reativado.');
      } else {
        await excluir.mutateAsync(cliente.id);
        setSucesso('Cliente excluído.');
      }
      setAcaoPendente(null);
      setErroModal(null);
    } catch (err) {
      // Tratamento por CODE (RNF-FE-14)
      if (tipo === 'excluir' && err.code === 'CUSTOMER_HAS_HISTORY') {
        // Erro de negócio: modal permanece aberto com mensagem clara
        setErroModal({
          code: 'CUSTOMER_HAS_HISTORY',
          message:
            'Este cliente possui vendas ou ordens de serviço vinculadas e não pode ser excluído. Você pode inativá-lo.',
        });
        return;
      }
      if (tipo === 'excluir' && err.code === 'CUSTOMER_NOT_FOUND') {
        setAcaoPendente(null);
        setErroModal(null);
        setSucesso('Este cliente não existe mais.');
        return;
      }
      if (err.code === 'VERSION_CONFLICT') {
        // Recarrega a lista com a versao atualizada
        refetch();
        setAcaoPendente(null);
        setErroModal(null);
        setErroAcao(
          'O cliente foi alterado por outra sessão. A lista foi recarregada.',
        );
        return;
      }
      // Rede / 5xx / demais: mantém o modal aberto para nova tentativa
      setErroModal({
        code: 'GENERIC',
        message:
          tipo === 'excluir'
            ? 'Não foi possível excluir o cliente. Tente novamente.'
            : 'Não foi possível concluir a operação. Tente novamente.',
      });
    }
  }

  return (
    <div>
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h1 className="fs-2 mb-0">Clientes</h1>
        <Link to="/clientes/novo" className="btn btn-primary">
          Novo cliente
        </Link>
      </div>

      {sucesso && (
        <Alert variant="success" role="alert" className="py-2 small mb-3">
          {sucesso}
        </Alert>
      )}
      {erroAcao && <ErrorAlert variant="danger" message={erroAcao} />}

      <ListToolbar
        searchInputId="busca-clientes"
        searchLabel="Busca"
        searchPlaceholder="Buscar por nome ou documento"
        searchValue={filters.q}
        onSearchChange={(v) => setFilter('q', v)}
      >
        <Form.Group controlId="filtro-situacao" className="mb-0">
          <Form.Label className="small">Situação</Form.Label>
          <Form.Select
            value={filters.situacao}
            onChange={(e) => setFilter('situacao', e.target.value)}
          >
            <option value="ATIVO">Ativos</option>
            <option value="INATIVO">Inativos</option>
            <option value="TODOS">Todos</option>
          </Form.Select>
        </Form.Group>
      </ListToolbar>

      {isPending && !data ? (
        <LoadingSpinner text="Carregando clientes…" />
      ) : isError ? (
        <ErrorScreen
          message={error?.detail || 'Não foi possível carregar os clientes.'}
          onRetry={refetch}
        />
      ) : data.content.length === 0 ? (
        <div className="text-center py-5">
          {isFiltered ? (
            <>
              <p className="text-secondary mb-3">
                Nenhum cliente encontrado para a busca.
              </p>
              <Button variant="outline-dark" onClick={clearFilters}>
                Limpar filtros
              </Button>
            </>
          ) : (
            <>
              <p className="text-secondary mb-3">
                Nenhum cliente cadastrado. Cadastre o primeiro cliente.
              </p>
              <Link to="/clientes/novo" className="btn btn-primary">
                Novo cliente
              </Link>
            </>
          )}
        </div>
      ) : (
        <>
          <div className="table-responsive">
            <table className="table align-middle">
              <caption className="visually-hidden">Lista de clientes</caption>
              <thead>
                <tr>
                  <th scope="col">Nome</th>
                  <th scope="col">Documento</th>
                  <th scope="col">Telefone</th>
                  <th scope="col">E-mail</th>
                  <th scope="col">Situação</th>
                  <th scope="col">Ações</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((c) => (
                  <tr key={c.id}>
                    <th scope="row">{c.nome}</th>
                    <td>{maskDocumento(c.tipoPessoa, c.documento)}</td>
                    <td>{c.telefone ? maskTelefone(c.telefone) : '—'}</td>
                    <td>{c.email || '—'}</td>
                    <td>
                      <StatusBadge ativo={c.ativo} />
                    </td>
                    <td>
                      <div className="d-flex gap-1 flex-wrap">
                        <Link
                          className="btn btn-sm btn-outline-dark"
                          to={`/clientes/${c.id}`}
                        >
                          Editar
                        </Link>
                        <button
                          type="button"
                          className="btn btn-sm btn-outline-dark"
                          onClick={() =>
                            abrirAcao(c.ativo ? 'inativar' : 'reativar', c)
                          }
                        >
                          {c.ativo ? 'Inativar' : 'Reativar'}
                        </button>
                        <button
                          type="button"
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => abrirAcao('excluir', c)}
                        >
                          Excluir
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginacao
            page={data.page}
            size={data.size}
            totalElements={data.totalElements}
            totalPages={data.totalPages}
            onChange={setPage}
          />
        </>
      )}

      {/* Modais de confirmação (inativar/excluir); reativar é direto */}
      <ConfirmModal
        show={
          acaoPendente?.tipo === 'inativar' || acaoPendente?.tipo === 'excluir'
        }
        title={
          acaoPendente?.tipo === 'inativar' ? 'Inativar cliente' : 'Excluir cliente'
        }
        message={
          acaoPendente?.tipo === 'inativar'
            ? `Inativar o cliente ${acaoPendente?.cliente?.nome}? Ele sai da lista de ativos e poderá ser reativado depois.`
            : `Excluir o cliente ${acaoPendente?.cliente?.nome}? Esta ação não pode ser desfeita.`
        }
        confirmLabel={acaoPendente?.tipo === 'inativar' ? 'Inativar' : 'Excluir'}
        variant="danger"
        busy={mudarSituacao.isPending || excluir.isPending}
        onConfirm={confirmarAcao}
        onCancel={fecharModal}
        alert={erroModal?.message}
        extraAction={
          erroModal?.code === 'CUSTOMER_HAS_HISTORY' &&
          acaoPendente?.tipo === 'excluir'
            ? {
                label: 'Inativar cliente',
                onClick: () => {
                  // Troca o modal para a confirmação de inativação
                  setAcaoPendente({
                    tipo: 'inativar',
                    cliente: acaoPendente.cliente,
                  });
                  setErroModal(null);
                },
              }
            : undefined
        }
      />
    </div>
  );
}
