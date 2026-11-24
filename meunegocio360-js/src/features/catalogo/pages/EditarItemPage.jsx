// ============================================================
// EditarItemPage — edição de item do catálogo (rota /catalogo/:id)
// ITEM_NOT_FOUND exibe a página "Item não encontrado"
// (RF-FE-005).
// ============================================================
import { Link, useNavigate, useParams } from 'react-router';
import { useQuery } from '@tanstack/react-query';
import { obterItem } from '../api/catalogoApi.js';
import ItemForm from '../components/ItemForm.jsx';
import usePageTitle from '../../../shared/lib/usePageTitle.js';
import LoadingSpinner from '../../../shared/ui/LoadingSpinner.jsx';
import ErrorScreen from '../../../shared/ui/ErrorScreen.jsx';

export default function EditarItemPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  usePageTitle('Editar item');

  const { data: item, isPending, isError, error, refetch } = useQuery({
    queryKey: ['itens-catalogo', id],
    queryFn: () => obterItem(id),
    retry: false,
  });

  if (isPending) {
    return <LoadingSpinner text="Carregando item…" />;
  }

  if (isError) {
    if (error?.code === 'ITEM_NOT_FOUND') {
      return (
        <div className="text-center py-5">
          <h1 className="fs-2 mb-3">Item não encontrado</h1>
          <p className="text-secondary mb-4">
            O item não existe mais ou pertence a outra empresa.
          </p>
          <Link to="/catalogo" className="btn btn-primary">
            Voltar para o catálogo
          </Link>
        </div>
      );
    }
    return (
      <ErrorScreen
        message={error?.detail || 'Não foi possível carregar o item.'}
        onRetry={refetch}
      />
    );
  }

  return (
    <div className="row">
      <div className="col-md-8 col-lg-7">
        <h1 className="fs-2 mb-4">Editar item</h1>
        <ItemForm
          modo="edicao"
          itemInicial={item}
          onSucesso={() =>
            navigate('/catalogo', {
              state: { sucesso: 'Item atualizado com sucesso.' },
            })
          }
          onRecarregar={refetch}
          onCancelar={() => navigate('/catalogo')}
        />
      </div>
    </div>
  );
}
