// ============================================================
// NovoItemPage — cadastro de item do catálogo (rota /catalogo/novo)
// ============================================================
import { useNavigate } from 'react-router';
import ItemForm from '../components/ItemForm.jsx';
import usePageTitle from '../../../shared/lib/usePageTitle.js';

export default function NovoItemPage() {
  usePageTitle('Novo item');
  const navigate = useNavigate();

  return (
    <div className="row">
      <div className="col-md-8 col-lg-7">
        <h1 className="fs-2 mb-4">Novo item</h1>
        <ItemForm
          modo="cadastro"
          onSucesso={() =>
            navigate('/catalogo', {
              state: { sucesso: 'Item cadastrado com sucesso.' },
            })
          }
        />
      </div>
    </div>
  );
}
