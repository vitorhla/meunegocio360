// ============================================================
// EditarClientePage — edição de cliente (rota /clientes/:id)
// CUSTOMER_NOT_FOUND exibe a página "Cliente não encontrado"
// (RF-FE-003).
// ============================================================
import { Link, useNavigate, useParams } from 'react-router';
import { useQuery } from '@tanstack/react-query';
import { obterCliente } from '../api/clientesApi.js';
import ClienteForm from '../components/ClienteForm.jsx';
import usePageTitle from '../../../shared/lib/usePageTitle.js';
import LoadingSpinner from '../../../shared/ui/LoadingSpinner.jsx';
import ErrorScreen from '../../../shared/ui/ErrorScreen.jsx';

export default function EditarClientePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  usePageTitle('Editar cliente');

  const { data: cliente, isPending, isError, error, refetch } = useQuery({
    queryKey: ['clientes', id],
    queryFn: () => obterCliente(id),
    retry: false,
  });

  if (isPending) {
    return <LoadingSpinner text="Carregando cliente…" />;
  }

  if (isError) {
    if (error?.code === 'CUSTOMER_NOT_FOUND') {
      return (
        <div className="text-center py-5">
          <h1 className="fs-2 mb-3">Cliente não encontrado</h1>
          <p className="text-secondary mb-4">
            O cliente não existe mais ou pertence a outra empresa.
          </p>
          <Link to="/clientes" className="btn btn-primary">
            Voltar para clientes
          </Link>
        </div>
      );
    }
    return (
      <ErrorScreen
        message={error?.detail || 'Não foi possível carregar o cliente.'}
        onRetry={refetch}
      />
    );
  }

  return (
    <div className="row">
      <div className="col-md-8 col-lg-6">
        <h1 className="fs-2 mb-4">Editar cliente</h1>
        <ClienteForm
          modo="edicao"
          clienteInicial={cliente}
          onSucesso={() =>
            navigate('/clientes', {
              state: { sucesso: 'Cliente atualizado com sucesso.' },
            })
          }
          onRecarregar={refetch}
          onCancelar={() => navigate('/clientes')}
        />
      </div>
    </div>
  );
}
