package br.com.meunegocio360.core.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida um documento brasileiro — CPF (11 dígitos) ou CNPJ (14 dígitos) — conferindo os dígitos
 * verificadores e rejeitando sequências repetidas (RNF-BE-27). O validador é único e reutilizável
 * em {@code shared}; a máscara (pontuação) é tolerada e removida na validação (RN-02, RF-BE-003).
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DocumentoConstraintValidator.class)
public @interface Documento {

  String message() default "deve ser um CPF ou CNPJ válido (somente dígitos)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
