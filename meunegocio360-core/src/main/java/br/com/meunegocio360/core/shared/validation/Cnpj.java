package br.com.meunegocio360.core.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida um CNPJ composto somente pelos 14 dígitos (sem pontuação), conferindo os dígitos
 * verificadores e rejeitando sequências repetidas (RNF-BE-27, RF-BE-002). A máscara é
 * responsabilidade do frontend.
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CnpjConstraintValidator.class)
public @interface Cnpj {

  String message() default "deve ser um CNPJ válido (14 dígitos, sem pontuação)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
