package com.loginservice.config;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Existem dois "tipos" de usuario no sistema:
 *
 * 1) Usuarios do sistema generico (tabela "usuarios"): sao os que fazem
 *    POST /login, trocam senha e podem ser bloqueados. A validacao deles e
 *    feita pelo proprio LoginService, consultando a tabela.
 *
 * 2) Operadores da API (tabelas "users" / "authorities" do
 *    JdbcUserDetailsManager): sao quem se autentica via HTTP Basic para usar
 *    os endpoints protegidos (ROLE_MANUTENCAO e ROLE_ADMIN).
 *
 * Obs: o formLogin() NAO foi habilitado porque ele registra o seu proprio
 * POST /login, o que conflitaria com o endpoint de login pedido no enunciado.
 */
@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // endpoints publicos do enunciado
                        .requestMatchers(antMatcher(HttpMethod.POST, "/login")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.PUT, "/trocarsenha/**")).permitAll()
                        // ferramentas (swagger, h2, actuator/prometheus e pagina de erro)
                        .requestMatchers(antMatcher("/swagger-ui/**"), antMatcher("/swagger-ui.html"),
                                antMatcher("/v3/api-docs/**")).permitAll()
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .requestMatchers(antMatcher("/actuator/**")).permitAll()
                        .requestMatchers(antMatcher("/error")).permitAll()
                        // endpoints protegidos por role
                        .requestMatchers(antMatcher(HttpMethod.PUT, "/unlock/**")).hasRole("ADMIN")
                        .requestMatchers(antMatcher(HttpMethod.GET, "/"), antMatcher(HttpMethod.POST, "/"))
                        .hasRole("MANUTENCAO")
                        .anyRequest().authenticated());

        return http.build();
    }

    // usuarios/roles dos operadores ficam nas tabelas users e authorities (ver data.sql)
    @Bean
    public JdbcUserDetailsManager userDetailsService(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

}
