package com.loginservice.login;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, String> {

    // usado pelo gauge de usuários bloqueados
    long countByLockedTrue();

}
