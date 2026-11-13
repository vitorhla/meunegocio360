package br.com.meunegocio360.core.auth.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
    @NotBlank @Email(message = "deve ser um e-mail válido") @Size(max = 160) String email,
    @NotBlank @Size(min = 8, max = 72) String senha) {}
