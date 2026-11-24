// ============================================================
// PrecoInput — input de preço com máscara de moeda pt-BR
// (RNF-FE-28). O estado do RHF guarda o decimal em string
// (ex.: "89.9"), nunca float (RNF-033).
// ============================================================
import { Controller } from 'react-hook-form';
import { Form } from 'react-bootstrap';
import { maskMoedaBR, textoParaDecimal } from '../lib/moeda.js';

/**
 * Campo de preço (R$) com máscara pt-BR e inputMode decimal.
 *
 * @param {object}    props
 * @param {object}    props.control   — control do RHF
 * @param {string}    props.name      — nome do campo no formulário
 * @param {string}    [props.id]
 * @param {boolean}   [props.isInvalid]
 * @param {string}    [props.describedBy]
 * @param {boolean}   [props.disabled]
 */
export default function PrecoInput({
  control,
  name,
  id,
  isInvalid,
  describedBy,
  disabled,
}) {
  return (
    <Controller
      control={control}
      name={name}
      render={({ field }) => (
        <Form.Control
          id={id}
          type="text"
          inputMode="decimal"
          autoComplete="off"
          placeholder="0,00"
          value={maskMoedaBR(field.value ?? '')}
          onChange={(e) => field.onChange(textoParaDecimal(e.target.value))}
          isInvalid={isInvalid}
          aria-describedby={describedBy}
          disabled={disabled}
        />
      )}
    />
  );
}
