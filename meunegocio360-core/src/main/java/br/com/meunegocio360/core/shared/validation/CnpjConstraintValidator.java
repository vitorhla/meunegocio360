package br.com.meunegocio360.core.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Regra de validação do {@link Cnpj} (RNF-BE-27): somente 14 dígitos, dígitos verificadores
 * corretos e rejeição de sequências repetidas (ex.: 11111111111111).
 */
public class CnpjConstraintValidator implements ConstraintValidator<Cnpj, String> {

  private static final int[] PESOS_PRIMEIRO_DV = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
  private static final int[] PESOS_SEGUNDO_DV = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return valido(value);
  }

  public static boolean valido(String cnpj) {
    if (cnpj == null || !cnpj.matches("\\d{14}")) {
      return false;
    }
    if (cnpj.chars().distinct().count() == 1) {
      return false;
    }
    return digitoVerificador(cnpj, 0, 12, PESOS_PRIMEIRO_DV)
            == Character.getNumericValue(cnpj.charAt(12))
        && digitoVerificador(cnpj, 0, 13, PESOS_SEGUNDO_DV)
            == Character.getNumericValue(cnpj.charAt(13));
  }

  private static int digitoVerificador(String cnpj, int inicio, int fim, int[] pesos) {
    int soma = 0;
    for (int i = inicio, p = 0; i < fim; i++, p++) {
      soma += Character.getNumericValue(cnpj.charAt(i)) * pesos[p];
    }
    int resto = soma % 11;
    return resto < 2 ? 0 : 11 - resto;
  }
}
