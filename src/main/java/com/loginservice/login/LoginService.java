package com.loginservice.login;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginservice.common.CredenciaisInvalidasException;
import com.loginservice.common.RecursoNaoEncontradoException;
import com.loginservice.metrica.LoginMetricas;
import com.loginservice.usuario.Usuario;
import com.loginservice.usuario.UsuarioRepository;

@Service
public class LoginService {

    public enum ResultadoLogin {
        SUCESSO, CREDENCIAIS_INVALIDAS, BLOQUEADO
    }

    private final UsuarioRepository usuarioRepository;
    private final LoginAttemptRepository attemptRepository;
    private final LoginMetricas metricas;
    private final int tentativasParaBloqueio;

    public LoginService(UsuarioRepository usuarioRepository, LoginAttemptRepository attemptRepository,
            LoginMetricas metricas, @Value("${app.login.tentativas-para-bloqueio:2}") int tentativasParaBloqueio) {
        this.usuarioRepository = usuarioRepository;
        this.attemptRepository = attemptRepository;
        this.metricas = metricas;
        this.tentativasParaBloqueio = tentativasParaBloqueio;
    }

    @Transactional
    public ResultadoLogin autenticar(LoginRequest request) {

        Optional<LoginAttempt> tentativa = attemptRepository.findById(request.username());

        // usuário bloqueado não entra nem com a senha certa
        if (tentativa.map(LoginAttempt::isLocked).orElse(false)) {
            metricas.loginInvalido();
            return ResultadoLogin.BLOQUEADO;
        }

        Optional<Usuario> usuario = usuarioRepository.findById(request.username());

        if (usuario.isPresent() && usuario.get().getSenha().equals(request.senha())) {
            // login ok: zera o contador de falhas
            tentativa.ifPresent(t -> {
                t.resetar();
                attemptRepository.save(t);
            });
            metricas.loginValido();
            return ResultadoLogin.SUCESSO;
        }

        metricas.loginInvalido();

        // só registra tentativa para usuários que existem (evita poluir a tabela com nomes aleatórios)
        if (usuario.isPresent()) {
            LoginAttempt registro = tentativa.orElseGet(() -> new LoginAttempt(request.username()));
            registro.registrarFalha(tentativasParaBloqueio);
            attemptRepository.save(registro);
            if (registro.isLocked()) {
                return ResultadoLogin.BLOQUEADO;
            }
        }

        return ResultadoLogin.CREDENCIAIS_INVALIDAS;
    }

    @Transactional
    public void trocarSenha(String userid, TrocaSenhaRequest request) {

        Usuario usuario = buscarUsuario(userid);

        if (estaBloqueado(userid)) {
            throw new CredenciaisInvalidasException("Usuário bloqueado. Solicite o desbloqueio a um administrador");
        }
        if (!usuario.getSenha().equals(request.atual())) {
            throw new CredenciaisInvalidasException("Senha atual incorreta");
        }

        usuario.setSenha(request.nova());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void desbloquear(String userid) {

        buscarUsuario(userid);

        attemptRepository.findById(userid).ifPresent(t -> {
            t.resetar();
            attemptRepository.save(t);
        });
    }

    private boolean estaBloqueado(String username) {
        return attemptRepository.findById(username).map(LoginAttempt::isLocked).orElse(false);
    }

    private Usuario buscarUsuario(String userid) {
        return usuarioRepository.findById(userid)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + userid + " não encontrado"));
    }

}
