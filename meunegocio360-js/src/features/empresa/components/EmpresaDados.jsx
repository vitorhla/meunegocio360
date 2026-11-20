// ============================================================
// EmpresaDados — exibição dos dados da empresa em modo leitura
// ============================================================
import { Badge, Card } from 'react-bootstrap';
import { maskCnpj } from '../../../shared/lib/cnpj.js';
import { maskTelefone } from '../../../shared/lib/telefone.js';
import { FUSOS_BR } from '../../../shared/lib/fusos.js';

/**
 * Card com os dados da empresa para consulta.
 *
 * @param {object}   props
 * @param {object}   props.empresa   — objeto da empresa (do contexto)
 */
export default function EmpresaDados({ empresa }) {
  if (!empresa) return null;

  const fuso = FUSOS_BR.find((f) => f.id === empresa.fusoHorario);
  const labelFuso = fuso ? fuso.label : empresa.fusoHorario;

  return (
    <Card className="shadow-sm">
      <Card.Body>
        <dl className="row mb-0">
          <div className="col-sm-6 mb-3">
            <dt className="text-secondary small mb-1">Razão social</dt>
            <dd className="mb-0">{empresa.razaoSocial}</dd>
          </div>

          <div className="col-sm-6 mb-3">
            <dt className="text-secondary small mb-1">Nome fantasia</dt>
            <dd className="mb-0">{empresa.nomeFantasia || '—'}</dd>
          </div>

          <div className="col-sm-6 mb-3">
            <dt className="text-secondary small mb-1">CNPJ</dt>
            <dd className="mb-0">{maskCnpj(empresa.cnpj)}</dd>
          </div>

          <div className="col-sm-6 mb-3">
            <dt className="text-secondary small mb-1">E-mail</dt>
            <dd className="mb-0">{empresa.email || '—'}</dd>
          </div>

          <div className="col-sm-6 mb-3">
            <dt className="text-secondary small mb-1">Telefone</dt>
            <dd className="mb-0">{empresa.telefone ? maskTelefone(empresa.telefone) : '—'}</dd>
          </div>

          <div className="col-sm-6 mb-3">
            <dt className="text-secondary small mb-1">Fuso horário</dt>
            <dd className="mb-0">{labelFuso}</dd>
          </div>

          <div className="col-sm-6 mb-0">
            <dt className="text-secondary small mb-1">Moeda</dt>
            <dd className="mb-0">{empresa.moeda}</dd>
          </div>

          <div className="col-sm-6 mb-0">
            <dt className="text-secondary small mb-1">Situação</dt>
            <dd className="mb-0">
              <Badge bg={empresa.ativa ? 'success' : 'secondary'}>
                {empresa.ativa ? 'Ativa' : 'Inativa'}
              </Badge>
            </dd>
          </div>
        </dl>
      </Card.Body>
    </Card>
  );
}
