package com.loginservice.login;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "Login", description = "Autenticação, troca de senha e desbloqueio de usuários")
@RestController
public class LoginController {

    private final LoginService service;

    public LoginController(LoginService service) {
        this.service = service;
    }

    // POST /login -> público
    @Operation(summary = "Efetua login",
            description = "Valida username e senha na tabela usuarios. Após 2 falhas seguidas o usuário é bloqueado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário e senha válidos",
                    content = @Content(schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário/senha inválidos ou usuário bloqueado",
                    content = @Content(schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Corpo da requisição inválido", content = @Content) })
    @PostMapping("/login")
    public ResponseEntity<MensagemResponse> login(@Valid @RequestBody LoginRequest request) {

        return switch (service.autenticar(request)) {
            case SUCESSO -> ResponseEntity.ok(new MensagemResponse("Login efetuado com sucesso"));
            case BLOQUEADO -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensagemResponse("Usuário bloqueado. Procure um administrador"));
            case CREDENCIAIS_INVALIDAS -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MensagemResponse("Usuário ou senha inválidos"));
        };
    }

    // PUT /trocarsenha/{userid} -> público
    @Operation(summary = "Troca a senha do usuário",
            description = "Confere a senha atual com a base de dados antes de gravar a nova senha")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Senha alterada",
                    content = @Content(schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Senha atual incorreta ou usuário bloqueado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content) })
    @PutMapping("/trocarsenha/{userid}")
    public ResponseEntity<MensagemResponse> trocarSenha(
            @Parameter(description = "username do usuário", example = "pedro") @PathVariable String userid,
            @Valid @RequestBody TrocaSenhaRequest request) {

        service.trocarSenha(userid, request);
        return ResponseEntity.ok(new MensagemResponse("Senha alterada com sucesso"));
    }

    // PUT /unlock/{userid} -> somente ROLE_ADMIN
    @Operation(summary = "Desbloqueia um usuário", description = "Requer a role ROLE_ADMIN")
    @SecurityRequirement(name = "basicAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário desbloqueado",
                    content = @Content(schema = @Schema(implementation = MensagemResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem a role ROLE_ADMIN", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content) })
    @PutMapping("/unlock/{userid}")
    public ResponseEntity<MensagemResponse> desbloquear(
            @Parameter(description = "username do usuário", example = "joao") @PathVariable String userid) {

        service.desbloquear(userid);
        return ResponseEntity.ok(new MensagemResponse("Usuário " + userid + " desbloqueado"));
    }

}
