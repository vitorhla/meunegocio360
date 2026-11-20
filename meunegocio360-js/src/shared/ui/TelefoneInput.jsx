// ============================================================
// TelefoneInput — input com máscara de telefone via Controller do RHF
// O estado do RHF guarda SOMENTE dígitos (RNF-FE-28).
// NUNCA usa ref manual em campo (lição FE-001).
// ============================================================
import { Controller } from 'react-hook-form';
import { Form } from 'react-bootstrap';
import { maskTelefone, somenteDigitos } from '../lib/telefone.js';

/**
 * Input de telefone com máscara progressiva (00) 00000-0000.
 *
 * @param {object}    props
 * @param {object}    props.control     — control do RHF
 * @param {string}    props.name        — nome do campo no formulário
 * @param {string}    [props.id]
 * @param {boolean}   [props.isInvalid]
 * @param {string}    [props.describedBy]
 * @param {boolean}   [props.disabled]
 */
export default function TelefoneInput({
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
          type="tel"
          autoComplete="tel"
          placeholder="(00) 00000-0000"
          value={maskTelefone(field.value ?? '')}
          onChange={(e) => field.onChange(somenteDigitos(e.target.value).slice(0, 11))}
          isInvalid={isInvalid}
          aria-describedby={describedBy}
          disabled={disabled}
        />
      )}
    />
  );
}
