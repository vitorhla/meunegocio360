-- RF-BE-005: catálogo de produtos e serviços.
-- Toda categoria e todo item pertencem à empresa ativa (RN-01, RNF-096); a empresa NUNCA é
-- informada pelo cliente (resolvida no servidor).

CREATE TABLE categoria (
    id              BINARY(16)     NOT NULL,
    empresa_id      BINARY(16)     NOT NULL,
    nome            VARCHAR(80)    NOT NULL,
    ativo           BOOLEAN        NOT NULL DEFAULT TRUE,
    criado_em       DATETIME(6)    NOT NULL,
    atualizado_em   DATETIME(6)    NOT NULL,
    versao          BIGINT         NOT NULL,
    -- RN-06 / RNF-BE-26: nome único por empresa, sem distinguir maiúsculas e acentos. MariaDB
    -- 10.4 não possui collation insensível a acento (utf8mb4_0900_ai_ci é exclusiva do MySQL
    -- 8), então a unicidade recai sobre esta coluna gerada (minúsculas, sem acento) — mesmo
    -- padrão de cliente/fornecedor (V5/V6). MariaDB 10.4 não aceita NOT NULL em coluna gerada;
    -- na prática nunca é NULL porque nome é NOT NULL.
    nome_busca        VARCHAR(80)
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
    CONSTRAINT pk_categoria PRIMARY KEY (id),
    CONSTRAINT fk_categoria_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    CONSTRAINT uk_categoria_empresa_nome UNIQUE (empresa_id, nome_busca)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- RNF-BE-23/26: índices de busca escopados pela empresa.
CREATE INDEX idx_categoria_empresa_nome ON categoria (empresa_id, nome);
CREATE INDEX idx_categoria_empresa_ativo ON categoria (empresa_id, ativo);

CREATE TABLE item_catalogo (
    id              BINARY(16)     NOT NULL,
    empresa_id      BINARY(16)     NOT NULL,
    categoria_id    BINARY(16)     NULL,
    tipo            VARCHAR(10)    NOT NULL,
    codigo          VARCHAR(40)    NULL,
    nome            VARCHAR(150)   NOT NULL,
    descricao       VARCHAR(500)   NULL,
    unidade         VARCHAR(6)     NOT NULL,
    -- RN-03: preço obrigatório, não negativo (0 permitido) e limitado a 99999999.99
    -- (precisão da coluna), reforçado pelo CHECK no banco (RNF-BE-23).
    preco           DECIMAL(12,2)  NOT NULL,
    -- RN-07: a coluna já existe no banco, mas NENHUMA API/DTO a lê ou grava nesta fase
    -- (será usada por Suprimentos).
    estoque_minimo  DECIMAL(15,3)  NOT NULL DEFAULT 0,
    ativo           BOOLEAN        NOT NULL DEFAULT TRUE,
    criado_em       DATETIME(6)    NOT NULL,
    atualizado_em   DATETIME(6)    NOT NULL,
    versao          BIGINT         NOT NULL,
    -- RN-10: busca por nome ou código, sem distinguir maiúsculas e acentos — colunas geradas
    -- (minúsculas, sem acento), mesmo padrão de cliente/fornecedor (V5/V6). Nulas quando a
    -- coluna de origem é NULL (categoria_id, codigo).
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
    -- RN-10: busca por nome ou código, sem distinguir maiúsculas e acentos — colunas geradas
    -- (minúsculas, sem acento), mesmo padrão de cliente/fornecedor (V5/V6). Nulas quando a
    -- coluna de origem é NULL (categoria_id, codigo).
    codigo_busca      VARCHAR(40)
                    GENERATED ALWAYS AS (
                        LOWER(
                            REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                                REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(
                                    codigo,
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
    CONSTRAINT pk_item_catalogo PRIMARY KEY (id),
    CONSTRAINT fk_item_catalogo_empresa FOREIGN KEY (empresa_id) REFERENCES empresa (id),
    -- Sem ON DELETE em cascata (RNF-BE-23): excluir a categoria com itens é bloqueado pela
    -- aplicação (RN-06, CATEGORY_IN_USE).
    CONSTRAINT fk_item_catalogo_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
    -- RN-04: código único por empresa quando informado; o MariaDB aceita vários NULLs em
    -- índice único.
    CONSTRAINT uk_item_catalogo_empresa_codigo UNIQUE (empresa_id, codigo),
    CONSTRAINT chk_item_catalogo_preco CHECK (preco >= 0),
    CONSTRAINT chk_item_catalogo_estoque_minimo CHECK (estoque_minimo >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- RNF-BE-23/26: índices de busca escopados pela empresa.
CREATE INDEX idx_item_catalogo_empresa_tipo_ativo ON item_catalogo (empresa_id, tipo, ativo);
CREATE INDEX idx_item_catalogo_empresa_nome ON item_catalogo (empresa_id, nome);
CREATE INDEX idx_item_catalogo_empresa_codigo_busca ON item_catalogo (empresa_id, codigo_busca);
