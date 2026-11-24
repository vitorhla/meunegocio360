// ============================================================
// CategoriasPage — gestão mínima de categorias
// (rota /catalogo/categorias, RF-FE-005): listar com busca e
// situação, criar (modal), renomear, inativar/reativar e
// excluir. Erros de negócio por code (RNF-FE-14).
// ============================================================
import { useState } from 'react';
import { Alert, Form, Button } from 'react-bootstrap';
import { Link, useLocation } from 'react-router';
import { useCategorias } from '../hooks/useCategorias.js';
import { useCategoriaAcoes } from '../hooks/useCategoriaAcoes.js';
import usePageTitle from '../../../shared/lib/usePageTitle.js';
import useUrlFilters from '../../../shared/lib/useUrlFilters.js';
import ListToolbar from '../../../shared/ui/ListToolbar.jsx';
import StatusBadge from '../../../shared/ui/StatusBadge.jsx';
import LoadingSpinner from '../../../shared/ui/LoadingSpinner.jsx';
import ErrorScreen from '../../../shared/ui/ErrorScreen.jsx';
import ConfirmModal from '../../../shared/ui/ConfirmModal.jsx';
import ErrorAlert from '../../../shared/ui/ErrorAlert.jsx';
import ModalCategoria from '../components/ModalCategoria.jsx';

const FILTROS_PADRAO = { q: '', situacao: 'ATIVO' };

export default function CategoriasPage() {
  usePageTitle('Categorias');
  const location = useLocation();

  const { filters, setFilter, clearFilters, isFiltered } =
    useUrlFilters(FILTROS_PADRAO);
  const { data, isPending, isError, error, refetch } = useCategorias({
    q: filters.q,
    situacao: filters.situacao,
  });
  const { mudarSituacao, excluir } = useCategoriaAcoes();

  const [sucesso, setSucesso] = useState(location.state?.sucesso || null);
  const [erroAcao, setErroAcao] = useState(null);
  // { modo: 'criar' | 'renomear', categoria? } — key remonta o modal
  const [modal, setModal] = useState(null);
  // { tipo: 'inativar' | 'excluir', categoria } — reativar é direto
  const [acaoPendente, setAcaoPendente] = useState(null);
  const [erroModal, setErroModal] = useState(null);

  function fecharModal() {
    setModal(null);
  }

  function abrirAcao(tipo, categoria) {
    setErroModal(null);
    setAcaoPendente({ tipo, categoria });
  }

  function fecharConfirmacao() {
    if (mudarSituacao.isPending || excluir.isPending) return;
    setAcaoPendente(null);
    setErroModal(null);
  }

  async function confirmarAcao() {
    const { tipo, categoria } = acaoPendente;
    try {
      if (tipo === 'inativar') {
        await mudarSituacao.mutateAsync({
          id: categoria.id,
          ativo: false,
          versao: categoria.versao,
        });
        setSucesso('Categoria inativada.');
      } else if (tipo === 'reativar') {
        await mudarSituacao.mutateAsync({
          id: categoria.id,
          ativo: true,
          versao: categoria.versao,
        });
        setSucesso('Categoria reativada.');
      } else {
        await excluir.mutateAsync(categoria.id);
        setSucesso('Categoria excluída.');
      }
      setAcaoPendente(null);
      setErroModal(null);
    } catch (err) {
      // Tratamento por CODE (RNF-FE-14)
      if (tipo === 'excluir' && err.code === 'CATEGORY_IN_USE') {
        // Erro de negócio: modal permanece aberto com mensagem clara
        setErroModal({
          code: 'CATEGORY_IN_USE',
          message:
            'Esta categoria possui itens vinculados e não pode ser excluída. Você pode inativá-la.',
        });
        return;
      }
      if (tipo === 'excluir' && err.code === 'CATEGORY_NOT_FOUND') {
        setAcaoPendente(null);
        setErroModal(null);
        setSucesso('Esta categoria não existe mais.');
        return;
      }
      if (err.code === 'VERSION_CONFLICT') {
        // Recarrega a lista com a versao atualizada
        refetch();
        setAcaoPendente(null);
        setErroModal(null);
        setErroAcao(
          'A categoria foi alterada por outra sessão. A lista foi recarregada.',
        );
        return;
      }
      // Rede / 5xx / demais: mantém o modal aberto para nova tentativa
      setErroModal({
        code: 'GENERIC',
        message:
          tipo === 'excluir'
            ? 'Não foi possível excluir a categoria. Tente novamente.'
            : 'Não foi possível concluir a operação. Tente novamente.',
      });
    }
  }

  const categorias = data?.content || [];

  return (
    <div>
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h1 className="fs-2 mb-0">Categorias</h1>
        <div className="d-flex gap-2 flex-wrap">
          <Link to="/catalogo" className="btn btn-outline-dark">
            Voltar ao catálogo
          </Link>
          <Button
            variant="primary"
            onClick={() => setModal({ modo: 'criar' })}
          >
            Nova categoria
          </Button>
        </div>
      </div>

      {sucesso && (
        <Alert variant="success" role="alert" className="py-2 small mb-3">
          {sucesso}
        </Alert>
      )}
      {erroAcao && <ErrorAlert variant="danger" message={erroAcao} />}

      <ListToolbar
        searchInputId="busca-categorias"
        searchLabel="Busca"
        searchPlaceholder="Buscar por nome"
        searchValue={filters.q}
        onSearchChange={(v) => setFilter('q', v)}
      >
        <Form.Group controlId="filtro-situacao-categoria" className="mb-0">
          <Form.Label className="small">Situação</Form.Label>
          <Form.Select
            value={filters.situacao}
            onChange={(e) => setFilter('situacao', e.target.value)}
          >
            <option value="ATIVO">Ativas</option>
            <option value="INATIVO">Inativas</option>
            <option value="TODOS">Todas</option>
          </Form.Select>
        </Form.Group>
      </ListToolbar>

      {isPending && !data ? (
        <LoadingSpinner text="Carregando categorias…" />
      ) : isError ? (
        <ErrorScreen
          message={
            error?.detail || 'Não foi possível carregar as categorias.'
          }
          onRetry={refetch}
        />
      ) : categorias.length === 0 ? (
        <div className="text-center py-5">
          {isFiltered ? (
            <>
              <p className="text-secondary mb-3">
                Nenhuma categoria encontrada para a busca.
              </p>
              <Button variant="outline-dark" onClick={clearFilters}>
                Limpar filtros
              </Button>
            </>
          ) : (
            <p className="text-secondary mb-3">
              Nenhuma categoria cadastrada. Crie a primeira categoria.
            </p>
          )}
        </div>
      ) : (
        <div className="table-responsive">
          <table className="table align-middle">
            <caption className="visually-hidden">Lista de categorias</caption>
            <thead>
              <tr>
                <th scope="col">Nome</th>
                <th scope="col">Situação</th>
                <th scope="col">Ações</th>
              </tr>
            </thead>
            <tbody>
              {categorias.map((c) => (
                <tr key={c.id}>
                  <th scope="row">{c.nome}</th>
                  <td>
                    <StatusBadge ativo={c.ativo} />
                  </td>
                  <td>
                    <div className="d-flex gap-1 flex-wrap">
                      <button
                        type="button"
                        className="btn btn-sm btn-outline-dark"
                        onClick={() =>
                          setModal({ modo: 'renomear', categoria: c })
                        }
                      >
                        Renomear
                      </button>
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
      )}

      {/* Modal de criação/renomeação (a key remonta com estado novo) */}
      {modal && (
        <ModalCategoria
          key={modal.categoria?.id || 'nova'}
          mostrar
          modo={modal.modo}
          categoria={modal.categoria}
          onSucesso={(mensagem) => {
            setSucesso(mensagem);
            fecharModal();
          }}
          onFechar={fecharModal}
        />
      )}

      {/* Modais de confirmação (inativar/excluir); reativar é direto */}
      <ConfirmModal
        show={
          acaoPendente?.tipo === 'inativar' || acaoPendente?.tipo === 'excluir'
        }
        title={
          acaoPendente?.tipo === 'inativar'
            ? 'Inativar categoria'
            : 'Excluir categoria'
        }
        message={
          acaoPendente?.tipo === 'inativar'
            ? `Inativar a categoria ${acaoPendente?.categoria?.nome}? Os itens que a usam não são alterados, mas ela sai das seleções de categoria.`
            : `Excluir a categoria ${acaoPendente?.categoria?.nome}? Esta ação não pode ser desfeita.`
        }
        confirmLabel={
          acaoPendente?.tipo === 'inativar' ? 'Inativar' : 'Excluir'
        }
        variant="danger"
        busy={mudarSituacao.isPending || excluir.isPending}
        onConfirm={confirmarAcao}
        onCancel={fecharConfirmacao}
        alert={erroModal?.message}
        extraAction={
          erroModal?.code === 'CATEGORY_IN_USE' &&
          acaoPendente?.tipo === 'excluir'
            ? {
                label: 'Inativar categoria',
                onClick: () => {
                  // Troca o modal para a confirmação de inativação
                  setAcaoPendente({
                    tipo: 'inativar',
                    categoria: acaoPendente.categoria,
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
