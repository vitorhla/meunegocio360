package br.com.meunegocio360.core.application;

import java.util.UUID;

public record AuthenticatedUser(UUID usuarioId) {
  public static AuthenticatedUser from(String subject) {
    return new AuthenticatedUser(UUID.fromString(subject));
  }
}
