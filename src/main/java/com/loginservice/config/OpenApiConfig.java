package com.loginservice.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Configuracao da documentacao OpenAPI.
 * O SecurityScheme "basicAuth" faz aparecer o botao "Authorize" no Swagger,
 * permitindo testar os endpoints protegidos (GET /, POST /, PUT /unlock).
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "Login Service", version = "1.0.0",
        description = "MS-AV-02 - Login com bloqueio por tentativas, controle de acesso por roles e métricas para o Grafana"))
@SecurityScheme(name = "basicAuth", type = SecuritySchemeType.HTTP, scheme = "basic")
public class OpenApiConfig {
}
