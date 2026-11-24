// ============================================================
// ItemSelectDevPage — rota de apoio para verificar o
// ItemCatalogoSelect (RF-FE-005), com e sem o filtro de tipo.
// Registrada SOMENTE em ambiente DEV (import.meta.env.DEV).
// ============================================================
import { useState } from 'react';
import { Form } from 'react-bootstrap';
import { ItemCatalogoSelect, useBuscaItens } from '../index.js';
import { TipoItem, rotuloTipoItem } from '../lib/tipoItem.js';
import { formatarMoedaBR } from '../../../shared/lib/moeda.js';
import usePageTitle from '../../../shared/lib/usePageTitle.js';

export default function ItemSelectDevPage() {
  usePageTitle('Dev: ItemCatalogoSelect');
  const [semTipo, setSemTipo] = useState(null);
  const [somenteProdutos, setSomenteProdutos] = useState(null);
  const [textoBusca, setTextoBusca] = useState('');
  const { data: resultados, isPending, isError } =
    useBuscaItens(textoBusca);

  const temBusca = textoBusca.trim().length >= 2;

  return (
    <div className="row">
      <div className="col-md-8 col-lg-8">
        <h1 className="fs-2 mb-2">
          ItemCatalogoSelect (rota de desenvolvimento)
        </h1>
        <p className="text-secondary small">
          Rota de apoio disponível apenas em DEV. O seletor à esquerda busca
          todos os itens ativos; o da direita usa{' '}
          <code>tipo="{TipoItem.PRODUTO}"</code> (o filtro é repassado à API) —
          nenhum serviço deve aparecer nele.
        </p>

        <div className="row g-3 mb-4">
          <div className="col-md-6">
            <div className="card h-100">
              <div className="card-body">
                <h2 className="h6">Sem filtro de tipo</h2>
                <ItemCatalogoSelect
                  label="Item"
                  value={semTipo}
                  onChange={setSemTipo}
                />
              </div>
            </div>
          </div>
          <div className="col-md-6">
            <div className="card h-100">
              <div className="card-body">
                <h2 className="h6">Somente produtos (tipo="PRODUTO")</h2>
                <ItemCatalogoSelect
                  label="Produto"
                  tipo={TipoItem.PRODUTO}
                  value={somenteProdutos}
                  onChange={setSomenteProdutos}
                />
              </div>
            </div>
          </div>
        </div>

        {semTipo && (
          <p className="small">
            Selecionado (sem filtro): {semTipo.nome} —{' '}
            {rotuloTipoItem(semTipo.tipo)} — {formatarMoedaBR(semTipo.preco)}
          </p>
        )}
        {somenteProdutos && (
          <p className="small">
            Selecionado (somente produtos): {somenteProdutos.nome} —{' '}
            {rotuloTipoItem(somenteProdutos.tipo)} —{' '}
            {formatarMoedaBR(somenteProdutos.preco)}
          </p>
        )}

        <h2 className="h5">Busca compartilhada (useBuscaItens)</h2>
        <Form.Group className="mb-2" controlId="dev-busca-itens">
          <Form.Label>Buscar (a partir de 2 caracteres)</Form.Label>
          <Form.Control
            type="search"
            placeholder="ex.: Mouse"
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
            <p className="small text-secondary">Nenhum item encontrado</p>
          ) : (
            <ul className="small">
              {resultados.map((i) => (
                <li key={i.id}>
                  {i.nome}
                  {i.codigo ? ` (${i.codigo})` : ''} —{' '}
                  {rotuloTipoItem(i.tipo)} — {formatarMoedaBR(i.preco)}
                </li>
              ))}
            </ul>
          ))}
      </div>
    </div>
  );
}
