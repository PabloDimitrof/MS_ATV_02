package com.loginservice.usuario;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.loginservice.common.ConflitoException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Usuários", description = "Manutenção de usuários (requer ROLE_MANUTENCAO)")
@SecurityRequirement(name = "basicAuth")
@RestController
public class UsuarioController {

    private final UsuarioRepository repository;

    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    // GET / -> somente ROLE_MANUTENCAO
    @Operation(summary = "Lista os usuários cadastrados", description = "A senha não é retornada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de usuários",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = Usuario.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content) })
    @GetMapping("/")
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(repository.findAll());
    }

    // POST / -> somente ROLE_MANUTENCAO
    @Operation(summary = "Cadastra um novo usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário criado",
                    content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_MANUTENCAO", content = @Content),
            @ApiResponse(responseCode = "409", description = "Username já existe", content = @Content) })
    @PostMapping("/")
    public ResponseEntity<Usuario> cadastrar(@Valid @RequestBody Usuario usuario) {

        if (repository.existsById(usuario.getUsername())) {
            throw new ConflitoException("Usuário " + usuario.getUsername() + " já existe");
        }
        Usuario salvo = repository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

}
