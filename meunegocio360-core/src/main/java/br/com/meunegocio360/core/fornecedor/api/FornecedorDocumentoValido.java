package br.com.meunegocio360.core.fornecedor.api;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Regra de classe do cadastro de fornecedor (RF-BE-004, RF-BE-003 RN-02): o tamanho do documento
 * (após remover a máscara) deve ser compatível com o {@code tipoPessoa} — 11 dígitos (CPF) para
 * {@code FISICA} e 14 (CNPJ) para {@code JURIDICA}. A violação é reportada no campo {@code
 * documento}.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FornecedorDocumentoValidoConstraintValidator.class)
public @interface FornecedorDocumentoValido {

  String message() default "tamanho do documento incompatível com o tipoPessoa";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
