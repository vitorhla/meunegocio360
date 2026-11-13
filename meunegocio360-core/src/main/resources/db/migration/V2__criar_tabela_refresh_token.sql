CREATE TABLE refresh_token (
    id              BINARY(16)      NOT NULL,
    usuario_id      BINARY(16)      NOT NULL,
    familia_id      BINARY(16)      NOT NULL,
    token_hash      CHAR(64)        NOT NULL,
    expira_em       DATETIME(6)     NOT NULL,
    revogado_em     DATETIME(6)     NULL,
    criado_em       DATETIME(6)     NOT NULL,
    CONSTRAINT pk_refresh_token PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_token_hash UNIQUE (token_hash),
    CONSTRAINT fk_refresh_token_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_refresh_token_usuario_id ON refresh_token (usuario_id);
CREATE INDEX idx_refresh_token_familia_id ON refresh_token (familia_id);
