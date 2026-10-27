package com.loginservice.metrica;

import org.springframework.stereotype.Component;

import com.loginservice.login.LoginAttemptRepository;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * Indicadores expostos em /actuator/prometheus:
 *   login_validos_total   -> contador de logins válidos
 *   login_invalidos_total -> contador de logins inválidos
 *   usuarios_bloqueados   -> gauge com o total atual de bloqueados (lido do banco)
 *
 * O total de bloqueados é um Gauge (e não um Counter) porque ele sobe e desce:
 * aumenta quando alguém é bloqueado e diminui quando o admin desbloqueia.
 */
@Component
public class LoginMetricas {

    private final Counter loginsValidos;
    private final Counter loginsInvalidos;

    public LoginMetricas(MeterRegistry registry, LoginAttemptRepository attemptRepository) {

        loginsValidos = Counter.builder("login.validos")
                .description("Quantidade de logins válidos")
                .register(registry);

        loginsInvalidos = Counter.builder("login.invalidos")
                .description("Quantidade de logins inválidos")
                .register(registry);

        Gauge.builder("usuarios.bloqueados", attemptRepository, repo -> repo.countByLockedTrue())
                .description("Total de usuários bloqueados")
                .register(registry);
    }

    public void loginValido() {
        loginsValidos.increment();
    }

    public void loginInvalido() {
        loginsInvalidos.increment();
    }

}
