// ============================================================
// FornecedoresPage — lista de fornecedores (rota /fornecedores,
// RF-FE-004)
// Busca, filtro de situação e paginação na URL (RNF-FE-27).
// Estados: carregamento, vazio (com/sem filtro), erro, sucesso
// (RNF-062). Reutiliza os componentes compartilhados de
// RF-FE-003 (ListToolbar, Paginacao, StatusBadge,
// ConfirmModal, ErrorAlert).
// ============================================================
import { useEffect, useState } from 'react';
import { Alert, Form, Button } from 'react-bootstrap';
import { Link, useLocation } from 'react-router';
import { useFornecedores } from '../hooks/useFornecedores.js';
import { useFornecedorAcoes } from '../hooks/useFornecedorAcoes.js';
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

export default function FornecedoresPage() {
  usePageTitle('Fornecedores');
  const location = useLocation();

  const { filters, page, setFilter, setPage, clearFilters, isFiltered } =
    useUrlFilters(FILTROS_PADRAO);
  const { data, isPending, isError, error, refetch } = useFornecedores({
    q: filters.q,
    situacao: filters.situacao,
    page,
  });
  const { mudarSituacao, excluir } = useFornecedorAcoes();

  const [sucesso, setSucesso] = useState(location.state?.sucesso || null);
  const [erroAcao, setErroAcao] = useState(null);
  // { tipo: 'inativar' | 'excluir', fornecedor } — reativar é direto
  const [acaoPendente, setAcaoPendente] = useState(null);
  const [erroModal, setErroModal] = useState(null);

  // Página que ficou vazia (ex.: após excluir o último item)
  // volta para a primeira página
  useEffect(() => {
    if (data && data.content.length === 0 && page > 0 && data.totalElements > 0) {
      setPage(0);
    }
  }, [data, page, setPage]);

  function abrirAcao(tipo, fornecedor) {
    setErroModal(null);
    setAcaoPendente({ tipo, fornecedor });
  }

  function fecharModal() {
    if (mudarSituacao.isPending || excluir.isPending) return;
    setAcaoPendente(null);
    setErroModal(null);
  }

  async function confirmarAcao() {
    const { tipo, fornecedor } = acaoPendente;
    try {
      if (tipo === 'inativar') {
        await mudarSituacao.mutateAsync({
          id: fornecedor.id,
          ativo: false,
          versao: fornecedor.versao,
        });
        setSucesso('Fornecedor inativado.');
      } else if (tipo === 'reativar') {
        await mudarSituacao.mutateAsync({
          id: fornecedor.id,
          ativo: true,
          versao: fornecedor.versao,
        });
        setSucesso('Fornecedor reativado.');
      } else {
        await excluir.mutateAsync(fornecedor.id);
        setSucesso('Fornecedor excluído.');
      }
      setAcaoPendente(null);
      setErroModal(null);
    } catch (err) {
      // Tratamento por CODE (RNF-FE-14)
      if (tipo === 'excluir' && err.code === 'SUPPLIER_HAS_HISTORY') {
        // Erro de negócio: modal permanece aberto com mensagem clara
        setErroModal({
          code: 'SUPPLIER_HAS_HISTORY',
          message:
            'Este fornecedor possui pedidos de compra ou contas a pagar vinculados e não pode ser excluído. Você pode inativá-lo.',
        });
        return;
      }
      if (tipo === 'excluir' && err.code === 'SUPPLIER_NOT_FOUND') {
        setAcaoPendente(null);
        setErroModal(null);
        setSucesso('Este fornecedor não existe mais.');
        return;
      }
      if (err.code === 'VERSION_CONFLICT') {
        // Recarrega a lista com a versao atualizada
        refetch();
        setAcaoPendente(null);
        setErroModal(null);
        setErroAcao(
          'O fornecedor foi alterado por outra sessão. A lista foi recarregada.',
        );
        return;
      }
      // Rede / 5xx / demais: mantém o modal aberto para nova tentativa
      setErroModal({
        code: 'GENERIC',
        message:
          tipo === 'excluir'
            ? 'Não foi possível excluir o fornecedor. Tente novamente.'
            : 'Não foi possível concluir a operação. Tente novamente.',
      });
    }
  }

  return (
    <div>
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h1 className="fs-2 mb-0">Fornecedores</h1>
        <Link to="/fornecedores/novo" className="btn btn-primary">
          Novo fornecedor
        </Link>
      </div>

      {sucesso && (
        <Alert variant="success" role="alert" className="py-2 small mb-3">
          {sucesso}
        </Alert>
      )}
      {erroAcao && <ErrorAlert variant="danger" message={erroAcao} />}

      <ListToolbar
        searchInputId="busca-fornecedores"
        searchLabel="Busca"
        searchPlaceholder="Buscar por nome ou documento"
        searchValue={filters.q}
        onSearchChange={(v) => setFilter('q', v)}
      >
        <Form.Group controlId="filtro-situacao-fornecedor" className="mb-0">
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
        <LoadingSpinner text="Carregando fornecedores…" />
      ) : isError ? (
        <ErrorScreen
          message={error?.detail || 'Não foi possível carregar os fornecedores.'}
          onRetry={refetch}
        />
      ) : data.content.length === 0 ? (
        <div className="text-center py-5">
          {isFiltered ? (
            <>
              <p className="text-secondary mb-3">
                Nenhum fornecedor encontrado para a busca.
              </p>
              <Button variant="outline-dark" onClick={clearFilters}>
                Limpar filtros
              </Button>
            </>
          ) : (
            <>
              <p className="text-secondary mb-3">
                Nenhum fornecedor cadastrado. Cadastre o primeiro fornecedor.
              </p>
              <Link to="/fornecedores/novo" className="btn btn-primary">
                Novo fornecedor
              </Link>
            </>
          )}
        </div>
      ) : (
        <>
          <div className="table-responsive">
            <table className="table align-middle">
              <caption className="visually-hidden">Lista de fornecedores</caption>
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
                {data.content.map((f) => (
                  <tr key={f.id}>
                    <th scope="row">{f.nome}</th>
                    <td>{maskDocumento(f.tipoPessoa, f.documento)}</td>
                    <td>{f.telefone ? maskTelefone(f.telefone) : '—'}</td>
                    <td>{f.email || '—'}</td>
                    <td>
                      <StatusBadge ativo={f.ativo} />
                    </td>
                    <td>
                      <div className="d-flex gap-1 flex-wrap">
                        <Link
                          className="btn btn-sm btn-outline-dark"
                          to={`/fornecedores/${f.id}`}
                        >
                          Editar
                        </Link>
                        <button
                          type="button"
                          className="btn btn-sm btn-outline-dark"
                          onClick={() =>
                            abrirAcao(f.ativo ? 'inativar' : 'reativar', f)
                          }
                        >
                          {f.ativo ? 'Inativar' : 'Reativar'}
                        </button>
                        <button
                          type="button"
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => abrirAcao('excluir', f)}
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
          acaoPendente?.tipo === 'inativar'
            ? 'Inativar fornecedor'
            : 'Excluir fornecedor'
        }
        message={
          acaoPendente?.tipo === 'inativar'
            ? `Inativar o fornecedor ${acaoPendente?.fornecedor?.nome}? Ele sai da lista de ativos e poderá ser reativado depois.`
            : `Excluir o fornecedor ${acaoPendente?.fornecedor?.nome}? Esta ação não pode ser desfeita.`
        }
        confirmLabel={
          acaoPendente?.tipo === 'inativar' ? 'Inativar' : 'Excluir'
        }
        variant="danger"
        busy={mudarSituacao.isPending || excluir.isPending}
        onConfirm={confirmarAcao}
        onCancel={fecharModal}
        alert={erroModal?.message}
        extraAction={
          erroModal?.code === 'SUPPLIER_HAS_HISTORY' &&
          acaoPendente?.tipo === 'excluir'
            ? {
                label: 'Inativar fornecedor',
                onClick: () => {
                  // Troca o modal para a confirmação de inativação
                  setAcaoPendente({
                    tipo: 'inativar',
                    fornecedor: acaoPendente.fornecedor,
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
