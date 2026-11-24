package br.com.meunegocio360.core.shared.domain;

/**
 * Tipo da pessoa em cadastros com documento (RF-BE-003/004, RN-02). {@code FISICA} usa CPF (11
 * dígitos); {@code JURIDICA} usa CNPJ (14 dígitos). Compartilhado entre clientes e fornecedores —
 * os cadastros são independentes, mas a regra de pessoa é a mesma (RNF-BE-27).
 */
public enum TipoPessoa {
  FISICA,
  JURIDICA
}
