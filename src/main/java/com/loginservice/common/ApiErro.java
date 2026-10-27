package com.loginservice.common;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErro {

    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String mensagem;
    private final Map<String, String> campos;

    public ApiErro(int status, String mensagem) {
        this(status, mensagem, null);
    }

    public ApiErro(int status, String mensagem, Map<String, String> campos) {
        this.status = status;
        this.mensagem = mensagem;
        this.campos = campos;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public Map<String, String> getCampos() {
        return campos;
    }

}
