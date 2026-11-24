package br.com.meunegocio360.core.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Regra de validação do {@link Documento} (RNF-BE-27): 11 dígitos (CPF) ou 14 dígitos (CNPJ),
 * dígitos verificadores corretos e rejeição de sequências repetidas (ex.: 111.111.111-11). A
 * pontuação é ignorada — a máscara é responsabilidade do frontend (RN-02).
 */
public class DocumentoConstraintValidator implements ConstraintValidator<Documento, String> {

  private static final int[] PESOS_CPF_PRIMEIRO_DV = {10, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_CPF_SEGUNDO_DV = {11, 10, 9, 8, 7, 6, 5, 4, 3, 2};

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.isBlank()) {
      return false;
    }
    String digitos = value.replaceAll("\\D", "");
    return switch (digitos.length()) {
      case 11 -> cpfValido(digitos);
      case 14 -> CnpjConstraintValidator.valido(digitos);
      default -> false;
    };
  }

  public static boolean cpfValido(String cpf) {
    if (cpf.chars().distinct().count() == 1) {
      return false;
    }
    return digitoVerificador(cpf, 0, 9, PESOS_CPF_PRIMEIRO_DV)
            == Character.getNumericValue(cpf.charAt(9))
        && digitoVerificador(cpf, 0, 10, PESOS_CPF_SEGUNDO_DV)
            == Character.getNumericValue(cpf.charAt(10));
  }

  private static int digitoVerificador(String documento, int inicio, int fim, int[] pesos) {
    int soma = 0;
    for (int i = inicio, p = 0; i < fim; i++, p++) {
      soma += Character.getNumericValue(documento.charAt(i)) * pesos[p];
    }
    int resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  }
}
