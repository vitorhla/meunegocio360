package br.com.meunegocio360.core.fornecedor.api;

import br.com.meunegocio360.core.shared.domain.TipoPessoa;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Implementação do {@link FornecedorDocumentoValido} (RF-BE-004, RF-BE-003 RN-02): {@code FISICA}
 * exige 11 dígitos (CPF) e {@code JURIDICA} exige 14 (CNPJ), contando os dígitos após remover a
 * máscara. O dígito verificador é conferido pela anotação de campo {@code @Documento}.
 */
public class FornecedorDocumentoValidoConstraintValidator
    implements ConstraintValidator<FornecedorDocumentoValido, FornecedorRequest> {

  @Override
  public boolean isValid(FornecedorRequest request, ConstraintValidatorContext context) {
    if (request == null || request.tipoPessoa() == null || request.documento() == null) {
      // Falta de tipoPessoa/documento já é reportado pelas anotações de campo.
      return true;
    }
    int esperados = request.tipoPessoa() == TipoPessoa.FISICA ? 11 : 14;
    String digitos = request.documento().replaceAll("\\D", "");
    if (digitos.length() == esperados) {
      return true;
    }
    context.disableDefaultConstraintViolation();
    context
        .buildConstraintViolationWithTemplate(
            "deve ter "
                + esperados
                + " dígitos ("
                + (esperados == 11 ? "CPF" : "CNPJ")
                + ") para o tipoPessoa informado")
        .addPropertyNode("documento")
        .addConstraintViolation();
    return false;
  }
}
