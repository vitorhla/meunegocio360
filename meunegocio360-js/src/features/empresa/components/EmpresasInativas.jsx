// ============================================================
// EmpresasInativas — lista das empresas inativas
// Exibida em /empresa (ADMIN) quando não há empresa ativa,
// para que o ADMIN reative a desejada (extensão da RF-BE-002).
// ============================================================
import { maskCnpj } from '../../../shared/lib/cnpj.js';

/**
 * Lista de empresas inativas com ação de reativar.
 *
 * @param {object} props
 * @param {object[]} props.empresas Empresas inativas (GET /empresas filtrado)
 * @param {boolean} props.ocupado    True enquanto uma reativação está em andamento
 * @param {Function} props.onReativar (empresa) => abre o modal de confirmação
 */
export default function EmpresasInativas({ empresas, ocupado, onReativar }) {
  return (
    <div className="row row-cols-1 row-cols-md-2 g-3 mb-2">
      {empresas.map((e) => (
        <div key={e.id} className="col">
          <div className="card h-100">
            <div className="card-body d-flex flex-column">
              <h2 className="h5 card-title mb-1">{e.razaoSocial}</h2>
              {e.nomeFantasia && (
                <p className="text-secondary small mb-2">{e.nomeFantasia}</p>
              )}
              <p className="small text-secondary mb-1">
                CNPJ: {maskCnpj(e.cnpj)}
              </p>
              {e.email && <p className="small text-secondary mb-1">{e.email}</p>}
              {e.telefone && (
                <p className="small text-secondary mb-1">Tel.: {e.telefone}</p>
              )}
              <div className="mt-auto">
                <button
                  type="button"
                  className="btn btn-outline-primary btn-sm"
                  disabled={ocupado}
                  onClick={() => onReativar(e)}
                >
                  Reativar
                </button>
              </div>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
