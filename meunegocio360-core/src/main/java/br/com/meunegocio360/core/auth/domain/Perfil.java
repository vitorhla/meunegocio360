package br.com.meunegocio360.core.auth.domain;

/**
 * Perfis de acesso (RF-BE-002, RN-07). O perfil {@code USUARIO} deixou de existir: usuários com
 * esse perfil foram migrados para {@code OPERADOR_COMERCIAL} pela migration V3.
 */
public enum Perfil {
  ADMIN,
  OPERADOR_COMERCIAL,
  OPERADOR_FINANCEIRO
}
