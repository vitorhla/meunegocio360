# RF-FE-003 — Cadastro e busca de clientes

> **Branch:** `feat/rf-fe-003-clientes`  
> **Funcionalidade:** F03 — Cadastro de Clientes  
> **Dependências:** [RF-FE-002](./RF-FE-002.md) (layout, guardas, `ErrorAlert`, `ConfirmModal`, máscaras) e [RF-BE-003](../backend/RF-BE-003.md) (contrato de `/api/v1/clientes`).  
> **Consumido por:** telas futuras que referenciam clientes (vendas, equipamentos e ordens de serviço), por meio do seletor `ClienteSelect`.

## História de usuário

Como **administrador ou operador comercial**, quero **buscar, cadastrar, editar, inativar e excluir clientes**, e **encontrar um cliente por nome ou documento em qualquer tela que o referencie**, para **trabalhar com rapidez e sem cadastros duplicados**.

## Escopo

**Dentro do escopo (obrigatório):**

- Lista de clientes com busca, filtro de situação e paginação; formulário de cadastro e de edição; inativação, reativação e exclusão com confirmação.
- **Seletor reutilizável `ClienteSelect`** (busca por nome ou documento), exposto pela API pública da feature (`features/clientes/index.js`, RNF-FE-01).
- Componentes compartilhados que as demais listagens reutilizam: `ListToolbar` (busca com *debounce* e filtro), `Paginacao`, `StatusBadge`, `DocumentoInput` (máscara de CPF/CNPJ conforme o tipo) e o hook `useUrlFilters` (filtros na URL, RNF-FE-27).
- Item **Clientes** no menu para `ADMIN` e `OPERADOR_COMERCIAL`.

**Fora do escopo:** endereços e múltiplos contatos, importação e exportação, histórico de compras, telas de vendas e de OS.

## Dependências introduzidas

Nenhuma biblioteca nova (RNF-015). O seletor é construído sobre `react-bootstrap` seguindo o padrão ARIA de *combobox*; máscaras reutilizam as funções de `shared/lib` ([RF-FE-002](./RF-FE-002.md)), estendidas com CPF.

## Telas e rotas

Todas protegidas por `RequireAuth`, `RequireEmpresa` e `RequireRole` (`ADMIN`, `OPERADOR_COMERCIAL`).

| Rota | Descrição |
| --- | --- |
| `/clientes` | Lista: campo de busca ("Buscar por nome ou documento"), filtro **Situação** (Ativos, Inativos, Todos; padrão Ativos), tabela (nome, documento formatado, telefone, e-mail, situação, ações), paginação e botão **Novo cliente**. Os filtros e a página ficam na URL (`?q=&situacao=&page=`). |
| `/clientes/novo` | Formulário de cadastro. |
| `/clientes/:id` | Formulário de edição (carrega por `GET /clientes/{id}`; `CUSTOMER_NOT_FOUND` exibe a página "Cliente não encontrado"). |

Estados obrigatórios da lista (RNF-062): carregamento, **vazio sem filtro** ("Nenhum cliente cadastrado. Cadastre o primeiro cliente."), **vazio com filtro** ("Nenhum cliente encontrado para a busca.", com ação **Limpar filtros**), erro com **Tentar novamente** e sucesso.

Ações por linha: **Editar**, **Inativar** (ou **Reativar**) e **Excluir**. No celular, a tabela rola dentro do seu contêiner ou vira lista de cartões (sem rolagem horizontal da página).

## Formulário

| Campo | Controle | Regras (Zod, espelhando o backend) | Mensagens |
| --- | --- | --- | --- |
| Tipo de pessoa | Grupo de opções (`radio`): Pessoa física / Pessoa jurídica | Obrigatório | "Selecione o tipo de pessoa." |
| Nome (ou razão social) | `input type="text"` | Obrigatório; *trim*; 2 a 150 caracteres | "Informe o nome." |
| CPF ou CNPJ | `DocumentoInput`: máscara de CPF para física e de CNPJ para jurídica | Obrigatório; 11 dígitos (CPF) ou 14 (CNPJ) com dígitos verificadores | "Informe o documento." / "CPF inválido." / "CNPJ inválido." |
| E-mail | `input type="email"`, `autocomplete="email"` | Opcional; formato válido; até 160 | "Informe um e-mail válido." |
| Telefone | `input type="tel"`, máscara | Opcional; 10 ou 11 dígitos | "Informe um telefone válido com DDD." |

Ao trocar o tipo de pessoa, o documento digitado é revalidado (e limpo se o tamanho for incompatível, com aviso). Documento e telefone vão à API **somente com dígitos** (RNF-FE-28).

## Seletor `ClienteSelect`

Componente de seleção de cliente para qualquer tela futura: campo de texto com lista de sugestões (padrão *combobox* ARIA: `role="combobox"`, `aria-expanded`, `aria-controls`, `aria-activedescendant`; lista com `role="listbox"`).

- Busca por **nome ou documento** com *debounce* de 400 ms e a partir de 2 caracteres, chamando `GET /clientes?q=&situacao=ATIVO&size=10`; os estados "Buscando…", "Nenhum cliente encontrado" e "Erro ao buscar. Tentar novamente" são anunciados por região `aria-live`.
- Teclado: setas navegam, `Enter` seleciona, `Esc` fecha, `Tab` sai do campo.
- Propriedades: `value`, `onChange` (recebe `{ id, nome, documento }`), `invalid`, `disabled` e `label`. O seletor só oferece clientes **ativos**.
- A mesma lógica de busca (`useBuscaClientes`) é usada pela lista, de modo que a busca se comporta igual em todas as telas (cada tela futura reutiliza o `ClienteSelect`, nunca recria a busca).
- Enquanto não existe tela que o consuma, ele é verificado em uma rota de apoio `/dev/cliente-select`, registrada **somente** quando `import.meta.env.DEV` for verdadeiro.

## Comportamentos e mensagens

| Situação (resposta da API) | Comportamento na interface |
| --- | --- |
| `POST` `201` | "Cliente cadastrado com sucesso." e retorno à lista (com o novo cliente visível ao buscar). |
| `PUT` `200` | "Cliente atualizado com sucesso." e retorno à lista. |
| `409` `CUSTOMER_DOCUMENT_DUPLICATE` | Erro no campo documento: "Já existe um cliente com este documento. Ele pode estar inativo; busque por ele e reative." |
| `409` `VERSION_CONFLICT` | Alerta com **Recarregar dados** (RNF-FE-29). |
| `422` `COMPANY_NOT_ACTIVE` | Alerta "Cadastre ou ative a empresa para continuar." com link para `/empresa`. |
| `400` `VALIDATION_ERROR` | Itens de `errors[]` ligados aos campos; foco no primeiro inválido. |
| `403` | Página "Acesso negado". |
| Inativar / reativar | Inativar pede confirmação; sucesso exibe "Cliente inativado." / "Cliente reativado." e atualiza a lista sem recarregar a página. |
| Excluir | `ConfirmModal` ("Excluir o cliente <nome>? Esta ação não pode ser desfeita."). `204`: "Cliente excluído." e atualização da lista. |
| Excluir `422` `CUSTOMER_HAS_HISTORY` | O modal permanece aberto com o erro de negócio claro: "Este cliente possui vendas ou ordens de serviço vinculadas e não pode ser excluído. Você pode inativá-lo." e o botão **Inativar cliente**. Nunca uma mensagem genérica. |
| Excluir `404` `CUSTOMER_NOT_FOUND` | "Este cliente não existe mais." e atualização da lista. |
| Falha de rede / `5xx` | `ErrorAlert` com **Tentar novamente**; nenhum dado digitado é perdido. |

## Critérios de aceite

```gherkin
Cenário: Menu e acesso por perfil
  Dado um OPERADOR_COMERCIAL ou ADMIN
  Então o menu exibe "Clientes"
  E um OPERADOR_FINANCEIRO não vê o item e, ao abrir "/clientes" pela URL, vê "Acesso negado"

Cenário: Lista vazia
  Dado que não há clientes
  Quando abro "/clientes"
  Então vejo "Nenhum cliente cadastrado. Cadastre o primeiro cliente." e o botão "Novo cliente"

Cenário: Cadastro de cliente
  Quando preencho tipo "Pessoa física", nome "Maria da Silva" e CPF "529.982.247-25" e salvo
  Então o CPF é enviado como "52998224725"
  E vejo "Cliente cadastrado com sucesso." e a cliente aparece na lista

Cenário: Documento incompatível com o tipo
  Quando seleciono "Pessoa jurídica" e informo um CPF, ou informo um documento com dígito verificador errado
  Então vejo "CNPJ inválido." (ou "CPF inválido.") e nenhuma requisição é enviada

Cenário: Documento duplicado
  Dado que a API responde 409 com code CUSTOMER_DOCUMENT_DUPLICATE
  Quando salvo
  Então vejo o erro no campo documento com a orientação de buscar o cliente inativo

Cenário: Busca por nome na lista
  Dado o cliente "José Antônio"
  Quando digito "jose antonio" na busca
  Então, após o debounce, a lista mostra o cliente e a URL contém "?q=jose antonio"
  E ao recarregar a página a busca e o resultado se mantêm

Cenário: Busca por documento com máscara
  Quando digito "529.982" na busca
  Então a lista mostra o cliente com documento "529.982.247-25"

Cenário: Filtro de situação
  Dado clientes ativos e inativos
  Então a lista inicia somente com ativos
  E ao escolher "Inativos" ou "Todos" a lista e a URL são atualizadas

Cenário: Busca sem resultado
  Quando busco algo que não existe
  Então vejo "Nenhum cliente encontrado para a busca." e a ação "Limpar filtros"

Cenário: Paginação
  Dado mais clientes do que o tamanho da página
  Então a lista é paginada pelo servidor e a navegação entre páginas atualiza "?page="

Cenário: Edição
  Quando abro um cliente, altero o telefone e salvo
  Então vejo "Cliente atualizado com sucesso."

Cenário: Inativação e reativação
  Quando inativo um cliente e confirmo
  Então ele sai da lista de Ativos e aparece em Inativos com o selo "Inativo"
  E ao reativar ele volta aos Ativos

Cenário: Exclusão de cliente sem histórico
  Quando excluo um cliente sem vínculos e confirmo
  Então vejo "Cliente excluído." e ele sai da lista

Cenário: Exclusão de cliente com histórico
  Dado que a API responde 422 com code CUSTOMER_HAS_HISTORY
  Quando confirmo a exclusão
  Então vejo no modal a mensagem de negócio clara sugerindo inativar, e não um erro genérico
  E o botão "Inativar cliente" inativa o cliente a partir do próprio modal
  # Verificável de ponta a ponta quando existir o primeiro vínculo (vendas); até lá, com a resposta
  # da API simulada nos testes (ver "Testes previstos").

Cenário: Busca em qualquer tela que referencie o cliente
  Dado o ClienteSelect exibido na rota "/dev/cliente-select"
  Quando digito parte do nome ou do documento
  Então vejo as sugestões de clientes ativos, selecionáveis pelo mouse e pelo teclado
  E o seletor usa a mesma busca da lista de clientes

Cenário: Sem empresa ativa
  Dado que a API responde 422 com code COMPANY_NOT_ACTIVE ao salvar
  Então vejo o aviso para cadastrar ou ativar a empresa, com link para "/empresa"

Cenário: Acessibilidade e responsividade
  Então todos os campos têm rótulo, os erros são anunciados, o foco vai ao primeiro campo inválido e a tabela tem cabeçalhos associados
  E a lista e o formulário funcionam em 360 px sem rolagem horizontal da página e por teclado
```

## Requisitos não funcionais com destaque neste requisito

RNF-FE-01, RNF-FE-03, RNF-FE-04, RNF-FE-16, RNF-FE-20 a RNF-FE-23, RNF-FE-25, RNF-FE-27, RNF-FE-28, RNF-FE-29, RNF-060, RNF-062, RNF-097.

## Testes previstos

Implementados na fase de testes (RNF-016). Até lá, os critérios de aceite são verificados manualmente no navegador, contra o backend local.

- **Unitários:** máscara e validação de CPF e CNPJ, *schema* Zod do cliente, `useUrlFilters`.
- **Componente/integração (Vitest + RTL + MSW):** lista (todos os estados, busca com *debounce*, filtros na URL, paginação), formulário (validações e erros `409`/`422`), exclusão com `CUSTOMER_HAS_HISTORY` **simulado pelo MSW**, e `ClienteSelect` (teclado, estados, somente ativos).
- **Acessibilidade:** `vitest-axe` na lista, no formulário e no seletor.

## Pontos de atenção ao revisar código gerado por IA

- `fetch` solto nos componentes, `useEffect` para buscar dados e busca disparada a cada tecla, sem *debounce* nem cancelamento de requisições antigas (resultado "atrasado" sobrescrevendo o novo).
- Filtro e paginação feitos no navegador sobre todos os clientes; filtros que não vão para a URL.
- Seletor recriado em cada tela, com busca própria, em vez de reutilizar `ClienteSelect`; seletor sem padrão ARIA ou sem operação por teclado; oferecendo clientes inativos.
- Mensagem genérica ("Erro ao excluir") no lugar do erro de negócio `CUSTOMER_HAS_HISTORY`; decisão pelo texto da mensagem em vez do `code`.
- Documento enviado com máscara; troca do tipo de pessoa sem revalidar o documento.
- Exclusão sem confirmação, ou botão que continua clicável durante a requisição.
- Tabela sem `<th scope>` / legenda, ou que força rolagem horizontal da página em telas pequenas.
- Dados pessoais exibidos em logs do console ou guardados em `localStorage`.
- Cores e estilos fora do tema do Bootstrap; pacote de tabela, de máscara ou de formulários instalado sem justificativa (RNF-015).

## Extensões opcionais (desafios)

1. Ordenação por coluna, preservada na URL.
2. Criação rápida de cliente a partir de dentro do `ClienteSelect` (modal), mantendo o foco ao voltar.
3. Exportação da lista filtrada em CSV.
