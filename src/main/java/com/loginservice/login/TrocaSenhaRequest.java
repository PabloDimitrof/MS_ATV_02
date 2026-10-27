package com.loginservice.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para troca de senha")
public record TrocaSenhaRequest(
        @NotBlank(message = "A senha atual é obrigatória") @Schema(example = "senha1") String atual,
        @NotBlank(message = "A nova senha é obrigatória")
        @Size(max = 10, message = "A nova senha deve ter no máximo 10 caracteres")
        @Schema(example = "nova123") String nova) {
}
