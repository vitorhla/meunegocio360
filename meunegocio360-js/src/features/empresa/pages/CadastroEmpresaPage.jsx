// ============================================================
// CadastroEmpresaPage — primeiro cadastro da empresa
// Rota /empresa/cadastro (ADMIN, somente sem empresa ativa)
// ============================================================
import { Navigate, useNavigate } from 'react-router';
import { useEmpresa } from '../index.js';
import usePageTitle from '../../../shared/lib/usePageTitle.js';
import LoadingSpinner from '../../../shared/ui/LoadingSpinner.jsx';
import ErrorScreen from '../../../shared/ui/ErrorScreen.jsx';
import EmpresaForm from '../components/EmpresaForm.jsx';

export default function CadastroEmpresaPage() {
  const { status, refetch } = useEmpresa();
  const navigate = useNavigate();
  usePageTitle('Cadastre sua empresa');

  if (status === 'carregando') {
    return <LoadingSpinner text="Carregando…" />;
  }

  if (status === 'ativa') {
    // Com empresa ativa, redireciona para a página de empresa
    return <Navigate to="/empresa" replace />;
  }

  if (status === 'erro') {
    return (
      <ErrorScreen
        message="Não foi possível carregar os dados da empresa."
        onRetry={refetch}
      />
    );
  }

  // 'ausente' — exibe o formulário de cadastro
  return (
    <div>
      <h1 className="fs-2 mb-4">Cadastre sua empresa</h1>

      <div className="row">
        <div className="col-md-8 col-lg-6">
          <EmpresaForm
            modo="cadastro"
            onSucesso={() => {
              navigate('/', {
                state: {
                  flash: {
                    variant: 'success',
                    message: 'Empresa cadastrada com sucesso.',
                  },
                },
              });
            }}
          />
        </div>
      </div>
    </div>
  );
}
