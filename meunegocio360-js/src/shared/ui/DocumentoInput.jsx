// ============================================================
// DocumentoInput — input com máscara de CPF ou CNPJ conforme o
// tipo de pessoa, via Controller do RHF (RNF-FE-28).
// O estado do RHF guarda SOMENTE dígitos.
// NUNCA usa ref manual em campo (lição FE-001).
// ============================================================
import { Controller } from 'react-hook-form';
import { Form } from 'react-bootstrap';
import { somenteDigitos } from '../lib/cnpj.js';
import {
  TipoPessoa,
  maskDocumento,
  maxDigitosDocumento,
} from '../lib/tipoPessoa.js';

/**
 * Input de documento com máscara progressiva que muda conforme
 * o tipo de pessoa (CPF: 000.000.000-00; CNPJ: 00.000.000/0000-00).
 *
 * @param {object}    props
 * @param {object}    props.control   — control do RHF
 * @param {string}    props.name      — nome do campo no formulário
 * @param {string}    [props.id]
 * @param {string}    props.tipoPessoa — FISICA | JURIDICA
 * @param {boolean}   [props.isInvalid]
 * @param {string}    [props.describedBy]
 * @param {boolean}   [props.disabled]
 */
export default function DocumentoInput({
  control,
  name,
  id,
  tipoPessoa,
  isInvalid,
  describedBy,
  disabled,
}) {
  const max = maxDigitosDocumento(tipoPessoa);
  const placeholder =
    tipoPessoa === TipoPessoa.JURIDICA
      ? '00.000.000/0000-00'
      : '000.000.000-00';

  return (
    <Controller
      control={control}
      name={name}
      render={({ field }) => (
        <Form.Control
          id={id}
          type="text"
          inputMode="numeric"
          autoComplete="off"
          placeholder={placeholder}
          value={maskDocumento(tipoPessoa, field.value ?? '')}
          onChange={(e) =>
            field.onChange(somenteDigitos(e.target.value).slice(0, max))
          }
          isInvalid={isInvalid}
          aria-describedby={describedBy}
          disabled={disabled}
        />
      )}
    />
  );
}
