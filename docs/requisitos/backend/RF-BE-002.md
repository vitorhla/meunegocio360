# RF-BE-002 — Cadastro de empresa, parâmetros básicos e perfis de acesso

> **Branch:** `feat/rf-be-002-empresa`  
> **Funcionalidade:** F02 — Cadastro de Empresa e Parâmetros Básicos  
> **Serviço:** Núcleo (`meunegocio360-core`).  
> **Depende de:** [RF-BE-001](./RF-BE-001.md) (autenticação).  
> **Consumido por:** [RF-FE-002](../frontend/RF-FE-002.md) e por todos os requisitos de dados mestres e transacionais seguintes (RF-BE-003 em diante), que dependem da empresa ativa (RNF-096).

## História de usuário

Como **administrador**, quero **cadastrar os dados da minha empresa (razão social, CNPJ e parâmetros de exibição)**, para que **todos os demais módulos do sistema operem no contexto dessa empresa**.

## Atores e precondições

- **Ator principal:** `ADMIN`. Os demais perfis podem apenas **consultar** a empresa ativa (necessário para exibir o nome e os parâmetros na interface).
- **Precondição:** nenhuma além de estar autenticado. O cadastro da empresa é o **primeiro registro de dados do sistema**: ele não depende de cliente, fornecedor nem produto.

## Escopo

**Dentro do escopo (obrigatório):**

- Perfis de acesso `ADMIN`, `OPERADOR_COMERCIAL` e `OPERADOR_FINANCEIRO`, com migração do perfil `USUARIO` e ajuste do *seed* de desenvolvimento.
- Tabela e API da **empresa** (consulta, cadastro, edição, inativação e reativação).
- O mecanismo transversal que exige empresa ativa: `EmpresaAtiva` (resolução do `empresa_id` no servidor) e o erro `COMPANY_NOT_ACTIVE`, reutilizados por RF-BE-003, 004, 005 e requisitos futuros (RNF-096).
- Matriz de autorização por perfil (RNF-BE-47) e `empresa_id` como padrão das tabelas de negócio (RNF-BE-26).

**Fora do escopo:** múltiplas empresas ativas simultaneamente (*multiempresa*), CRUD de usuários e atribuição de perfis pela interface, logotipo, endereço fiscal completo, regime tributário e demais parâmetros fiscais.

## Dependências introduzidas

Nenhuma biblioteca nova. O requisito reutiliza Flyway, Spring Security, validação e springdoc já introduzidos por [RF-BE-001](./RF-BE-001.md) (RNF-015). Variável de ambiente nova: `SEED_FINANCEIRO_SENHA` (somente `dev`, já documentada no `.env.example`). `SEED_USUARIO_SENHA` passa a ser a senha do usuário `OPERADOR_COMERCIAL`.

## Decisões de modelo

- **Modelo de empresa única ativa:** o sistema atende **uma empresa por instalação**. Pode haver empresas inativas (histórico), mas **no máximo uma ativa**, garantido pelo banco (índice único sobre coluna gerada, ver "Modelo de dados"). A evolução para multiempresa é uma extensão opcional.
- **`empresa_id` resolvido no servidor:** os demais requisitos NUNCA recebem `empresa_id` do cliente (RNF-096). Eles obtêm a empresa ativa pelo componente `EmpresaAtiva`, que lança `COMPANY_NOT_ACTIVE` (`422`) quando não existe empresa ativa.

## Regras de negócio

| ID | Regra |
| --- | --- |
| RN-01 | O **CNPJ** é obrigatório, armazenado somente com dígitos, validado pelos dígitos verificadores (RNF-BE-27) e **único entre todas as empresas** cadastradas, ativas ou inativas. |
| RN-02 | Só pode existir **uma empresa ativa**. Cadastrar uma empresa quando já existe uma ativa retorna `409` `COMPANY_ALREADY_ACTIVE`. Reativar uma empresa com outra ativa também. |
| RN-03 | Somente `ADMIN` cadastra, edita, inativa e reativa a empresa. Qualquer usuário autenticado pode consultá-la. |
| RN-04 | A empresa é **inativada, nunca excluída**. Enquanto não há empresa ativa, nenhum cliente, fornecedor, produto, serviço ou venda pode ser criado (`422` `COMPANY_NOT_ACTIVE`); a consulta aos dados já existentes continua permitida. |
| RN-05 | Os **parâmetros de exibição** são `fusoHorario` (identificador IANA válido, padrão `America/Sao_Paulo`) e `moeda` (nesta fase somente `BRL`). Eles orientam a apresentação; a API continua trafegando datas em UTC e valores como decimal em *string* (RNF-033). |
| RN-06 | A atualização exige `versao` (RNF-BE-28). Campos de texto sofrem *trim*; `nomeFantasia`, `email` e `telefone` são opcionais, mas validados quando informados. |
| RN-07 | O perfil `USUARIO` deixa de existir: usuários com esse perfil passam a `OPERADOR_COMERCIAL` (migration). Os perfis válidos são `ADMIN`, `OPERADOR_COMERCIAL` e `OPERADOR_FINANCEIRO`, gravados na claim `perfil` do JWT. |
| RN-08 | O *seed* de desenvolvimento cria um usuário por perfil (`ADMIN`, `OPERADOR_COMERCIAL` e `OPERADOR_FINANCEIRO`) com senhas lidas do ambiente e **não cria empresa**, para que o fluxo de primeiro cadastro possa ser verificado. |

## Matriz de autorização

| Operação | `ADMIN` | `OPERADOR_COMERCIAL` | `OPERADOR_FINANCEIRO` |
| --- | :---: | :---: | :---: |
| Consultar empresa ativa | ✔ | ✔ | ✔ |
| Cadastrar, editar, inativar e reativar | ✔ | ✘ (`403`) | ✘ (`403`) |

## Contrato da API

Todos os caminhos sob `/api/v1`, autenticados (`Authorization: Bearer`).

### `GET /empresa`

Retorna a **empresa ativa**. `404` com `code` `COMPANY_NOT_REGISTERED` quando não existe empresa ativa (a interface usa isso para iniciar o primeiro cadastro).

```json
{
  "id": "0b1d6a64-6a0e-4c1c-a2f4-5f6a2d1f9a10",
  "razaoSocial": "Comércio Exemplo Ltda",
  "nomeFantasia": "Exemplo Informática",
  "cnpj": "11222333000181",
  "email": "contato@exemplo.com.br",
  "telefone": "11987654321",
  "fusoHorario": "America/Sao_Paulo",
  "moeda": "BRL",
  "ativa": true,
  "criadoEm": "2026-11-18T14:30:00Z",
  "atualizadoEm": "2026-11-18T14:30:00Z",
  "versao": 0
}
```

### `GET /empresa/{id}` (`ADMIN`) — pendente de implementação

Consulta uma empresa **ativa ou inativa** pelo `id` (UUID), respondendo o mesmo objeto de `GET /empresa` (com `ativa` e `versao`). `404` `COMPANY_NOT_FOUND` quando o `id` não existe.

> **Origem (proposta vinda do RF-FE-002, 19/nov/2026):** após a inativação, `GET /empresa` passa a responder `404 COMPANY_NOT_REGISTERED` e o `id` da empresa inativa deixa de ser alcançável pela interface — o botão **Reativar** do RF-FE-002 fica inerte sem um endpoint para consultar a inativa. Alternativa aceitável: listagem de empresas inativas.

### `POST /empresa` (`ADMIN`)

Corpo: `razaoSocial` (obrigatório, 3 a 150), `nomeFantasia` (opcional, até 150), `cnpj` (obrigatório, 14 dígitos), `email` (opcional, até 160), `telefone` (opcional, 10 ou 11 dígitos), `fusoHorario` e `moeda` (opcionais, com os padrões da RN-05). Resposta `201 Created` com `Location` e o objeto da empresa, já **ativa**.

### `PUT /empresa/{id}` (`ADMIN`)

Mesmos campos do cadastro, mais `versao`. O `cnpj` PODE ser alterado, respeitando a RN-01. Resposta `200` com a empresa atualizada.

### `PATCH /empresa/{id}/situacao` (`ADMIN`)

Corpo `{ "ativa": false, "versao": 3 }`. Inativa ou reativa a empresa (RN-02 e RN-04). Resposta `200` com a empresa.

### Erros (`application/problem+json`, RNF-BE-14 e RNF-BE-15)

| Situação | Status | `code` |
| --- | --- | --- |
| Campos inválidos (CNPJ com dígito verificador errado, razão social curta etc.) | `400` | `VALIDATION_ERROR` |
| Sem empresa ativa (consulta) | `404` | `COMPANY_NOT_REGISTERED` |
| Empresa não encontrada por `id` (`GET /empresa/{id}`) | `404` | `COMPANY_NOT_FOUND` |
| CNPJ já cadastrado | `409` | `COMPANY_CNPJ_DUPLICATE` |
| Já existe empresa ativa | `409` | `COMPANY_ALREADY_ACTIVE` |
| `versao` desatualizada | `409` | `VERSION_CONFLICT` |
| Perfil sem permissão | `403` | `ACCESS_DENIED` |
| Operação de negócio sem empresa ativa (demais RFs) | `422` | `COMPANY_NOT_ACTIVE` |

## Modelo de dados

Migrations novas, em sequência à `V2` (RNF-BE-20; os números abaixo são ilustrativos):

- **`V3__ajustar_perfis_usuario.sql`:** `UPDATE usuario SET perfil = 'OPERADOR_COMERCIAL' WHERE perfil = 'USUARIO'`. O *runner* de seed (`SeedUsuariosDev`) passa a criar também o usuário `OPERADOR_FINANCEIRO`, com `SEED_FINANCEIRO_SENHA`.
- **`V4__criar_tabela_empresa.sql`**, nomes ilustrativos:

| Coluna | Tipo | Restrições |
| --- | --- | --- |
| `id` | `BINARY(16)` | PK (`pk_empresa`) |
| `razao_social` | `VARCHAR(150)` | `NOT NULL` |
| `nome_fantasia` | `VARCHAR(150)` | `NULL` |
| `cnpj` | `CHAR(14)` | `NOT NULL`, único (`uk_empresa_cnpj`) |
| `email` | `VARCHAR(160)` | `NULL` |
| `telefone` | `VARCHAR(11)` | `NULL` |
| `fuso_horario` | `VARCHAR(50)` | `NOT NULL`, padrão `America/Sao_Paulo` |
| `moeda` | `CHAR(3)` | `NOT NULL`, padrão `BRL` |
| `ativa` | `BOOLEAN` | `NOT NULL`, padrão `TRUE` |
| `ativa_unica` | `TINYINT` | Coluna gerada: `CASE WHEN ativa THEN 1 END` (`STORED`), com índice **único** (`uk_empresa_ativa_unica`): o MySQL aceita vários `NULL`, logo só uma linha pode ter `ativa = TRUE` |
| `criado_em`, `atualizado_em`, `versao` | conforme RNF-BE-22 | `NOT NULL` |

A tabela `empresa` é a única que **não** tem `empresa_id`. Todas as tabelas de negócio criadas depois dela têm (RNF-BE-26).

## Critérios de aceite

```gherkin
Cenário: Primeiro acesso sem empresa cadastrada
  Dado que estou autenticado e não existe empresa ativa
  Quando envio GET /api/v1/empresa
  Então recebo 404 com code COMPANY_NOT_REGISTERED

Cenário: Administrador cadastra a empresa
  Dado que estou autenticado como ADMIN e não existe empresa ativa
  Quando envio POST /api/v1/empresa com razaoSocial "Comércio Exemplo Ltda" e cnpj "11222333000181"
  Então recebo 201 com Location e a empresa com ativa true, fusoHorario "America/Sao_Paulo" e moeda "BRL"
  E GET /api/v1/empresa passa a retornar essa empresa

Cenário: CNPJ inválido
  Dado que estou autenticado como ADMIN
  Quando envio POST /api/v1/empresa com cnpj "11222333000180"
  Então recebo 400 com code VALIDATION_ERROR e um erro no campo cnpj

Cenário: CNPJ duplicado
  Dado que existe uma empresa com cnpj "11222333000181"
  Quando um ADMIN tenta cadastrar outra empresa com o mesmo cnpj
  Então recebo 409 com code COMPANY_CNPJ_DUPLICATE

Cenário: Segunda empresa ativa
  Dado que existe uma empresa ativa
  Quando um ADMIN tenta cadastrar uma empresa com outro cnpj válido
  Então recebo 409 com code COMPANY_ALREADY_ACTIVE
  E nenhuma segunda empresa é criada

Cenário: Edição da empresa
  Dado que existe uma empresa ativa com versao 0
  Quando um ADMIN envia PUT /api/v1/empresa/{id} com novo nomeFantasia e versao 0
  Então recebo 200 com o novo nomeFantasia e versao 1

Cenário: Edição com versão desatualizada
  Dado que a empresa está na versao 1
  Quando um ADMIN envia PUT com versao 0
  Então recebo 409 com code VERSION_CONFLICT

Cenário: Operador não altera a empresa
  Dado que estou autenticado como OPERADOR_COMERCIAL
  Quando envio POST, PUT ou PATCH de empresa
  Então recebo 403 com code ACCESS_DENIED
  E GET /api/v1/empresa responde 200

Cenário: Inativação da empresa
  Dado que existe uma empresa ativa
  Quando um ADMIN envia PATCH /api/v1/empresa/{id}/situacao com ativa false
  Então recebo 200 com ativa false
  E GET /api/v1/empresa retorna 404 com code COMPANY_NOT_REGISTERED

Cenário: Reativação com outra empresa ativa
  Dado que existe uma empresa inativa e outra ativa
  Quando um ADMIN tenta reativar a inativa
  Então recebo 409 com code COMPANY_ALREADY_ACTIVE

Cenário: Perfis migrados
  Dado um banco com um usuário de perfil USUARIO
  Quando a migration V3 é aplicada
  Então esse usuário passa a ter o perfil OPERADOR_COMERCIAL
  E o login continua funcionando com a claim perfil "OPERADOR_COMERCIAL"

Cenário: Operações de negócio exigem empresa ativa
  Dado que não existe empresa ativa
  Quando tento criar um cliente, fornecedor ou item do catálogo
  Então recebo 422 com code COMPANY_NOT_ACTIVE
```

O último cenário depende dos cadastros de [RF-BE-003](./RF-BE-003.md), [RF-BE-004](./RF-BE-004.md) e [RF-BE-005](./RF-BE-005.md) e é verificado em cada um deles; aqui o mecanismo `EmpresaAtiva` DEVE existir e estar documentado no OpenAPI.

## Requisitos não funcionais com destaque neste requisito

RNF-096, RNF-BE-15, RNF-BE-20, RNF-BE-23, RNF-BE-26, RNF-BE-27, RNF-BE-28, RNF-BE-47, RNF-021, RNF-024.

## Testes previstos

Implementados na fase de testes (RNF-016). Até lá, os critérios de aceite são verificados manualmente na Swagger UI.

- **Unitários:** validador de CNPJ (válidos, inválidos, sequências repetidas), regras RN-02 e RN-04, `EmpresaAtiva`.
- **Web (`MockMvc`):** matriz de autorização por perfil e formato de erros.
- **Integração (Testcontainers):** unicidade de CNPJ e de empresa ativa (índice da coluna gerada), migration V3 sobre dados existentes.
- **Arquitetura:** nenhuma feature acessa o repositório da empresa diretamente; todas usam `EmpresaAtiva`.

## Pontos de atenção ao revisar código gerado por IA

- `empresa_id` recebido no corpo ou na URL de outros recursos, em vez de resolvido no servidor.
- Unicidade de "uma empresa ativa" garantida só por `if` na aplicação (condição de corrida): ela DEVE existir também no banco.
- Editar a migration `V1`/`V2` em vez de criar a `V3`; perfis como `String` solta em vez de um `enum`.
- Perfis validados só no frontend, ou `@PreAuthorize` ausente em algum método de escrita.
- `DELETE` físico da empresa, ou erro `500` genérico em vez de `COMPANY_CNPJ_DUPLICATE` quando o banco rejeita a duplicidade.
- Validador de CNPJ que aceita sequências repetidas (`00000000000000`) ou ignora os dígitos verificadores.
- Criar empresa de exemplo no *seed* (contraria a RN-08) ou senhas de seed escritas no código.
- Fuso horário aceito sem validar contra os identificadores IANA.

## Extensões opcionais (desafios)

1. Endereço e telefone estruturados da empresa, com consulta de CEP.
2. Evolução para **multiempresa**: troca de empresa ativa por usuário e `empresa_id` no token (exige ADR).
3. Trilha de auditoria das alterações da empresa (quem, quando, o que mudou).
