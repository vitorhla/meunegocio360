// ============================================================
// NovoFornecedorPage — cadastro de fornecedor
// (rota /fornecedores/novo)
// ============================================================
import { useNavigate } from 'react-router';
import PessoaForm from '../../../shared/ui/PessoaForm.jsx';
import { useFornecedorAcoes } from '../hooks/useFornecedorAcoes.js';
import usePageTitle from '../../../shared/lib/usePageTitle.js';

export default function NovoFornecedorPage() {
  usePageTitle('Novo fornecedor');
  const navigate = useNavigate();
  const { cadastrar } = useFornecedorAcoes();

  return (
    <div className="row">
      <div className="col-md-8 col-lg-6">
        <h1 className="fs-2 mb-4">Novo fornecedor</h1>
        <PessoaForm
          modo="cadastro"
          entidade="fornecedor"
          codigoDocumentoDuplicado="SUPPLIER_DOCUMENT_DUPLICATE"
          onSubmit={(payload) => cadastrar.mutateAsync(payload)}
          onSucesso={() =>
            navigate('/fornecedores', {
              state: { sucesso: 'Fornecedor cadastrado com sucesso.' },
            })
          }
        />
      </div>
    </div>
  );
}
