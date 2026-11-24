-- RF-BE-004: cadastro de fornecedores.
-- Todo fornecedor pertence à empresa ativa (RF-BE-003 RN-01, RNF-096); a empresa NUNCA é
-- informada pelo cliente (resolvida no servidor). O documento é único por empresa,
-- considerando fornecedores ativos E inativos (RN-02), POR ENTIDADE: o mesmo CNPJ PODE
-- existir como cliente e como fornecedor.
CREATE TABLE fornecedor (
    id              BINARY(16)     NOT NULL,
    empresa_id      BINARY(16)     NOT NULL,
    tipo_pessoa     VARCHAR(10)    NOT NULL,
    nome            VARCHAR(150)   NOT NULL,
    documento       VARCHAR(14)    NOT NULL,
    email           VARCHAR(160)   NULL,
    telefone        VARCHAR(11)    NULL,
    ativo           BOOLEAN        NOT NULL DEFAULT TRUE,
    criado_em       DATETIME(6)    NOT NULL,
    atualizado_em   DATETIME(6)    NOT NULL,
    versao          BIGINT         NOT NULL,
    -- RF-BE-003 RN-08 / RNF-BE-26: busca por nome sem distinção de maiúsculas e acentos.
    -- MariaDB 10.4 não possui collation insensível a acento (utf8mb4_0900_ai_ci é exclusiva
    -- do MySQL 8), então a busca usa esta coluna gerada (minúsculas, sem acento). MariaDB
    -- 10.4 não aceita NOT NULL em coluna gerada (limitação do dialecto); na prática nunca
    -- é NULL porque nome é NOT NULL.
    nome_busca        VARCHAR(150)
                    GENERATED ALWAYS AS (
                        LOWER(
                            REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                                REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                                    nome,
                                    'á', 'a'),
                                    'à', 'a'),
                                    'â', 'a'),
                                    'ã', 'a'),
                                    'é', 'e'),
                                    'ê', 'e'),
                                    'í', 'i'),
                                    'ó', 'o'),
                                    'ô', 'o'),
                                    'ú', 'u'),
                                    'ü', 'u'),
                                    'ç', 'c'),
                                    'Á', 'a'),
                                    'À', 'a'),
                                    'Â', 'a'),
                                    'Ã', 'a'),
                                    'É', 'e'),
                                    'Ê', 'e'),
                                    'Í', 'i'),
                                    'Ó', 'o'),
                                    'Ô', 'o'),
                                    'Ú', 'u'),
                                    'Ü', 'u'),
                                    'Ç', 'c')
                        )
                    ) STORED,
    CONSTRAINT pk_fornecedor PRIMARY KEY (id),
    CONSTRAINT fk_fornecedor_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT uk_fornecedor_empresa_documento UNIQUE (empresa_id, documento)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- RF-BE-003 RN-08 / RNF-BE-26: índices de busca escopados pela empresa (RNF-BE-23/26).
CREATE INDEX idx_fornecedor_empresa_nome ON fornecedor (empresa_id, nome);
CREATE INDEX idx_fornecedor_empresa_ativo ON fornecedor (empresa_id, ativo);
CREATE INDEX idx_fornecedor_empresa_nome_busca ON fornecedor (empresa_id, nome_busca);
