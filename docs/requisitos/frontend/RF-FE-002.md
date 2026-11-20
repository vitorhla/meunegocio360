# RF-FE-002 — Cadastro de empresa, layout da aplicação e guardas de acesso

> **Branch:** `feat/rf-fe-002-empresa`  
> **Funcionalidade:** F02 — Cadastro de Empresa e Parâmetros Básicos  
> **Dependências:** [RF-FE-001](./RF-FE-001.md) (sessão) e [RF-BE-002](../backend/RF-BE-002.md) (contrato de `/api/v1/empresa`). O frontend é desenvolvido contra o backend em execução local.  
> **Consumido por:** [RF-FE-003](./RF-FE-003.md), [RF-FE-004](./RF-FE-004.md) e [RF-FE-005](./RF-FE-005.md), que reutilizam o layout, as guardas e os componentes introduzidos aqui.

## História de usuário

Como **administrador**, quero **cadastrar e manter os dados da minha empresa em uma tela simples**, para que **o sistema use esse contexto em todos os módulos**; como **qualquer usuário**, quero **ver o nome da empresa e navegar apenas pelo que meu perfil pode usar**.

## Escopo

**Dentro do escopo (obrigatório):**

- Telas de **cadastro inicial** e de **edição** da empresa, inativação e reativação.
- **Layout da aplicação** (barra de navegação com o nome da empresa e menu por perfil), que passa a envolver as rotas protegidas, inclusive a página inicial.
- **Guardas de rota**: `RequireRole` (perfil) e `RequireEmpresa` (empresa ativa).
- Estado `EmpresaProvider` com a empresa ativa e os parâmetros de exibição.
- Componentes e funções compartilhados: `ErrorAlert`, `ConfirmModal`, formatação e validação de CNPJ e de telefone (RNF-FE-28).

**Fora do escopo:** gestão de usuários e perfis, logotipo da empresa, troca de empresa (multiempresa) e dashboard.

## Dependências introduzidas

Nenhuma biblioteca nova (RNF-015): usa React Router, TanStack Query, React Hook Form, Zod e react-bootstrap, já introduzidos por [RF-FE-001](./RF-FE-001.md). Máscaras e validações são funções próprias em `shared/lib`, sem pacote adicional.

## Perfis e acesso

Os perfis vêm de `usuario.perfil` na sessão: `ADMIN`, `OPERADOR_COMERCIAL`, `OPERADOR_FINANCEIRO`, centralizados em uma constante em `shared/lib` (nunca escritos como texto solto). O menu e as ações exibidos seguem o perfil (RNF-FE-16); a autorização real é do backend.

| Item de menu | Rota | `ADMIN` | `OPERADOR_COMERCIAL` | `OPERADOR_FINANCEIRO` |
| --- | --- | :---: | :---: | :---: |
| Início | `/` | ✔ | ✔ | ✔ |
| Empresa | `/empresa` | edita | consulta | consulta |
| Clientes ([RF-FE-003](./RF-FE-003.md)) | `/clientes` | ✔ | ✔ | — |
| Fornecedores ([RF-FE-004](./RF-FE-004.md)) | `/fornecedores` | ✔ | — | ✔ |
| Catálogo ([RF-FE-005](./RF-FE-005.md)) | `/catalogo` | ✔ | ✔ | — |

Cada RF seguinte acrescenta o seu item ao menu; até lá, o menu mostra apenas **Início** e **Empresa**.

## Telas e rotas

| Rota | Acesso | Descrição |
| --- | --- | --- |
| `/empresa/cadastro` | `ADMIN`, somente sem empresa ativa | Formulário do primeiro cadastro, com o título "Cadastre sua empresa". Com empresa ativa, redireciona para `/empresa`. |
| `/empresa` | Autenticado | Exibe os dados da empresa ativa. `ADMIN` vê o botão **Editar**, o formulário em modo de edição e as ações **Inativar**/**Reativar**. Os demais perfis veem os dados somente em leitura. |
| `/` e demais rotas protegidas | Autenticado **com empresa ativa** (`RequireEmpresa`) | Dentro do `AppLayout`. |

### Comportamento das guardas

1. `RequireAuth` ([RF-FE-001](./RF-FE-001.md)) garante a sessão; `EmpresaProvider` consulta `GET /empresa` uma vez por sessão (TanStack Query) e expõe os estados `carregando`, `ativa`, `ausente` (`404 COMPANY_NOT_REGISTERED`) e `erro`.
2. `RequireEmpresa` envolve as rotas de negócio. Estado `ausente`: `ADMIN` é redirecionado para `/empresa/cadastro`; os demais perfis veem a página "Aguardando o cadastro da empresa" (com orientação para falar com o administrador), sem acesso às telas de negócio. Estado `erro`: tela com **Tentar novamente**, como na restauração de sessão.
3. `RequireRole` recebe a lista de perfis permitidos. Perfil sem permissão vê uma página "Acesso negado" (`403`) com link para o início, sem revelar dados.
4. Nenhuma tela protegida é renderizada antes de a sessão e a empresa serem resolvidas (sem *flash* de conteúdo).

## Formulário da empresa

| Campo | Controle | Regras (Zod, espelhando o backend) | Mensagens |
| --- | --- | --- | --- |
| Razão social | `input type="text"` | Obrigatório; *trim*; 3 a 150 caracteres | "Informe a razão social." |
| Nome fantasia | `input type="text"` | Opcional; até 150 caracteres | — |
| CNPJ | `input` com máscara `00.000.000/0000-00`, `inputMode="numeric"` | Obrigatório; 14 dígitos; dígitos verificadores válidos (função de `shared/lib`) | "Informe o CNPJ." / "CNPJ inválido." |
| E-mail | `input type="email"`, `autocomplete="email"` | Opcional; formato de e-mail; até 160 | "Informe um e-mail válido." |
| Telefone | `input type="tel"`, `autocomplete="tel"`, máscara `(00) 00000-0000` | Opcional; 10 ou 11 dígitos | "Informe um telefone válido com DDD." |
| Fuso horário | `select` | Obrigatório; lista de fusos brasileiros mais `UTC` (padrão `America/Sao_Paulo`) | — |
| Moeda | `select` desabilitado | Somente `BRL` (nesta fase) | — |

CNPJ e telefone são enviados **somente com dígitos** (RNF-FE-28). O campo `versao` vai oculto no `PUT`.

## Comportamentos e mensagens

| Situação (resposta da API) | Comportamento na interface |
| --- | --- |
| `GET /empresa` `404` `COMPANY_NOT_REGISTERED` | Estado `ausente` das guardas (ver acima). |
| `POST` `201` | Atualiza o `EmpresaProvider`, exibe "Empresa cadastrada com sucesso." e leva o usuário ao início `/`. |
| `PUT` `200` | Exibe "Dados da empresa atualizados." e volta ao modo de leitura. |
| `409` `COMPANY_CNPJ_DUPLICATE` | Erro no campo CNPJ: "Já existe uma empresa cadastrada com este CNPJ." |
| `409` `COMPANY_ALREADY_ACTIVE` | `ErrorAlert`: "Já existe uma empresa ativa. Inative-a antes de cadastrar ou reativar outra." |
| `409` `VERSION_CONFLICT` | `ErrorAlert` com o botão **Recarregar dados** (RNF-FE-29). |
| `400` `VALIDATION_ERROR` | Cada item de `errors[]` é ligado ao seu campo; foco no primeiro inválido. |
| `403` | `ErrorAlert`: "Você não tem permissão para alterar os dados da empresa." |
| Inativação | `ConfirmModal`: "Ao inativar a empresa, não será possível cadastrar clientes, fornecedores, produtos nem vendas até que uma empresa seja ativada." Confirmada, `PATCH` com `ativa: false`; as guardas passam ao estado `ausente`. |
| Falha de rede / `5xx` | `ErrorAlert` genérico com **Tentar novamente**; o formulário preserva o que foi digitado. |

O nome fantasia (ou a razão social, se não houver) aparece na barra de navegação para todos os usuários, ao lado do nome do usuário e do botão **Sair**.

## Critérios de aceite

```gherkin
Cenário: Administrador sem empresa cadastrada é levado ao primeiro cadastro
  Dado que não existe empresa ativa
  Quando um ADMIN entra no sistema
  Então é redirecionado para "/empresa/cadastro" com o título "Cadastre sua empresa"
  E não consegue abrir "/", "/clientes" ou outras telas de negócio por URL

Cenário: Operador sem empresa cadastrada
  Dado que não existe empresa ativa
  Quando um OPERADOR_COMERCIAL entra no sistema
  Então vê "Aguardando o cadastro da empresa" e nenhuma tela de negócio

Cenário: Cadastro da empresa
  Dado o formulário de primeiro cadastro
  Quando preencho razão social, CNPJ "11.222.333/0001-81" e envio
  Então o CNPJ é enviado como "11222333000181"
  E vejo "Empresa cadastrada com sucesso." e sou levado ao início
  E a barra de navegação mostra o nome da empresa

Cenário: CNPJ inválido
  Quando informo o CNPJ "11.222.333/0001-80"
  Então vejo "CNPJ inválido." no campo e nenhuma requisição é enviada

Cenário: CNPJ duplicado
  Dado que a API responde 409 com code COMPANY_CNPJ_DUPLICATE
  Quando envio o formulário
  Então vejo "Já existe uma empresa cadastrada com este CNPJ." no campo CNPJ

Cenário: Máscara e dígitos
  Quando digito "11222333000181" no campo CNPJ
  Então o campo exibe "11.222.333/0001-81"
  E ao colar um valor com letras apenas os dígitos são mantidos

Cenário: Edição da empresa
  Dado uma empresa ativa e um ADMIN em "/empresa"
  Quando clico em Editar, altero o nome fantasia e salvo
  Então vejo "Dados da empresa atualizados." e o novo nome na barra de navegação

Cenário: Conflito de versão
  Dado que a API responde 409 com code VERSION_CONFLICT
  Quando salvo a edição
  Então vejo o aviso com o botão "Recarregar dados" e os dados digitados não são perdidos antes da escolha

Cenário: Operador consulta a empresa em modo leitura
  Dado um OPERADOR_FINANCEIRO em "/empresa"
  Então vejo os dados sem os botões Editar, Inativar e Reativar

Cenário: Inativação com confirmação
  Dado um ADMIN em "/empresa"
  Quando clico em Inativar
  Então o modal pede confirmação e explica a consequência
  E ao cancelar nada muda
  E ao confirmar a empresa fica inativa e as telas de negócio são bloqueadas pelas guardas

Cenário: Acesso negado por perfil
  Dado um usuário sem permissão para uma rota
  Quando abre a URL diretamente
  Então vê a página "Acesso negado" e nenhuma requisição de dados é feita

Cenário: Menu por perfil
  Dado um OPERADOR_COMERCIAL
  Então o menu mostra Início e Empresa e não mostra itens exclusivos de outros perfis

Cenário: Acessibilidade e teclado
  Então todos os campos têm rótulo visível, erros são anunciados por role="alert" e o foco vai ao primeiro campo inválido
  E o formulário e o modal são operáveis apenas pelo teclado, com o foco retornando ao botão de origem ao fechar o modal
  E a tela funciona em 360 px de largura sem rolagem horizontal

Cenário: Envio duplicado
  Quando clico duas vezes em Salvar
  Então somente uma requisição é enviada e o botão exibe o estado de carregamento
```

## Requisitos não funcionais com destaque neste requisito

RNF-FE-05, RNF-FE-13, RNF-FE-16, RNF-FE-20 a RNF-FE-25, RNF-FE-28, RNF-FE-29, RNF-060, RNF-061, RNF-062, RNF-020.

## Testes previstos

Implementados na fase de testes (RNF-016). Até lá, os critérios de aceite são verificados manualmente no navegador, contra o backend local.

- **Unitários:** validação e máscara de CNPJ e de telefone; *schema* Zod da empresa.
- **Componente/integração (Vitest + RTL + MSW):** guardas `RequireEmpresa` e `RequireRole` em todos os estados, formulário (validações, erros `409`/`400`/`403`), modal de inativação e menu por perfil.
- **Acessibilidade:** `vitest-axe` no formulário, na página da empresa, em "Acesso negado" e em "Aguardando o cadastro".

## Pontos de atenção ao revisar código gerado por IA

- Instalar uma biblioteca de máscara ou de validação de CNPJ sem necessidade, contra o RNF-015; ou duplicar a função de CNPJ em vários componentes.
- Enviar o CNPJ com máscara à API, ou aceitar `empresa_id` em qualquer formulário.
- Controle de acesso **só** no menu: a guarda precisa bloquear a rota digitada na URL (e o backend, a API).
- Perfil lido de `localStorage` ou de outra fonte que não a sessão; perfis escritos como texto solto em vários arquivos.
- Chamar `GET /empresa` em cada componente em vez de uma vez no `EmpresaProvider`; renderizar telas antes de resolver a empresa.
- Tratar `COMPANY_NOT_REGISTERED` como erro genérico e exibir "falha ao carregar".
- Mensagens técnicas do servidor exibidas ao usuário; falta de `aria-live` e de retorno do foco ao fechar o modal.
- Cores, fontes e tamanhos fora do tema do Bootstrap (RNF-FE-08).

## Extensões opcionais (desafios)

1. Pré-preenchimento dos dados do CNPJ por serviço público de consulta (com tratamento de indisponibilidade).
2. Logotipo da empresa exibido na barra de navegação.
3. Alternância entre empresas, caso o backend evolua para multiempresa.
