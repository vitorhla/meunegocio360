// ============================================================
// AguardandoEmpresaPage — empresa ainda não cadastrada
// Exibida para perfis não-ADMIN quando não há empresa ativa.
// ============================================================
import { useEmpresa } from '../../features/empresa/index.js';
import usePageTitle from '../../shared/lib/usePageTitle.js';

export default function AguardandoEmpresaPage() {
  const { refetch } = useEmpresa();
  usePageTitle('Aguardando a empresa');

  return (
    <div className="min-vh-100 d-flex flex-column align-items-center justify-content-center px-3 text-center">
      <h1 className="fs-2 mb-3">Aguardando o cadastro da empresa</h1>
      <p className="text-secondary mb-4">
        A empresa ainda não foi cadastrada. Fale com o administrador para
        realizar o cadastro inicial.
      </p>
      <button
        type="button"
        className="btn btn-primary"
        onClick={() => refetch()}
      >
        Tentar novamente
      </button>
    </div>
  );
}
