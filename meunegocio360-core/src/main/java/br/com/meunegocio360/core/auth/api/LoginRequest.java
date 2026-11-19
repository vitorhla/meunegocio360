package br.com.meunegocio360.core.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(
    description = "Credenciais de login: e-mail e senha (RF-BE-001).",
    example = "{\"email\":\"admin@meunegocio360.com.br\",\"senha\":\"Admin123!\"}")
public record LoginRequest(
    @Schema(description = "E-mail do usuário.", example = "admin@meunegocio360.com.br")
        @NotBlank @Email(message = "deve ser um e-mail válido") @Size(max = 160) String email,
    @Schema(
        description = "Senha. Mínimo 8 e máximo 72 caracteres (limite do BCrypt).",
        example = "Admin123!",
        hidden = true)
        @NotBlank @Size(min = 8, max = 72) String senha) {}
