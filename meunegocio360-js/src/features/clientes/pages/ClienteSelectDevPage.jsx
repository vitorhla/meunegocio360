// ============================================================
// ClienteSelectDevPage — rota de apoio para verificar o
// ClienteSelect e a busca compartilhada (RF-FE-003).
// Registrada SOMENTE em ambiente DEV (import.meta.env.DEV).
// ============================================================
import { useState } from 'react';
import { Form } from 'react-bootstrap';
import { ClienteSelect, useBuscaClientes } from '../index.js';
import usePageTitle from '../../../shared/lib/usePageTitle.js';
import { maskDocumento } from '../../../shared/lib/tipoPessoa.js';

export default function ClienteSelectDevPage() {
  usePageTitle('Dev: ClienteSelect');
  const [selecionado, setSelecionado] = useState(null);
  const [textoBusca, setTextoBusca] = useState('');
  const { data: resultados, isPending, isError } = useBuscaClientes(textoBusca);

  const temBusca = textoBusca.trim().length >= 2;

  return (
    <div className="row">
      <div className="col-md-8 col-lg-7">
        <h1 className="fs-2 mb-2">ClienteSelect (rota de desenvolvimento)</h1>
        <p className="text-secondary small">
          Rota de apoio disponível apenas em DEV. Use-a para verificar o
          seletor (mouse e teclado) e a busca compartilhada — a mesma lógica
          da lista de clientes.
        </p>

        <div className="card mb-4">
          <div className="card-body">
            <ClienteSelect
              label="Cliente"
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
                  {maskDocumento(selecionado.tipoPessoa, selecionado.documento)}
                </dd>
              </dl>
            ) : (
              <p className="text-secondary small mt-3 mb-0">
                Nenhum cliente selecionado.
              </p>
            )}
          </div>
        </div>

        <h2 className="h5">Busca compartilhada (useBuscaClientes)</h2>
        <Form.Group className="mb-2" controlId="dev-busca-clientes">
          <Form.Label>Buscar (a partir de 2 caracteres)</Form.Label>
          <Form.Control
            type="search"
            placeholder="ex.: Maria"
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
            <p className="small text-secondary">Nenhum cliente encontrado</p>
          ) : (
            <ul className="small">
              {resultados.map((c) => (
                <li key={c.id}>
                  {c.nome} — {maskDocumento(c.tipoPessoa, c.documento)}
                </li>
              ))}
            </ul>
          ))}
      </div>
    </div>
  );
}
