package com.loginservice;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

/**
 * Cada teste usa um usuário diferente do data.sql para não interferir nos outros.
 */
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability // habilita o registry do Prometheus nos testes (vem desligado por padrão)
class LoginServiceApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void loginValidoRetorna200() throws Exception {
        mvc.perform(json(post("/login"), "{\"username\":\"maria\",\"senha\":\"abc123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void usuarioInexistenteRetorna401() throws Exception {
        mvc.perform(json(post("/login"), "{\"username\":\"fantasma\",\"senha\":\"x\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void bloqueiaAposDuasFalhasESoAdminDesbloqueia() throws Exception {
        String errado = "{\"username\":\"joao\",\"senha\":\"errada\"}";
        String certo = "{\"username\":\"joao\",\"senha\":\"123456\"}";

        mvc.perform(json(post("/login"), errado)).andExpect(status().isUnauthorized());
        mvc.perform(json(post("/login"), errado)).andExpect(status().isUnauthorized());

        // bloqueado: nem a senha certa funciona
        mvc.perform(json(post("/login"), certo)).andExpect(status().isUnauthorized());

        // indicadores publicados para o Prometheus
        mvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("login_validos_total")))
                .andExpect(content().string(containsString("login_invalidos_total")))
                .andExpect(content().string(containsString("usuarios_bloqueados")));

        // manutencao não pode desbloquear
        mvc.perform(put("/unlock/joao").with(httpBasic("manutencao", "manutencao")))
                .andExpect(status().isForbidden());

        // admin pode
        mvc.perform(put("/unlock/joao").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk());

        mvc.perform(json(post("/login"), certo)).andExpect(status().isOk());
    }

    @Test
    void listarUsuariosExigeRoleManutencao() throws Exception {
        mvc.perform(get("/")).andExpect(status().isUnauthorized());
        mvc.perform(get("/").with(httpBasic("admin", "admin"))).andExpect(status().isForbidden());
        mvc.perform(get("/").with(httpBasic("manutencao", "manutencao")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").exists())
                .andExpect(jsonPath("$[0].senha").doesNotExist());
    }

    @Test
    void cadastrarUsuarioExigeRoleManutencao() throws Exception {
        String novo = "{\"username\":\"carla\",\"senha\":\"c4rla\"}";

        mvc.perform(json(post("/"), novo).with(httpBasic("admin", "admin"))).andExpect(status().isForbidden());
        mvc.perform(json(post("/"), novo).with(httpBasic("manutencao", "manutencao"))).andExpect(status().isCreated());
        mvc.perform(json(post("/"), novo).with(httpBasic("manutencao", "manutencao"))).andExpect(status().isConflict());

        mvc.perform(json(post("/login"), novo)).andExpect(status().isOk());
    }

    @Test
    void trocarSenhaConfereSenhaAtual() throws Exception {
        mvc.perform(json(put("/trocarsenha/pedro"), "{\"atual\":\"errada\",\"nova\":\"nova1\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(json(put("/trocarsenha/pedro"), "{\"atual\":\"senha1\",\"nova\":\"nova1\"}"))
                .andExpect(status().isOk());
        mvc.perform(json(post("/login"), "{\"username\":\"pedro\",\"senha\":\"nova1\"}"))
                .andExpect(status().isOk());
        mvc.perform(json(put("/trocarsenha/ninguem"), "{\"atual\":\"a\",\"nova\":\"b\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void swaggerEstaPublico() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    private MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
    }

}
