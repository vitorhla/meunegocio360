// ============================================================
// CatalogoPage — lista do catálogo (rota /catalogo, RF-FE-005)
// Busca, filtros por tipo, categoria e situação e paginação
// na URL (RNF-FE-27). Reutiliza os componentes compartilhados
// de RF-FE-002/003.
// ============================================================
import { useEffect, useState } from 'react';
import { Alert, Form, Button } from 'react-bootstrap';
import { Link, useLocation } from 'react-router';
import { useItens } from '../hooks/useItens.js';
import { useItemAcoes } from '../hooks/useItemAcoes.js';
import { useCategoriasAtivas } from '../hooks/useCategorias.js';
import usePageTitle from '../../../shared/lib/usePageTitle.js';
import useUrlFilters from '../../../shared/lib/useUrlFilters.js';
import { formatarMoedaBR } from '../../../shared/lib/moeda.js';
import ListToolbar from '../../../shared/ui/ListToolbar.jsx';
import Paginacao from '../../../shared/ui/Paginacao.jsx';
import StatusBadge from '../../../shared/ui/StatusBadge.jsx';
import LoadingSpinner from '../../../shared/ui/LoadingSpinner.jsx';
import ErrorScreen from '../../../shared/ui/ErrorScreen.jsx';
import ConfirmModal from '../../../shared/ui/ConfirmModal.jsx';
import ErrorAlert from '../../../shared/ui/ErrorAlert.jsx';
import TipoBadge from '../components/TipoBadge.jsx';

const FILTROS_PADRAO = { q: '', tipo: '', categoriaId: '', situacao: 'ATIVO' };

export default function CatalogoPage() {
  usePageTitle('Catálogo');
  const location = useLocation();

  const { filters, page, setFilter, setPage, clearFilters, isFiltered } =
    useUrlFilters(FILTROS_PADRAO);
  const { data, isPending, isError, error, refetch } = useItens({
    q: filters.q,
    tipo: filters.tipo,
    categoriaId: filters.categoriaId,
    situacao: filters.situacao,
    page,
  });
  const { data: categorias } = useCategoriasAtivas();
  const { mudarSituacao, excluir } = useItemAcoes();

  const [sucesso, setSucesso] = useState(location.state?.sucesso || null);
  const [erroAcao, setErroAcao] = useState(null);
  // { tipo: 'inativar' | 'excluir', item } — reativar é direto
  const [acaoPendente, setAcaoPendente] = useState(null);
  const [erroModal, setErroModal] = useState(null);

  // Página que ficou vazia (ex.: após excluir o último item)
  // volta para a primeira página
  useEffect(() => {
    if (data && data.content.length === 0 && page > 0 && data.totalElements > 0) {
      setPage(0);
    }
  }, [data, page, setPage]);

  function abrirAcao(tipo, item) {
    setErroModal(null);
    setAcaoPendente({ tipo, item });
  }

  function fecharModal() {
    if (mudarSituacao.isPending || excluir.isPending) return;
    setAcaoPendente(null);
    setErroModal(null);
  }

  async function confirmarAcao() {
    const { tipo, item } = acaoPendente;
    try {
      if (tipo === 'inativar') {
        await mudarSituacao.mutateAsync({
          id: item.id,
          ativo: false,
          versao: item.versao,
        });
        setSucesso('Item inativado.');
      } else if (tipo === 'reativar') {
        await mudarSituacao.mutateAsync({
          id: item.id,
          ativo: true,
          versao: item.versao,
        });
        setSucesso('Item reativado.');
      } else {
        await excluir.mutateAsync(item.id);
        setSucesso('Item excluído.');
      }
      setAcaoPendente(null);
      setErroModal(null);
    } catch (err) {
      // Tratamento por CODE (RNF-FE-14)
      if (tipo === 'excluir' && err.code === 'ITEM_HAS_HISTORY') {
        // Erro de negócio: modal permanece aberto com mensagem clara
        setErroModal({
          code: 'ITEM_HAS_HISTORY',
          message:
            'Este item possui vendas ou compras vinculadas e não pode ser excluído. Você pode inativá-lo.',
        });
        return;
      }
      if (tipo === 'excluir' && err.code === 'ITEM_NOT_FOUND') {
        setAcaoPendente(null);
        setErroModal(null);
        setSucesso('Este item não existe mais.');
        return;
      }
      if (err.code === 'VERSION_CONFLICT') {
        // Recarrega a lista com a versao atualizada
        refetch();
        setAcaoPendente(null);
        setErroModal(null);
        setErroAcao(
          'O item foi alterado por outra sessão. A lista foi recarregada.',
        );
        return;
      }
      // Rede / 5xx / demais: mantém o modal aberto para nova tentativa
      setErroModal({
        code: 'GENERIC',
        message:
          tipo === 'excluir'
            ? 'Não foi possível excluir o item. Tente novamente.'
            : 'Não foi possível concluir a operação. Tente novamente.',
      });
    }
  }

  return (
    <div>
      <div className="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-3">
        <h1 className="fs-2 mb-0">Catálogo</h1>
        <div className="d-flex gap-2 flex-wrap">
          <Link to="/catalogo/categorias" className="btn btn-outline-dark">
            Categorias
          </Link>
          <Link to="/catalogo/novo" className="btn btn-primary">
            Novo item
          </Link>
        </div>
      </div>

      {sucesso && (
        <Alert variant="success" role="alert" className="py-2 small mb-3">
          {sucesso}
        </Alert>
      )}
      {erroAcao && <ErrorAlert variant="danger" message={erroAcao} />}

      <ListToolbar
        searchInputId="busca-catalogo"
        searchLabel="Busca"
        searchPlaceholder="Buscar por nome ou código"
        searchValue={filters.q}
        onSearchChange={(v) => setFilter('q', v)}
      >
        <Form.Group controlId="filtro-tipo" className="mb-0">
          <Form.Label className="small">Tipo</Form.Label>
          <Form.Select
            value={filters.tipo}
            onChange={(e) => setFilter('tipo', e.target.value)}
          >
            <option value="">Todos</option>
            <option value="PRODUTO">Produtos</option>
            <option value="SERVICO">Serviços</option>
          </Form.Select>
        </Form.Group>
        <Form.Group controlId="filtro-categoria" className="mb-0">
          <Form.Label className="small">Categoria</Form.Label>
          <Form.Select
            value={filters.categoriaId}
            onChange={(e) => setFilter('categoriaId', e.target.value)}
          >
            <option value="">Sem filtro</option>
            {(categorias || []).map((c) => (
              <option key={c.id} value={c.id}>
                {c.nome}
              </option>
            ))}
          </Form.Select>
        </Form.Group>
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
        <LoadingSpinner text="Carregando catálogo…" />
      ) : isError ? (
        <ErrorScreen
          message={
            error?.detail || 'Não foi possível carregar o catálogo.'
          }
          onRetry={refetch}
        />
      ) : data.content.length === 0 ? (
        <div className="text-center py-5">
          {isFiltered ? (
            <>
              <p className="text-secondary mb-3">
                Nenhum item encontrado para os filtros aplicados.
              </p>
              <Button variant="outline-dark" onClick={clearFilters}>
                Limpar filtros
              </Button>
            </>
          ) : (
            <>
              <p className="text-secondary mb-3">
                O catálogo está vazio. Cadastre o primeiro produto ou serviço.
              </p>
              <Link to="/catalogo/novo" className="btn btn-primary">
                Novo item
              </Link>
            </>
          )}
        </div>
      ) : (
        <>
          <div className="table-responsive">
            <table className="table align-middle">
              <caption className="visually-hidden">Lista de itens do catálogo</caption>
              <thead>
                <tr>
                  <th scope="col">Código</th>
                  <th scope="col">Nome</th>
                  <th scope="col">Tipo</th>
                  <th scope="col">Categoria</th>
                  <th scope="col">Unidade</th>
                  <th scope="col">Preço</th>
                  <th scope="col">Situação</th>
                  <th scope="col">Ações</th>
                </tr>
              </thead>
              <tbody>
                {data.content.map((item) => (
                  <tr key={item.id}>
                    <td>{item.codigo || '—'}</td>
                    <th scope="row">{item.nome}</th>
                    <td>
                      <TipoBadge tipo={item.tipo} />
                    </td>
                    <td>{item.categoria?.nome || '—'}</td>
                    <td>{item.unidade}</td>
                    <td>{formatarMoedaBR(item.preco)}</td>
                    <td>
                      <StatusBadge ativo={item.ativo} />
                    </td>
                    <td>
                      <div className="d-flex gap-1 flex-wrap">
                        <Link
                          className="btn btn-sm btn-outline-dark"
                          to={`/catalogo/${item.id}`}
                        >
                          Editar
                        </Link>
                        <button
                          type="button"
                          className="btn btn-sm btn-outline-dark"
                          onClick={() =>
                            abrirAcao(item.ativo ? 'inativar' : 'reativar', item)
                          }
                        >
                          {item.ativo ? 'Inativar' : 'Reativar'}
                        </button>
                        <button
                          type="button"
                          className="btn btn-sm btn-outline-danger"
                          onClick={() => abrirAcao('excluir', item)}
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
          acaoPendente?.tipo === 'inativar' ? 'Inativar item' : 'Excluir item'
        }
        message={
          acaoPendente?.tipo === 'inativar'
            ? `Inativar o item ${acaoPendente?.item?.nome}? Ele sai da lista de ativos e poderá ser reativado depois.`
            : `Excluir o item ${acaoPendente?.item?.nome}? Esta ação não pode ser desfeita.`
        }
        confirmLabel={acaoPendente?.tipo === 'inativar' ? 'Inativar' : 'Excluir'}
        variant="danger"
        busy={mudarSituacao.isPending || excluir.isPending}
        onConfirm={confirmarAcao}
        onCancel={fecharModal}
        alert={erroModal?.message}
        extraAction={
          erroModal?.code === 'ITEM_HAS_HISTORY' &&
          acaoPendente?.tipo === 'excluir'
            ? {
                label: 'Inativar item',
                onClick: () => {
                  // Troca o modal para a confirmação de inativação
                  setAcaoPendente({
                    tipo: 'inativar',
                    item: acaoPendente.item,
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
