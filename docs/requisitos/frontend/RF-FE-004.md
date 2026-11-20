# RF-FE-004 — Cadastro e busca de fornecedores

> **Branch:** `feat/rf-fe-004-fornecedores`  
> **Funcionalidade:** F04 — Cadastro de Fornecedores  
> **Dependências:** [RF-FE-003](./RF-FE-003.md) (componentes de listagem, `DocumentoInput`, `useUrlFilters`, padrão de tela), [RF-FE-002](./RF-FE-002.md) (layout e guardas) e [RF-BE-004](../backend/RF-BE-004.md) (contrato de `/api/v1/fornecedores`).  
> **Consumido por:** telas futuras de compras e de contas a pagar, por meio do seletor `FornecedorSelect`.

## História de usuário

Como **administrador ou operador financeiro**, quero **buscar, cadastrar, editar, inativar e excluir fornecedores**, e **encontrá-los por nome ou documento em qualquer tela que os referencie**, para **usá-los com segurança em compras e contas a pagar**.

## Escopo

**Dentro do escopo (obrigatório):** lista com busca, filtro de situação e paginação; formulário de cadastro e de edição; inativação, reativação e exclusão com confirmação; seletor reutilizável `FornecedorSelect`; item **Fornecedores** no menu para `ADMIN` e `OPERADOR_FINANCEIRO`.

**Fora do escopo:** dados bancários, condições de pagamento, múltiplos contatos, importação e exportação, telas de compras e de contas a pagar.

## Dependências introduzidas

Nenhuma biblioteca nova (RNF-015). Esta tela **reutiliza** `ListToolbar`, `Paginacao`, `StatusBadge`, `DocumentoInput`, `ConfirmModal`, `ErrorAlert` e `useUrlFilters`, sem copiá-los. Se faltar uma variação, ela é acrescentada ao componente compartilhado, e não duplicada na feature.

## Telas e rotas

Protegidas por `RequireAuth`, `RequireEmpresa` e `RequireRole` (`ADMIN`, `OPERADOR_FINANCEIRO`). A estrutura e os estados são os de [RF-FE-003](./RF-FE-003.md#telas-e-rotas), trocando o recurso:

| Rota | Descrição |
| --- | --- |
| `/fornecedores` | Lista com busca ("Buscar por nome ou documento"), filtro **Situação** (padrão Ativos), tabela, paginação e **Novo fornecedor**; filtros e página na URL. Vazio sem filtro: "Nenhum fornecedor cadastrado. Cadastre o primeiro fornecedor."; vazio com filtro: "Nenhum fornecedor encontrado para a busca." |
| `/fornecedores/novo` | Formulário de cadastro. |
| `/fornecedores/:id` | Formulário de edição; `SUPPLIER_NOT_FOUND` exibe "Fornecedor não encontrado". |

## Formulário

Mesmos campos, controles e regras de [RF-FE-003](./RF-FE-003.md#formulário) (tipo de pessoa, nome ou razão social, CPF/CNPJ com máscara, e-mail e telefone), com os rótulos "Fornecedor" onde couber. Documento e telefone vão à API somente com dígitos (RNF-FE-28).

## Seletor `FornecedorSelect`

Mesmo comportamento e acessibilidade de [`ClienteSelect`](./RF-FE-003.md#seletor-clienteselect), consumindo `GET /fornecedores?q=&situacao=ATIVO&size=10`, exposto por `features/fornecedores/index.js` e baseado no mesmo componente genérico de *combobox* (extraído em `shared/ui` se ainda não existir, sem duplicar a implementação). Verificado na rota de apoio `/dev/fornecedor-select`, registrada somente quando `import.meta.env.DEV` for verdadeiro.

## Comportamentos e mensagens

Iguais aos de [RF-FE-003](./RF-FE-003.md#comportamentos-e-mensagens), com os códigos e textos de fornecedor:

| Situação (resposta da API) | Comportamento na interface |
| --- | --- |
| `POST` `201` / `PUT` `200` | "Fornecedor cadastrado com sucesso." / "Fornecedor atualizado com sucesso." e retorno à lista. |
| `409` `SUPPLIER_DOCUMENT_DUPLICATE` | Erro no campo documento: "Já existe um fornecedor com este documento. Ele pode estar inativo; busque por ele e reative." |
| `409` `VERSION_CONFLICT` | Alerta com **Recarregar dados**. |
| `422` `COMPANY_NOT_ACTIVE` | Alerta para cadastrar ou ativar a empresa, com link para `/empresa`. |
| Excluir `204` | "Fornecedor excluído." e atualização da lista. |
| Excluir `422` `SUPPLIER_HAS_HISTORY` | O modal permanece aberto com: "Este fornecedor possui pedidos de compra ou contas a pagar vinculados e não pode ser excluído. Você pode inativá-lo." e o botão **Inativar fornecedor**. Nunca uma mensagem genérica. |
| Inativar / reativar | Com confirmação para inativar; "Fornecedor inativado." / "Fornecedor reativado." |
| `403`, `400`, falha de rede | Como em [RF-FE-003](./RF-FE-003.md#comportamentos-e-mensagens). |

## Critérios de aceite

```gherkin
Cenário: Menu e acesso por perfil
  Dado um OPERADOR_FINANCEIRO ou ADMIN
  Então o menu exibe "Fornecedores"
  E um OPERADOR_COMERCIAL não vê o item e, ao abrir "/fornecedores" pela URL, vê "Acesso negado"

Cenário: Lista vazia
  Dado que não há fornecedores
  Quando abro "/fornecedores"
  Então vejo "Nenhum fornecedor cadastrado. Cadastre o primeiro fornecedor."

Cenário: Cadastro de fornecedor
  Quando preencho "Pessoa jurídica", nome "Distribuidora Alfa Ltda" e CNPJ "11.222.333/0001-81" e salvo
  Então o CNPJ é enviado como "11222333000181"
  E vejo "Fornecedor cadastrado com sucesso." e o fornecedor na lista

Cenário: Documento inválido
  Quando informo um CPF ou CNPJ com dígito verificador errado
  Então vejo "CPF inválido." ou "CNPJ inválido." e nenhuma requisição é enviada

Cenário: Documento duplicado
  Dado que a API responde 409 com code SUPPLIER_DOCUMENT_DUPLICATE
  Quando salvo
  Então vejo o erro no campo documento com a orientação de buscar o fornecedor inativo

Cenário: Busca por nome ou documento
  Dado o fornecedor "Distribuidora Álfa Ltda"
  Quando digito "distribuidora alfa" ou "11.222" na busca
  Então a lista mostra o fornecedor e a URL reflete a busca, que se mantém ao recarregar

Cenário: Filtro de situação, busca sem resultado e paginação
  Então se comportam como na lista de clientes, com os textos de fornecedor

Cenário: Edição, inativação e reativação
  Quando edito o telefone e salvo
  Então vejo "Fornecedor atualizado com sucesso."
  E ao inativar (com confirmação) o fornecedor sai da lista de Ativos e aparece em Inativos

Cenário: Exclusão de fornecedor sem histórico
  Quando excluo um fornecedor sem vínculos e confirmo
  Então vejo "Fornecedor excluído."

Cenário: Exclusão de fornecedor com histórico
  Dado que a API responde 422 com code SUPPLIER_HAS_HISTORY
  Quando confirmo a exclusão
  Então vejo no modal a mensagem de negócio clara sugerindo inativar, e não um erro genérico
  E o botão "Inativar fornecedor" inativa o fornecedor a partir do modal
  # Verificável de ponta a ponta quando existir o primeiro vínculo (compras ou contas a pagar);
  # até lá, com a resposta da API simulada nos testes.

Cenário: Busca em qualquer tela que referencie o fornecedor
  Dado o FornecedorSelect na rota "/dev/fornecedor-select"
  Quando digito parte do nome ou do documento
  Então vejo as sugestões de fornecedores ativos, selecionáveis por mouse e teclado

Cenário: Reutilização dos componentes
  Então a lista e o formulário usam os componentes compartilhados de RF-FE-003 e não há cópias deles na feature

Cenário: Acessibilidade e responsividade
  Então os critérios de acessibilidade, teclado e 360 px de RF-FE-003 também se aplicam a estas telas
```

## Requisitos não funcionais com destaque neste requisito

RNF-FE-01, RNF-FE-16, RNF-FE-20 a RNF-FE-23, RNF-FE-25, RNF-FE-27, RNF-FE-28, RNF-FE-29, RNF-060, RNF-062, RNF-097.

## Testes previstos

Implementados na fase de testes (RNF-016). Até lá, os critérios de aceite são verificados manualmente no navegador.

- **Componente/integração (Vitest + RTL + MSW):** lista, formulário e exclusão com `SUPPLIER_HAS_HISTORY` simulado; acesso por perfil (inversão em relação a clientes); `FornecedorSelect`.
- **Acessibilidade:** `vitest-axe` na lista, no formulário e no seletor.

## Pontos de atenção ao revisar código gerado por IA

- Todos os pontos de [RF-FE-003](./RF-FE-003.md#pontos-de-atenção-ao-revisar-código-gerado-por-ia).
- Cópia integral da feature `clientes` com troca de nomes, em vez de reutilizar os componentes compartilhados.
- Matriz de perfis copiada de clientes sem inverter (`OPERADOR_COMERCIAL` vendo fornecedores) ou rota digitada na URL sem guarda de perfil.
- Códigos de erro de clientes (`CUSTOMER_*`) tratados nesta tela, ou textos que dizem "cliente".
- Importar internos da feature `clientes` (viola RNF-FE-01): o que for comum vai para `shared/`.

## Extensões opcionais (desafios)

1. Campo de observações e contato principal do fornecedor.
2. Ordenação por coluna, preservada na URL.
3. Exportação da lista filtrada em CSV.
