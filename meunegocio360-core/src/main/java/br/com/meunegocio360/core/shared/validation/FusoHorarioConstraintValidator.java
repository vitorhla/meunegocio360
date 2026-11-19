package br.com.meunegocio360.core.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.DateTimeException;
import java.time.ZoneId;

/** Regra de validação do {@link FusoHorario} (RN-05 do RF-BE-002). */
public class FusoHorarioConstraintValidator implements ConstraintValidator<FusoHorario, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return true;
    }
    try {
      ZoneId.of(value);
      return true;
    } catch (DateTimeException e) {
      return false;
    }
  }
}
