// ============================================================
// NovoClientePage — cadastro de cliente (rota /clientes/novo)
// ============================================================
import { useNavigate } from 'react-router';
import ClienteForm from '../components/ClienteForm.jsx';
import usePageTitle from '../../../shared/lib/usePageTitle.js';

export default function NovoClientePage() {
  usePageTitle('Novo cliente');
  const navigate = useNavigate();

  return (
    <div className="row">
      <div className="col-md-8 col-lg-6">
        <h1 className="fs-2 mb-4">Novo cliente</h1>
        <ClienteForm
          modo="cadastro"
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
