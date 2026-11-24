// ============================================================
// NovoClientePage — cadastro de cliente (rota /clientes/novo)
// ============================================================
import { useNavigate } from 'react-router';
import PessoaForm from '../../../shared/ui/PessoaForm.jsx';
import { useClienteAcoes } from '../hooks/useClienteAcoes.js';
import usePageTitle from '../../../shared/lib/usePageTitle.js';

export default function NovoClientePage() {
  usePageTitle('Novo cliente');
  const navigate = useNavigate();
  const { cadastrar } = useClienteAcoes();

  return (
    <div className="row">
      <div className="col-md-8 col-lg-6">
        <h1 className="fs-2 mb-4">Novo cliente</h1>
        <PessoaForm
          modo="cadastro"
          entidade="cliente"
          codigoDocumentoDuplicado="CUSTOMER_DOCUMENT_DUPLICATE"
          onSubmit={(payload) => cadastrar.mutateAsync(payload)}
          onSucesso={() =>
            navigate('/clientes', {
              state: { sucesso: 'Cliente cadastrado com sucesso.' },
            })
          }
        />
      </div>
    </div>
  );
}
