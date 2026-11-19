-- RF-BE-002: cadastro da empresa (modelo de empresa única ativa, RNF-096).
-- É a única tabela sem empresa_id; todas as tabelas de negócio posteriores a referenciam
-- (RNF-BE-26).
CREATE TABLE empresa (
    id              BINARY(16)     NOT NULL,
    razao_social    VARCHAR(150)   NOT NULL,
    nome_fantasia   VARCHAR(150)   NULL,
    cnpj            CHAR(14)       NOT NULL,
    email           VARCHAR(160)   NULL,
    telefone        VARCHAR(11)    NULL,
    fuso_horario    VARCHAR(50)    NOT NULL DEFAULT 'America/Sao_Paulo',
    moeda           CHAR(3)        NOT NULL DEFAULT 'BRL',
    ativa           BOOLEAN        NOT NULL DEFAULT TRUE,
    ativa_unica     TINYINT        GENERATED ALWAYS AS (CASE WHEN ativa THEN 1 END) STORED,
    criado_em       DATETIME(6)    NOT NULL,
    atualizado_em   DATETIME(6)    NOT NULL,
    versao          BIGINT         NOT NULL,
    CONSTRAINT pk_empresa PRIMARY KEY (id),
    CONSTRAINT uk_empresa_cnpj UNIQUE (cnpj)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- RN-02 garantida no banco (não só por if na aplicação): linhas inativas têm
-- ativa_unica = NULL e o índice único aceita vários NULLs, logo no máximo uma
-- empresa pode ter ativa = TRUE.
CREATE UNIQUE INDEX uk_empresa_ativa_unica ON empresa (ativa_unica);
