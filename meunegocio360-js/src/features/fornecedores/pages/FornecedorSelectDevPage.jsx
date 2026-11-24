// ============================================================
// FornecedorSelectDevPage — rota de apoio para verificar o
// FornecedorSelect e a busca compartilhada (RF-FE-004).
// Registrada SOMENTE em ambiente DEV (import.meta.env.DEV).
// ============================================================
import { useState } from 'react';
import { Form } from 'react-bootstrap';
import { FornecedorSelect, useBuscaFornecedores } from '../index.js';
import usePageTitle from '../../../shared/lib/usePageTitle.js';
import { maskDocumento } from '../../../shared/lib/tipoPessoa.js';

export default function FornecedorSelectDevPage() {
  usePageTitle('Dev: FornecedorSelect');
  const [selecionado, setSelecionado] = useState(null);
  const [textoBusca, setTextoBusca] = useState('');
  const { data: resultados, isPending, isError } =
    useBuscaFornecedores(textoBusca);

  const temBusca = textoBusca.trim().length >= 2;

  return (
    <div className="row">
      <div className="col-md-8 col-lg-7">
        <h1 className="fs-2 mb-2">
          FornecedorSelect (rota de desenvolvimento)
        </h1>
        <p className="text-secondary small">
          Rota de apoio disponível apenas em DEV. Use-a para verificar o
          seletor (mouse e teclado) e a busca compartilhada — a mesma lógica
          da lista de fornecedores.
        </p>

        <div className="card mb-4">
          <div className="card-body">
            <FornecedorSelect
              label="Fornecedor"
              value={selecionado}
              onChange={setSelecionado}
            />
            {selecionado ? (
              <dl className="row small mt-3 mb-0">
                <dt className="col-4">id</dt>
                <dd className="col-8">{selecionado.id}</dd>
                <dt className="col-4">nome</dt>
                <dd className="col-8">{selecionado.nome}</dd>
                <dt className="col-4">documento</dt>
                <dd className="col-8">
                  {maskDocumento(
                    selecionado.tipoPessoa,
                    selecionado.documento,
                  )}
                </dd>
              </dl>
            ) : (
              <p className="text-secondary small mt-3 mb-0">
                Nenhum fornecedor selecionado.
              </p>
            )}
          </div>
        </div>

        <h2 className="h5">Busca compartilhada (useBuscaFornecedores)</h2>
        <Form.Group className="mb-2" controlId="dev-busca-fornecedores">
          <Form.Label>Buscar (a partir de 2 caracteres)</Form.Label>
          <Form.Control
            type="search"
            placeholder="ex.: Distribuidora"
            value={textoBusca}
            onChange={(e) => setTextoBusca(e.target.value)}
          />
        </Form.Group>
        {temBusca &&
          (isPending ? (
            <p className="small text-secondary">Buscando…</p>
          ) : isError ? (
            <p className="small text-danger">Erro na busca.</p>
          ) : resultados.length === 0 ? (
            <p className="small text-secondary">Nenhum fornecedor encontrado</p>
          ) : (
            <ul className="small">
              {resultados.map((f) => (
                <li key={f.id}>
                  {f.nome} — {maskDocumento(f.tipoPessoa, f.documento)}
                </li>
              ))}
            </ul>
          ))}
      </div>
    </div>
  );
}
