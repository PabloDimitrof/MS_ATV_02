package com.loginservice.login;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * Controle de tentativas de login por usuário (tabela login_attempts).
 */
@Entity
@Table(name = "login_attempts")
public class LoginAttempt {

    @Id
    private String username;

    @Column(name = "failed_attempts")
    private int failedAttempts;

    private boolean locked;

    @Column(name = "last_failed_at")
    private LocalDateTime lastFailedAt;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected LoginAttempt() {
    }

    public LoginAttempt(String username) {
        this.username = username;
    }

    // registra uma falha e bloqueia ao atingir o limite de tentativas
    public void registrarFalha(int tentativasParaBloqueio) {
        LocalDateTime agora = LocalDateTime.now();
        failedAttempts++;
        lastFailedAt = agora;
        if (failedAttempts >= tentativasParaBloqueio && !locked) {
            locked = true;
            lockedAt = agora;
        }
    }

    // usado no login bem sucedido e no desbloqueio pelo admin
    public void resetar() {
        failedAttempts = 0;
        locked = false;
        lockedAt = null;
    }

    @PrePersist
    @PreUpdate
    void atualizarTimestamp() {
        updatedAt = LocalDateTime.now();
    }

    public String getUsername() {
        return username;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public boolean isLocked() {
        return locked;
    }

    public LocalDateTime getLastFailedAt() {
        return lastFailedAt;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}
