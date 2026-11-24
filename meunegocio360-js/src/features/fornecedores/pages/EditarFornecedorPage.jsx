// ============================================================
// EditarFornecedorPage — edição de fornecedor
// (rota /fornecedores/:id)
// SUPPLIER_NOT_FOUND exibe a página "Fornecedor não encontrado"
// (RF-FE-004).
// ============================================================
import { Link, useNavigate, useParams } from 'react-router';
import { useQuery } from '@tanstack/react-query';
import { obterFornecedor } from '../api/fornecedoresApi.js';
import { useFornecedorAcoes } from '../hooks/useFornecedorAcoes.js';
import PessoaForm from '../../../shared/ui/PessoaForm.jsx';
import usePageTitle from '../../../shared/lib/usePageTitle.js';
import LoadingSpinner from '../../../shared/ui/LoadingSpinner.jsx';
import ErrorScreen from '../../../shared/ui/ErrorScreen.jsx';

export default function EditarFornecedorPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  usePageTitle('Editar fornecedor');
  const { atualizar } = useFornecedorAcoes();

  const { data: fornecedor, isPending, isError, error, refetch } = useQuery({
    queryKey: ['fornecedores', id],
    queryFn: () => obterFornecedor(id),
    retry: false,
  });

  if (isPending) {
    return <LoadingSpinner text="Carregando fornecedor…" />;
  }

  if (isError) {
    if (error?.code === 'SUPPLIER_NOT_FOUND') {
      return (
        <div className="text-center py-5">
          <h1 className="fs-2 mb-3">Fornecedor não encontrado</h1>
          <p className="text-secondary mb-4">
            O fornecedor não existe mais ou pertence a outra empresa.
          </p>
          <Link to="/fornecedores" className="btn btn-primary">
            Voltar para fornecedores
          </Link>
        </div>
      );
    }
    return (
      <ErrorScreen
        message={
          error?.detail || 'Não foi possível carregar o fornecedor.'
        }
        onRetry={refetch}
      />
    );
  }

  return (
    <div className="row">
      <div className="col-md-8 col-lg-6">
        <h1 className="fs-2 mb-4">Editar fornecedor</h1>
        <PessoaForm
          modo="edicao"
          registroInicial={fornecedor}
          entidade="fornecedor"
          codigoDocumentoDuplicado="SUPPLIER_DOCUMENT_DUPLICATE"
          onSubmit={(payload) =>
            atualizar.mutateAsync({ id: fornecedor.id, payload })
          }
          onSucesso={() =>
            navigate('/fornecedores', {
              state: { sucesso: 'Fornecedor atualizado com sucesso.' },
            })
          }
          onRecarregar={refetch}
          onCancelar={() => navigate('/fornecedores')}
        />
      </div>
    </div>
  );
}
