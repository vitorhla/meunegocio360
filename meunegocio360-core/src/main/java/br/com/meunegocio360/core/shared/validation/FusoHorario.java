package br.com.meunegocio360.core.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida um identificador de fuso horário IANA (ex.: {@code America/Sao_Paulo}), conforme a RN-05
 * do RF-BE-002. Campo opcional: valores em branco são tratados como padrão.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FusoHorarioConstraintValidator.class)
public @interface FusoHorario {

  String message() default "deve ser um identificador IANA válido (ex.: America/Sao_Paulo)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
