package com.loginservice.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para login")
public record LoginRequest(
        @NotBlank(message = "O username é obrigatório") @Schema(example = "maria") String username,
        @NotBlank(message = "A senha é obrigatória") @Schema(example = "abc123") String senha) {
}
