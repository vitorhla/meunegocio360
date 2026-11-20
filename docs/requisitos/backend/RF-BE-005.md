# RF-BE-005 — Catálogo de produtos e serviços

> **Branch:** `feat/rf-be-005-catalogo`  
> **Funcionalidade:** F05 — Catálogo de Produtos e Serviços  
> **Serviço:** Núcleo (`meunegocio360-core`).  
> **Depende de:** [RF-BE-002](./RF-BE-002.md) (empresa ativa e perfis).  
> **Consumido por:** [RF-FE-005](../frontend/RF-FE-005.md) e, futuramente, vendas, compras e controle de estoque (serviço Suprimentos).

## História de usuário

Como **administrador ou operador comercial**, quero **manter um catálogo de itens vendáveis, produtos (físicos) ou serviços, organizado por categorias**, para **vendê-los e, futuramente, controlar o estoque dos produtos**.

## Atores e precondições

- **Atores:** `ADMIN` e `OPERADOR_COMERCIAL`.
- **Precondições:** empresa ativa cadastrada ([RF-BE-002](./RF-BE-002.md)). Categoria é **opcional**: um item PODE ser criado sem categoria.

## Escopo

**Dentro do escopo (obrigatório):** tabelas e APIs de **categoria** (cadastro mínimo) e de **item de catálogo** (produto ou serviço), com listagem filtrada por tipo e por categoria, preço, inativação e exclusão condicionada. A coluna `estoque_minimo` **já existe** no modelo desta fase.

**Fora do escopo:** controle de estoque (saldos, movimentações, alertas de estoque mínimo), que é do serviço Suprimentos; **exposição do `estoque_minimo` na API ou na interface** (a coluna existe, mas nenhum DTO a lê ou grava nesta fase); imagens, variações (tamanho, cor), preço de custo, tabelas de preço, impostos, categorias hierárquicas e vínculos reais com vendas (criados pelos requisitos que as implementarem).

## Dependências introduzidas

Nenhuma biblioteca nova (RNF-015). Os valores monetários seguem RNF-033 (decimal em *string*, nunca `float`).

## Regras de negócio

| ID | Regra |
| --- | --- |
| RN-01 | Todo item e toda categoria pertencem à empresa ativa (`empresa_id`, RNF-096), resolvida no servidor. |
| RN-02 | Produtos e serviços **coexistem no mesmo catálogo**, distinguidos pelo campo `tipo` (`PRODUTO` ou `SERVICO`). O `tipo` é obrigatório e **não pode ser alterado** depois da criação (`422` `ITEM_TYPE_IMMUTABLE`), para preservar a consistência de vendas e estoque futuros. |
| RN-03 | O **preço** é obrigatório, decimal em *string* com até 2 casas, **não pode ser negativo** (`0` é permitido; valor negativo retorna `400` `VALIDATION_ERROR` no campo `preco`) e limitado a `9999999999.99`. |
| RN-04 | Item: `nome` obrigatório (2 a 150), `descricao` opcional (até 500), `unidade` (1 a 6 caracteres em maiúsculas; padrão `UN` para produto e `H` para serviço), `codigo` opcional (até 40; **único por empresa** quando informado, `409` `ITEM_CODE_DUPLICATE`) e `categoriaId` opcional. |
| RN-05 | A categoria do item DEVE existir na empresa ativa e estar **ativa** no momento da associação (`422` `CATEGORY_INVALID`). Inativar uma categoria NÃO altera os itens que a usam. |
| RN-06 | Categoria: `nome` obrigatório (2 a 80), **único por empresa** sem distinguir maiúsculas e acentos (`409` `CATEGORY_NAME_DUPLICATE`). Uma categoria **com itens vinculados não pode ser excluída** (`422` `CATEGORY_IN_USE`, com a quantidade de itens no `detail`); pode ser inativada. |
| RN-07 | O campo `estoque_minimo` existe no banco (`DECIMAL(15,3)`, `NOT NULL`, padrão `0`) e **não é exposto na API** nesta fase: nenhum DTO o recebe ou devolve. Ele será usado por Suprimentos. |
| RN-08 | Um item do tipo `SERVICO` **nunca** é retornado por consultas de produtos: o filtro `tipo=PRODUTO` o exclui. Telas e serviços de **controle de estoque** DEVEM consultar o catálogo com `tipo=PRODUTO`. |
| RN-09 | Um item com **vínculo** (venda, compra) NÃO PODE ser excluído, apenas **inativado** (RNF-097): `422` `ITEM_HAS_HISTORY`. A verificação usa o ponto de extensão `VerificadorVinculoItem`, nos moldes de [RF-BE-003 (RN-06)](./RF-BE-003.md#regras-de-negócio): sem implementações nesta fase. |
| RN-10 | A busca `q` procura por nome ou código (contém, sem distinguir maiúsculas e acentos). Item inativo não aparece por padrão (`situacao=ATIVO`). A atualização exige `versao` (RNF-BE-28). |

## Matriz de autorização

| Operação | `ADMIN` | `OPERADOR_COMERCIAL` | `OPERADOR_FINANCEIRO` |
| --- | :---: | :---: | :---: |
| Categorias e itens: listar, consultar, cadastrar, editar, inativar/reativar e excluir | ✔ | ✔ | ✘ (`403`) |

## Contrato da API

Caminhos sob `/api/v1`, autenticados.

### Itens de catálogo

| Método e caminho | Descrição |
| --- | --- |
| `GET /itens-catalogo` | Listagem paginada. Parâmetros: `q` (nome ou código), `tipo` (`PRODUTO` ou `SERVICO`), `categoriaId`, `situacao` (`ATIVO`, `INATIVO`, `TODOS`; padrão `ATIVO`), `page`, `size`, `sort` (`nome`, `preco`, `criadoEm`; padrão `nome`). Filtros combinam entre si (E). |
| `POST /itens-catalogo` | Cadastro. `201` com `Location`. |
| `GET /itens-catalogo/{id}` | Consulta. `404` `ITEM_NOT_FOUND`. |
| `PUT /itens-catalogo/{id}` | Edição completa, com `versao`; o `tipo` enviado DEVE ser igual ao atual (RN-02). |
| `PATCH /itens-catalogo/{id}/situacao` | Corpo `{ "ativo": false, "versao": 2 }`. |
| `DELETE /itens-catalogo/{id}` | `204` sem vínculos; `422` `ITEM_HAS_HISTORY` com vínculos. |

Objeto de item (sem `estoque_minimo`, RN-07):

```json
{
  "id": "9d3e7c10-5c2a-4a8e-8d1b-3a9f6e2b7c44",
  "tipo": "PRODUTO",
  "codigo": "NOTE-I5-16",
  "nome": "Notebook 15\" i5 16 GB",
  "descricao": "Notebook para uso profissional",
  "unidade": "UN",
  "preco": "3499.90",
  "categoria": { "id": "2f0a8c9e-1b3d-4e5f-a6b7-c8d9e0f1a2b3", "nome": "Informática" },
  "ativo": true,
  "criadoEm": "2026-11-18T14:30:00Z",
  "atualizadoEm": "2026-11-18T14:30:00Z",
  "versao": 0
}
```

Entrada (`POST`/`PUT`): `tipo`, `codigo`, `nome`, `descricao`, `unidade`, `preco` e `categoriaId` (e `versao` no `PUT`).

### Categorias

| Método e caminho | Descrição |
| --- | --- |
| `GET /categorias` | Listagem paginada (`q`, `situacao`, `page`, `size`, `sort=nome`). |
| `POST /categorias` | Corpo `{ "nome": "Informática" }`. `201`. |
| `PUT /categorias/{id}` | Renomeia, com `versao`. |
| `PATCH /categorias/{id}/situacao` | Inativa ou reativa, com `versao`. |
| `DELETE /categorias/{id}` | `204` ou `422` `CATEGORY_IN_USE` (RN-06). |

### Erros (`application/problem+json`)

| Situação | Status | `code` |
| --- | --- | --- |
| Campo inválido (preço negativo, nome curto, tipo desconhecido) | `400` | `VALIDATION_ERROR` |
| Sem empresa ativa | `422` | `COMPANY_NOT_ACTIVE` |
| Código do item já usado na empresa | `409` | `ITEM_CODE_DUPLICATE` |
| Nome de categoria já usado na empresa | `409` | `CATEGORY_NAME_DUPLICATE` |
| Tentativa de alterar o `tipo` | `422` | `ITEM_TYPE_IMMUTABLE` |
| Categoria inexistente ou inativa na associação | `422` | `CATEGORY_INVALID` |
| Categoria com itens não pode ser excluída | `422` | `CATEGORY_IN_USE` |
| Item com histórico não pode ser excluído | `422` | `ITEM_HAS_HISTORY` |
| Item ou categoria inexistente | `404` | `ITEM_NOT_FOUND`, `CATEGORY_NOT_FOUND` |
| `versao` desatualizada | `409` | `VERSION_CONFLICT` |
| Perfil sem permissão | `403` | `ACCESS_DENIED` |

## Modelo de dados

Migration `V7__criar_tabelas_categoria_e_item_catalogo.sql` (nomes ilustrativos):

**`categoria`:** `id` (PK), `empresa_id` (FK `NOT NULL`), `nome` `VARCHAR(80)` (*collation* `utf8mb4_0900_ai_ci`), `ativo` `BOOLEAN` e as colunas padrão (RNF-BE-22). `UNIQUE (empresa_id, nome)`.

**`item_catalogo`:**

| Coluna | Tipo | Restrições |
| --- | --- | --- |
| `id` | `BINARY(16)` | PK |
| `empresa_id` | `BINARY(16)` | `NOT NULL`, FK para `empresa` |
| `categoria_id` | `BINARY(16)` | `NULL`, FK para `categoria` (`fk_item_catalogo_categoria`), indexada |
| `tipo` | `VARCHAR(10)` | `NOT NULL` (`PRODUTO`, `SERVICO`) |
| `codigo` | `VARCHAR(40)` | `NULL`; `UNIQUE (empresa_id, codigo)` (o MySQL aceita vários `NULL`) |
| `nome` | `VARCHAR(150)` | `NOT NULL`, *collation* `utf8mb4_0900_ai_ci` |
| `descricao` | `VARCHAR(500)` | `NULL` |
| `unidade` | `VARCHAR(6)` | `NOT NULL` |
| `preco` | `DECIMAL(12,2)` | `NOT NULL`, `CHECK (preco >= 0)` |
| `estoque_minimo` | `DECIMAL(15,3)` | `NOT NULL`, padrão `0`, `CHECK (estoque_minimo >= 0)`; sem uso funcional nesta fase |
| `ativo` | `BOOLEAN` | `NOT NULL`, padrão `TRUE` |
| `criado_em`, `atualizado_em`, `versao` | conforme RNF-BE-22 | `NOT NULL` |

Índices: `(empresa_id, tipo, ativo)` e `(empresa_id, nome)`.

## Critérios de aceite

```gherkin
Cenário: Cadastro de produto
  Dado que estou autenticado como OPERADOR_COMERCIAL e existe empresa ativa
  Quando envio POST /api/v1/itens-catalogo com tipo PRODUTO, nome "Mouse sem fio" e preco "89.90"
  Então recebo 201 com Location, unidade "UN", ativo true e preco "89.90"
  E a resposta não contém o campo estoqueMinimo

Cenário: Cadastro de serviço
  Quando envio POST com tipo SERVICO, nome "Formatação de notebook" e preco "150.00"
  Então recebo 201 com unidade "H"

Cenário: Preço negativo
  Quando envio um item com preco "-1.00"
  Então recebo 400 com code VALIDATION_ERROR e um erro no campo preco

Cenário: Preço zero e preço inválido
  Quando envio preco "0.00"
  Então recebo 201
  E preco "10,5" ou "abc" retorna 400 com code VALIDATION_ERROR

Cenário: Item sem categoria
  Quando cadastro um item sem categoriaId
  Então recebo 201 e categoria é nula

Cenário: Item com categoria inativa
  Dado uma categoria inativa
  Quando tento cadastrar um item com essa categoriaId
  Então recebo 422 com code CATEGORY_INVALID

Cenário: Filtro por tipo
  Dado que existem produtos e serviços
  Quando envio GET /api/v1/itens-catalogo?tipo=PRODUTO
  Então todos os itens retornados têm tipo PRODUTO
  E nenhum serviço é retornado
  E GET ?tipo=SERVICO retorna somente serviços

Cenário: Filtro por categoria e por tipo combinados
  Quando envio GET /api/v1/itens-catalogo?tipo=PRODUTO&categoriaId={id}
  Então recebo somente produtos dessa categoria

Cenário: Busca por nome ou código
  Dado o item "Mouse sem fio" com codigo "MSE-01"
  Quando envio GET /api/v1/itens-catalogo?q=mouse ou q=mse
  Então o item está na resposta

Cenário: Código duplicado
  Dado um item com codigo "MSE-01"
  Quando cadastro outro com o mesmo codigo
  Então recebo 409 com code ITEM_CODE_DUPLICATE

Cenário: Tipo não pode ser alterado
  Dado um item PRODUTO
  Quando envio PUT com tipo SERVICO
  Então recebo 422 com code ITEM_TYPE_IMMUTABLE

Cenário: Edição com controle de versão
  Dado um item com versao 0
  Quando envio PUT com novo preco e versao 0, e depois novamente com versao 0
  Então o primeiro retorna 200 com versao 1 e o segundo 409 com code VERSION_CONFLICT

Cenário: Categoria duplicada
  Dado a categoria "Informática"
  Quando cadastro "informatica"
  Então recebo 409 com code CATEGORY_NAME_DUPLICATE

Cenário: Categoria em uso não pode ser excluída
  Dado uma categoria com itens
  Quando envio DELETE /api/v1/categorias/{id}
  Então recebo 422 com code CATEGORY_IN_USE
  E ao inativar a categoria (PATCH) recebo 200 e os itens permanecem inalterados

Cenário: Exclusão de item sem vínculos
  Quando envio DELETE /api/v1/itens-catalogo/{id} de um item sem vendas nem compras
  Então recebo 204

Cenário: Exclusão de item com histórico
  Dado um item com venda ou compra vinculada
  Quando envio DELETE /api/v1/itens-catalogo/{id}
  Então recebo 422 com code ITEM_HAS_HISTORY e detail claro em português
  # Verificável de ponta a ponta quando existir o primeiro vínculo (vendas). Até lá, verificado
  # com uma implementação simulada de VerificadorVinculoItem.

Cenário: Sem empresa ativa
  Dado que não existe empresa ativa
  Quando tento cadastrar um item ou uma categoria
  Então recebo 422 com code COMPANY_NOT_ACTIVE

Cenário: Perfil financeiro não acessa o catálogo
  Dado que estou autenticado como OPERADOR_FINANCEIRO
  Quando envio GET /api/v1/itens-catalogo
  Então recebo 403 com code ACCESS_DENIED

Cenário: Coluna estoque_minimo existe no banco
  Dado o banco migrado
  Então a tabela item_catalogo possui a coluna estoque_minimo NOT NULL com padrão 0
  E nenhuma resposta da API a expõe
```

## Requisitos não funcionais com destaque neste requisito

RNF-096, RNF-097, RNF-033, RNF-054, RNF-BE-15, RNF-BE-16, RNF-BE-23, RNF-BE-26, RNF-BE-28, RNF-BE-47.

## Testes previstos

Implementados na fase de testes (RNF-016). Até lá, os critérios de aceite são verificados manualmente na Swagger UI.

- **Unitários:** regras RN-02 a RN-09 (imutabilidade do tipo, categoria ativa, preço), conversão decimal sem perda, `VerificadorVinculoItem` simulado.
- **Web (`MockMvc`):** matriz de perfis, formato dos erros, serialização de `preco` como *string*.
- **Integração (Testcontainers):** `CHECK` de preço, unicidade de `codigo` e de nome de categoria, filtros combinados e garantia de que `tipo=PRODUTO` nunca retorna serviços.

## Pontos de atenção ao revisar código gerado por IA

- `preco` como `double`/`float` ou `BigDecimal` serializado como número JSON; arredondamento implícito.
- Preço negativo bloqueado só no frontend, ou sem `CHECK` no banco.
- `estoque_minimo` exposto no DTO "porque está na entidade", ou removido do banco "porque não é usado" (contraria RN-07).
- Duas tabelas separadas (`produto` e `servico`) ou herança JPA complexa: o requisito pede **um catálogo** com o campo `tipo`.
- Filtro por tipo feito no frontend, depois de trazer tudo; ou `tipo=PRODUTO` implementado como "tipo diferente de SERVICO" (quebra quando surgirem novos tipos).
- Exclusão de categoria em cascata apagando itens; erro `500` em vez de `CATEGORY_IN_USE`.
- `categoriaId` aceito de outra empresa (sem checar `empresa_id`); `sort` livre.
- `Enum` do `tipo` aceitando minúsculas ou valor desconhecido como `null` silencioso.

## Extensões opcionais (desafios)

1. Categorias hierárquicas (subcategorias) e ordenação manual.
2. Imagem do produto, com limites de tipo e tamanho.
3. Preço de custo e margem, mantendo o preço de venda como fonte da verdade para vendas.
