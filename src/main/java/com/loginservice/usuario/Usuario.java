package com.loginservice.usuario;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "Usuario", description = "Usuário do sistema (tabela usuarios)")
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @NotBlank(message = "O username é obrigatório")
    @Size(max = 100, message = "O username deve ter no máximo 100 caracteres")
    @Schema(description = "Identificador do usuário", example = "carla")
    private String username;

    // WRITE_ONLY: a senha é aceita no cadastro, mas nunca volta no GET /
    @NotBlank(message = "A senha é obrigatória")
    @Size(max = 10, message = "A senha deve ter no máximo 10 caracteres")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Schema(description = "Senha (máx. 10 caracteres)", example = "s3nh4", accessMode = Schema.AccessMode.WRITE_ONLY)
    private String senha;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

}
