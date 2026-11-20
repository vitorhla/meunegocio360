# RF-FE-005 — Catálogo de produtos e serviços

> **Branch:** `feat/rf-fe-005-catalogo`  
> **Funcionalidade:** F05 — Catálogo de Produtos e Serviços  
> **Dependências:** [RF-FE-003](./RF-FE-003.md) (componentes compartilhados de listagem), [RF-FE-002](./RF-FE-002.md) (layout e guardas) e [RF-BE-005](../backend/RF-BE-005.md) (contrato de `/api/v1/itens-catalogo` e `/api/v1/categorias`).  
> **Consumido por:** telas futuras de vendas, compras e controle de estoque, por meio do seletor `ItemCatalogoSelect`.

## História de usuário

Como **administrador ou operador comercial**, quero **manter o catálogo de produtos e serviços, filtrá-lo por tipo e por categoria e organizar as categorias**, para **encontrar rapidamente o que vendo e manter os preços em dia**.

## Escopo

**Dentro do escopo (obrigatório):**

- Lista do catálogo com busca, filtros por **tipo** e por **categoria**, situação e paginação; formulário de produto ou serviço; inativação, reativação e exclusão com confirmação.
- Gestão **mínima de categorias**: listar, criar, renomear, inativar/reativar e excluir.
- Campo de preço com máscara de moeda, enviado como decimal em *string* (RNF-FE-28).
- Seletor reutilizável `ItemCatalogoSelect`, com filtro opcional por `tipo`, exposto por `features/catalogo/index.js`.
- Item **Catálogo** no menu para `ADMIN` e `OPERADOR_COMERCIAL`.

**Fora do escopo:** estoque (saldos, movimentações), **qualquer exibição ou edição do estoque mínimo** (o campo existe só no banco, [RF-BE-005](../backend/RF-BE-005.md)), imagens, variações, preço de custo e telas de venda.

## Dependências introduzidas

Nenhuma biblioteca nova (RNF-015). Reutiliza os componentes compartilhados de [RF-FE-002](./RF-FE-002.md) e [RF-FE-003](./RF-FE-003.md) e o *combobox* do seletor de clientes. A formatação de moeda é uma função própria em `shared/lib` (BRL, vírgula decimal).

## Telas e rotas

Protegidas por `RequireAuth`, `RequireEmpresa` e `RequireRole` (`ADMIN`, `OPERADOR_COMERCIAL`).

| Rota | Descrição |
| --- | --- |
| `/catalogo` | Lista do catálogo: busca ("Buscar por nome ou código"), filtro **Tipo** (Todos, Produtos, Serviços; padrão Todos), filtro **Categoria** (lista de categorias ativas e a opção "Sem filtro"), filtro **Situação** (padrão Ativos), tabela (código, nome, selo de tipo, categoria, unidade, preço em R$, situação, ações), paginação e **Novo item**. Filtros e página ficam na URL (`?q=&tipo=&categoriaId=&situacao=&page=`). |
| `/catalogo/novo` | Formulário de cadastro. |
| `/catalogo/:id` | Formulário de edição; `ITEM_NOT_FOUND` exibe "Item não encontrado". |
| `/catalogo/categorias` | Gestão de categorias (lista com busca e situação; **Nova categoria** em modal; renomear, inativar/reativar e excluir). Há um atalho para ela na tela do catálogo. |

Estados da lista (RNF-062): carregamento, **vazio sem filtro** ("O catálogo está vazio. Cadastre o primeiro produto ou serviço."), **vazio com filtro** ("Nenhum item encontrado para os filtros aplicados.", com **Limpar filtros**), erro com **Tentar novamente** e sucesso. O selo de tipo usa texto ("Produto", "Serviço") e não só cor.

## Formulário do item

| Campo | Controle | Regras (Zod, espelhando o backend) | Mensagens |
| --- | --- | --- | --- |
| Tipo | Grupo de opções (`radio`): Produto / Serviço. **Desabilitado na edição** (o tipo não muda depois de criado) | Obrigatório | "Selecione o tipo." |
| Código | `input type="text"` | Opcional; até 40 caracteres | — |
| Nome | `input type="text"` | Obrigatório; *trim*; 2 a 150 | "Informe o nome." |
| Descrição | `textarea` | Opcional; até 500, com contador | — |
| Categoria | `select` com categorias ativas e "Sem categoria" | Opcional | — |
| Unidade | `input type="text"` (maiúsculas) | Obrigatório; 1 a 6 caracteres; padrão `UN` para produto e `H` para serviço (ao trocar o tipo na criação, o padrão acompanha, se o usuário não alterou) | "Informe a unidade." |
| Preço (R$) | `input` com `inputMode="decimal"` e máscara de moeda pt-BR | Obrigatório; **maior ou igual a zero**; até 2 casas; máximo `9.999.999.999,99` | "Informe o preço." / "O preço não pode ser negativo." |

O preço digitado como "1.234,50" é enviado como `"1234.50"` (RNF-FE-28, RNF-033). **Não há campo de estoque mínimo.** O `versao` vai oculto no `PUT`.

## Seletor `ItemCatalogoSelect`

Mesmo padrão ARIA e de teclado de [`ClienteSelect`](./RF-FE-003.md#seletor-clienteselect), consumindo `GET /itens-catalogo?q=&situacao=ATIVO&size=10`, com a propriedade opcional `tipo` (`PRODUTO` ou `SERVICO`), repassada à API. **Telas de controle de estoque DEVEM usar `tipo="PRODUTO"`**, de modo que um serviço nunca seja oferecido nelas. As sugestões mostram nome, código, tipo e preço. Verificado na rota de apoio `/dev/item-select`, registrada somente quando `import.meta.env.DEV` for verdadeiro.

## Comportamentos e mensagens

| Situação (resposta da API) | Comportamento na interface |
| --- | --- |
| `POST` `201` / `PUT` `200` | "Item cadastrado com sucesso." / "Item atualizado com sucesso." e retorno à lista. |
| `400` `VALIDATION_ERROR` (ex.: preço negativo) | Erro ligado ao campo (`errors[]`); foco no primeiro inválido. |
| `409` `ITEM_CODE_DUPLICATE` | Erro no campo código: "Já existe um item com este código." |
| `409` `VERSION_CONFLICT` | Alerta com **Recarregar dados**. |
| `422` `CATEGORY_INVALID` | Erro no campo categoria: "Selecione uma categoria ativa." e recarga da lista de categorias. |
| `422` `ITEM_TYPE_IMMUTABLE` | Alerta: "O tipo do item não pode ser alterado depois de criado." |
| `422` `COMPANY_NOT_ACTIVE` | Alerta para cadastrar ou ativar a empresa, com link para `/empresa`. |
| Inativar / reativar | Com confirmação para inativar; "Item inativado." / "Item reativado." |
| Excluir item `204` | "Item excluído." |
| Excluir item `422` `ITEM_HAS_HISTORY` | O modal permanece aberto com: "Este item possui vendas ou compras vinculadas e não pode ser excluído. Você pode inativá-lo." e o botão **Inativar item**. |
| Categoria: `409` `CATEGORY_NAME_DUPLICATE` | Erro no campo nome: "Já existe uma categoria com este nome." |
| Excluir categoria `422` `CATEGORY_IN_USE` | "Esta categoria possui itens vinculados e não pode ser excluída. Você pode inativá-la." e o botão **Inativar categoria**. |
| `403`, falha de rede, `5xx` | Como em [RF-FE-003](./RF-FE-003.md#comportamentos-e-mensagens). |

## Critérios de aceite

```gherkin
Cenário: Menu e acesso por perfil
  Dado um ADMIN ou OPERADOR_COMERCIAL
  Então o menu exibe "Catálogo"
  E um OPERADOR_FINANCEIRO não vê o item e, ao abrir "/catalogo" pela URL, vê "Acesso negado"

Cenário: Catálogo vazio
  Dado que não há itens
  Quando abro "/catalogo"
  Então vejo "O catálogo está vazio. Cadastre o primeiro produto ou serviço."

Cenário: Cadastro de produto
  Quando seleciono "Produto", informo nome "Mouse sem fio" e preço "89,90" e salvo
  Então o preço é enviado como "89.90" e a unidade padrão é "UN"
  E vejo "Item cadastrado com sucesso." e o item na lista com o selo "Produto"

Cenário: Cadastro de serviço
  Quando seleciono "Serviço", informo nome "Formatação de notebook" e preço "150,00"
  Então a unidade sugerida é "H" e, ao salvar, o item aparece com o selo "Serviço"

Cenário: Preço negativo
  Quando informo preço "-10,00"
  Então vejo "O preço não pode ser negativo." e nenhuma requisição é enviada

Cenário: Preço zero e formatação
  Quando informo preço "0"
  Então o envio é permitido e a lista exibe "R$ 0,00"
  E valores como "1.234,5" são normalizados para "R$ 1.234,50"

Cenário: Filtro por tipo
  Dado produtos e serviços no catálogo
  Quando escolho o filtro "Produtos"
  Então a lista mostra somente produtos e a URL contém "tipo=PRODUTO"
  E ao escolher "Serviços" mostra somente serviços

Cenário: Filtro por categoria e combinação de filtros
  Quando escolho a categoria "Informática" e o tipo "Produtos"
  Então a lista mostra somente produtos dessa categoria e a URL reflete os dois filtros
  E ao recarregar a página os filtros se mantêm

Cenário: Busca por nome ou código
  Quando digito "mse" ou "mouse" na busca
  Então a lista mostra o item "Mouse sem fio" de código "MSE-01"

Cenário: Item sem categoria
  Quando salvo um item com "Sem categoria"
  Então ele aparece na lista com a categoria em branco ("—")

Cenário: Tipo não editável
  Quando abro um item existente
  Então o campo Tipo está desabilitado

Cenário: Código duplicado
  Dado que a API responde 409 com code ITEM_CODE_DUPLICATE
  Quando salvo
  Então vejo "Já existe um item com este código." no campo código

Cenário: Estoque mínimo não aparece
  Então nem o formulário, nem a lista, nem o seletor exibem ou enviam estoque mínimo

Cenário: Serviço não aparece em seleções de estoque
  Dado o ItemCatalogoSelect com tipo="PRODUTO" na rota "/dev/item-select"
  Quando digito o nome de um serviço
  Então nenhum serviço é sugerido
  E sem o filtro de tipo, serviços e produtos são sugeridos

Cenário: Gestão de categorias
  Quando crio a categoria "Informática" em "/catalogo/categorias"
  Então ela aparece na lista e fica disponível no filtro e no formulário de itens
  E criar "informatica" exibe "Já existe uma categoria com este nome."

Cenário: Categoria em uso
  Dado que a API responde 422 com code CATEGORY_IN_USE
  Quando confirmo a exclusão da categoria
  Então vejo a mensagem de negócio clara e o botão "Inativar categoria", sem erro genérico

Cenário: Inativação de item e exclusão com histórico
  Quando inativo um item e confirmo
  Então ele sai da lista de Ativos e aparece em Inativos
  E, se a API responder 422 com code ITEM_HAS_HISTORY ao excluir, vejo a mensagem clara com o botão "Inativar item"
  # A exclusão com histórico é verificável de ponta a ponta quando existir o primeiro vínculo (vendas);
  # até lá, com a resposta da API simulada nos testes.

Cenário: Acessibilidade e responsividade
  Então todos os campos têm rótulo, os erros são anunciados e o foco vai ao primeiro campo inválido
  E os filtros, a tabela e os modais funcionam por teclado e em 360 px sem rolagem horizontal da página
```

## Requisitos não funcionais com destaque neste requisito

RNF-FE-01, RNF-FE-16, RNF-FE-20 a RNF-FE-23, RNF-FE-25, RNF-FE-27, RNF-FE-28, RNF-FE-29, RNF-033, RNF-060, RNF-062, RNF-097.

## Testes previstos

Implementados na fase de testes (RNF-016). Até lá, os critérios de aceite são verificados manualmente no navegador, contra o backend local.

- **Unitários:** conversão de moeda pt-BR para decimal em *string* e o inverso (sem `float`), *schema* Zod do item, `useUrlFilters` com os filtros do catálogo.
- **Componente/integração (Vitest + RTL + MSW):** lista (todos os estados, filtros combinados e URL), formulário (preço, tipo desabilitado na edição, erros `409`/`422`), categorias (incluindo `CATEGORY_IN_USE`), `ITEM_HAS_HISTORY` simulado e `ItemCatalogoSelect` com e sem `tipo`.
- **Acessibilidade:** `vitest-axe` na lista, no formulário, na gestão de categorias e no seletor.

## Pontos de atenção ao revisar código gerado por IA

- Preço manipulado como `Number`/`float` (erros de arredondamento) ou enviado com vírgula e símbolo de moeda; máscara que impede digitar o zero ou aceita negativo.
- Filtro por tipo e por categoria aplicado no navegador sobre uma lista completa; filtros fora da URL.
- Campo de estoque mínimo "para já deixar pronto" no formulário ou na lista (contraria o escopo).
- Duas features separadas para produtos e serviços, em vez de um catálogo com o campo `tipo`.
- Seletor de itens que não repassa `tipo` à API, deixando serviços aparecerem em telas de estoque.
- Tipo editável na edição; troca de tipo sem aviso.
- Mensagens genéricas no lugar dos erros de negócio `CATEGORY_IN_USE` e `ITEM_HAS_HISTORY`; decisão pelo texto, não pelo `code`.
- Selo de tipo comunicado somente por cor (RNF-060).
- Categoria carregada uma vez e nunca atualizada depois de criar, renomear ou inativar categorias (cache do TanStack Query sem invalidação).

## Extensões opcionais (desafios)

1. Ordenação por preço e por nome, preservada na URL.
2. Edição rápida de preço direto na lista, com confirmação.
3. Importação do catálogo por CSV, com relatório das linhas rejeitadas.
