DROP TABLE IF EXISTS login_attempts;
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS authorities;
DROP TABLE IF EXISTS users;

-- Script do enunciado (removida a virgula sobrando depois de updated_at, que dava erro de sintaxe)
CREATE TABLE IF NOT EXISTS login_attempts (
    username VARCHAR(100) PRIMARY KEY,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    last_failed_at TIMESTAMP NULL,
    locked_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS usuarios (
    username VARCHAR(100) PRIMARY KEY,
    senha VARCHAR(10)
);

-- Usuarios do sistema (fazem POST /login)
INSERT INTO usuarios (username, senha) VALUES ('joao', '123456');
INSERT INTO usuarios (username, senha) VALUES ('maria', 'abc123');
INSERT INTO usuarios (username, senha) VALUES ('pedro', 'senha1');
INSERT INTO usuarios (username, senha) VALUES ('ana', 'ana2026');

-- Schema padrao do JdbcUserDetailsManager (operadores da API e suas roles)
CREATE TABLE users (
    username VARCHAR(50) NOT NULL PRIMARY KEY,
    password VARCHAR(500) NOT NULL,
    enabled BOOLEAN NOT NULL
);

CREATE TABLE authorities (
    username VARCHAR(50) NOT NULL,
    authority VARCHAR_IGNORECASE(50) NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (username) REFERENCES users(username)
);

CREATE UNIQUE INDEX ix_auth_username ON authorities (username, authority);

INSERT INTO users (username, password, enabled) VALUES ('manutencao', '{noop}manutencao', true);
INSERT INTO authorities (username, authority) VALUES ('manutencao', 'ROLE_MANUTENCAO');

INSERT INTO users (username, password, enabled) VALUES ('admin', '{noop}admin', true);
INSERT INTO authorities (username, authority) VALUES ('admin', 'ROLE_ADMIN');
