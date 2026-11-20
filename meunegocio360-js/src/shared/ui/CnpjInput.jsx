// ============================================================
// CnpjInput — input com máscara de CNPJ via Controller do RHF
// O estado do RHF guarda SOMENTE dígitos (RNF-FE-28).
// NUNCA usa ref manual em campo (lição FE-001).
// ============================================================
import { Controller } from 'react-hook-form';
import { Form } from 'react-bootstrap';
import { maskCnpj, somenteDigitos } from '../lib/cnpj.js';

/**
 * Input de CNPJ com máscara progressiva 00.000.000/0000-00.
 *
 * @param {object}    props
 * @param {object}    props.control     — control do RHF
 * @param {string}    props.name        — nome do campo no formulário
 * @param {string}    [props.id]
 * @param {boolean}   [props.isInvalid]
 * @param {string}    [props.describedBy]
 * @param {boolean}   [props.disabled]
 */
export default function CnpjInput({
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
          inputMode="numeric"
          autoComplete="off"
          placeholder="00.000.000/0000-00"
          value={maskCnpj(field.value ?? '')}
          onChange={(e) => field.onChange(somenteDigitos(e.target.value).slice(0, 14))}
          isInvalid={isInvalid}
          aria-describedby={describedBy}
          disabled={disabled}
        />
      )}
    />
  );
}
