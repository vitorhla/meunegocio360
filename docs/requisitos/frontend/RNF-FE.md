# MeuNegocio360 — Requisitos Não Funcionais do Frontend

| | |
| --- | --- |
| **Versão** | 3.0 |
| **Atualizado em** | 18/11/2026 |
| **Projeto** | `meunegocio360-js` |
| **Herda de** | [`GERAIS.md`](../GERAIS.md) (todos os RNF se aplicam; aqui só há o que é específico do frontend) |
| **Contrato de API** | [`RNF-BE.md`](../backend/RNF-BE.md) e `RF-BE-NNN.md` (o OpenAPI do backend é a fonte da verdade) |
| **Requisitos funcionais** | [`RF-FE-001.md`](./RF-FE-001.md) a [`RF-FE-005.md`](./RF-FE-005.md) (índice em [`GERAIS.md`](../GERAIS.md#2-requisitos-funcionais)) |
## 1. Arquitetura

A estrutura de pastas do frontend está no [`README.md`](../../../README.md#frontend). Regras:

| ID | Requisito |
| --- | --- |
| RNF-FE-01 | Uma feature NÃO DEVE importar arquivos internos de outra; só pelo `index.js` público. `shared/` NÃO DEVE importar de `features/`. |
| RNF-FE-02 | Componentes são funções com *hooks*; sem componentes de classe. Lógica de dados e efeitos ficam em *hooks* e não em JSX. |
| RNF-FE-03 | Chamadas HTTP NUNCA são feitas direto em componentes: passam pela camada `features/<feature>/api` sobre o cliente HTTP único de `shared/lib`. |
| RNF-FE-04 | Dados de servidor ficam no TanStack Query (cache, *loading*, erro). Estado global próprio de aplicação (ex.: sessão) via Context. Evitar `useEffect` para buscar dados e "prop drilling" além de dois níveis. |
| RNF-FE-05 | Rotas carregadas com *code splitting* (`React.lazy`) por página, com *fallback* acessível de carregamento. |
| RNF-FE-06 | Chaves de lista, `useEffect` com dependências corretas e ausência de avisos do `eslint-plugin-react-hooks` são obrigatórios. |
| RNF-FE-07 | Todo estilo DEVE usar o **Bootstrap 5** (grid, utilitários e componentes como `.btn`, `.form-control`, `.card`, `.navbar`, `.table`, `.alert`, `.spinner-border`). NÃO DEVE usar outro framework de CSS nem recriar componentes que o Bootstrap já oferece. |
| RNF-FE-08 | O Bootstrap DEVE ser importado a partir do Sass (`bootstrap/scss`), com a personalização do tema concentrada em `src/shared/styles/theme.scss` (seção 2). CSS próprio só para o que o Bootstrap não cobre, sempre reutilizando variáveis do tema. Cores, fontes e tamanhos NÃO DEVEM ser escritos direto em componentes nem em `style` inline. |
| RNF-FE-09 | Componentes com comportamento (menus, colapsos, modais, *dropdowns*, *toasts*) DEVEM usar `react-bootstrap`. O JavaScript do Bootstrap NÃO DEVE manipular o DOM diretamente em componentes React. |

## 2. Estilos e identidade visual

A interface é construída sobre o **Bootstrap 5** (RNF-FE-07). A identidade da marca é aplicada **uma única vez**, no tema do Bootstrap (RNF-FE-08).

### 2.1 Paleta

| Cor | Valor | Uso | Variável Sass do Bootstrap |
| --- | --- | --- | --- |
| Laranja | `#EC4E26` | Botões primários, barra de navegação, destaque de marca | `$primary` |
| Creme | `#FCF5F0` | Fundo de páginas e cartões | `$body-bg`, `$card-bg` |
| Cinza | `#4A3F39` | Linhas de tabelas, bordas de cartões e texto secundário | `$border-color`, `$table-border-color`, `$card-border-color`, `$body-secondary-color` |
| Preto | `#1F1612` | Texto principal | `$body-color`, `$color-contrast-dark` |
| Laranja amarronzado claro | `#CDBBAE` | Rodapé | Cor adicionada ao mapa `$theme-colors` (ex.: `footer`, gerando `bg-footer`) |

Cores de feedback e foco também DEVEM ser definidas no tema, com contraste AA contra o fundo em que forem usadas (seção 2.2): `$danger`, `$success` e o anel de foco (`$focus-ring-color`).

### 2.2 Contraste (WCAG 2.2 AA, RNF-060)

Razões de contraste calculadas para os pares da paleta e para as cores de feedback padrão do Bootstrap:

| Texto sobre fundo | Razão | Texto normal (mín. 4,5:1) | Texto grande (mín. 3:1) |
| --- | --- | --- | --- |
| Preto sobre Creme | 16,5:1 | Passa | Passa |
| Cinza sobre Creme | 9,4:1 | Passa | Passa |
| Preto sobre Laranja | 4,8:1 | Passa | Passa |
| Cinza sobre Laranja amarronzado | 5,5:1 | Passa | Passa |
| **Creme sobre Laranja** | **3,4:1** | **Não passa** | Passa |
| **Laranja sobre Creme** | **3,4:1** | **Não passa** | Passa |
| **Branco sobre Laranja** | **3,7:1** | **Não passa** | Passa |
| **`$danger` padrão do Bootstrap (`#DC3545`) sobre Creme** | **4,2:1** | **Não passa** | Passa |
| **`$success` padrão do Bootstrap (`#198754`) sobre Creme** | **4,2:1** | **Não passa** | Passa |
| `#B02A37` (vermelho) sobre Creme | 6,0:1 | Passa | Passa |
| `#146C43` (verde) sobre Creme | 6,0:1 | Passa | Passa |

Consequências obrigatórias no tema e no uso dos componentes:

- **Botão primário:** texto **Preto** sobre Laranja. Manter `$min-contrast-ratio` em 4,5 (padrão) e definir `$color-contrast-dark` como o Preto da marca, para que o Bootstrap escolha o texto de `.btn-primary` automaticamente. Branco e Creme sobre Laranja só são permitidos em texto grande (≥ 24 px, ou ≥ 18,66 px em negrito).
- **Barra de navegação laranja:** texto e links em Preto. NÃO usar `navbar-dark`, `text-white` nem texto Creme.
- **Botões secundários (`btn-outline-*`) e estados `:hover`** com texto Laranja sobre Creme: só com texto grande/negrito e borda visível. Para texto pequeno, usar Preto (`btn-outline-dark`) ou Cinza.
- **Links:** `$link-color` em Preto (ou Cinza), sempre sublinhados, pois Laranja sobre Creme não atende ao contraste em texto normal.
- **Feedback:** definir `$danger` como `#B02A37` e `$success` como `#146C43`.
- **Foco:** anel de foco com contraste mínimo de 3:1 contra o fundo.
- Informação NUNCA DEVE ser transmitida apenas por cor (ex.: erro precisa de ícone ou texto, além de vermelho).

### 2.3 Tipografia

| Uso | Fonte | Pesos | Variável Sass do Bootstrap |
| --- | --- | --- | --- |
| Títulos | Barlow Condensed | 500, 600 e 700 | `$headings-font-family`, `$headings-font-weight` |
| Texto, tabelas, formulários e rodapé | Barlow | 400, 500 e 600 | `$font-family-sans-serif`, `$font-weight-base` |

- As fontes DEVEM ser **auto-hospedadas** via `@fontsource` (importando apenas os pesos acima), e não carregadas do Google Fonts: evita requisição a terceiros (privacidade/LGPD) e melhora desempenho.
- Corpo de texto com no mínimo 16 px (`$font-size-base` de `1rem`).

## 3. Camada HTTP, sessão e roteamento

| ID | Requisito |
| --- | --- |
| RNF-FE-10 | Um único cliente HTTP (`shared/lib/http`), baseado em `fetch`, lê a URL base de `VITE_API_BASE_URL`, que DEVE ser **relativa** (`/api/v1`) e única para todos os serviços (RNF-103). Em desenvolvimento, o `server.proxy` do Vite repassa `/api` ao backend local. As chamadas aos endpoints `/auth/*` DEVEM enviar o cookie de refresh (`credentials: "same-origin"`, o padrão do `fetch`). Variáveis `VITE_*` são **públicas**: NUNCA colocar segredos nelas. |
| RNF-FE-11 | O cliente DEVE: injetar `Authorization: Bearer`, enviar `X-Request-Id`, aplicar *timeout* (`AbortController`), interpretar `application/problem+json` em um erro tipado (`ApiError` com `status`, `code`, `detail`, `errors`) e tratar `401` globalmente: tentar **uma única renovação** de sessão (`/auth/refresh`) e repetir a requisição original; se a renovação falhar, encerrar a sessão e redirecionar ao login. |
| RNF-FE-12 | Tokens NÃO DEVEM ser guardados em `localStorage` nem `sessionStorage` (acessíveis a qualquer script, inclusive um XSS). O **token de acesso** fica **em memória** (contexto de autenticação). O **refresh token** é um cookie `HttpOnly` gerenciado pelo navegador e **inacessível ao JavaScript**: o frontend nunca o lê nem o grava. |
| RNF-FE-15 | Renovações simultâneas dentro da mesma aba DEVEM ser **agrupadas** (*single-flight*): se várias requisições recebem `401` ao mesmo tempo, apenas uma chamada a `/auth/refresh` é feita e todas aguardam seu resultado. Como o refresh token é rotativo, duas chamadas paralelas com o mesmo cookie derrubariam a sessão (detecção de reuso do backend). |
| RNF-FE-16 | A interface DEVE exibir menus e ações conforme o `perfil` da sessão (por exemplo, ocultar o que o perfil não pode usar) e tratar `403` com mensagem clara. Isso é apenas UX: a autorização real é do backend (RNF-BE-47). |
| RNF-FE-13 | Rotas protegidas DEVEM usar um componente de guarda (`RequireAuth`). Controle de acesso no frontend é apenas UX; a autorização real é do backend. |
| RNF-FE-14 | A decisão de comportamento diante de erros usa o campo `code` do erro, nunca o texto da mensagem. |

## 4. Formulários, acessibilidade e UX

| ID | Requisito |
| --- | --- |
| RNF-FE-20 | Formulários com React Hook Form + schema Zod, com a marcação de validação do Bootstrap (`is-invalid`, `invalid-feedback`) e mensagens em português. A validação do cliente espelha a do servidor, mas nunca a substitui (RNF-020). |
| RNF-FE-21 | Todo campo DEVE ter `<label>` visível associado, `autocomplete` correto, tipo de `input` adequado e mensagem de erro ligada por `aria-describedby`/`aria-invalid`. |
| RNF-FE-22 | Erros de formulário e de servidor DEVEM ser anunciados a leitores de tela (`role="alert"` ou região `aria-live`) e o foco levado ao primeiro campo inválido ou à mensagem. |
| RNF-FE-23 | Navegação completa por teclado, ordem de foco lógica, foco visível com contraste mínimo de 3:1 e alvos interativos com pelo menos 44×44 px. |
| RNF-FE-24 | HTML semântico (`main`, `nav`, `header`, `footer`, `h1` único por tela, botões `<button>`, links `<a>`). Atributo `lang="pt-BR"` no documento e título de página (`<title>`) por rota. |
| RNF-FE-25 | Botões que disparam requisições DEVEM ficar desabilitados e exibir estado de carregamento, impedindo envio duplicado. |
| RNF-FE-26 | Layout *mobile-first* com a grade do Bootstrap (RNF-061). Respeitar `prefers-reduced-motion`. |
| RNF-FE-27 | Telas de listagem DEVEM ter busca com *debounce* (400 ms), filtros e página refletidos na **URL** (*query string*), paginação no servidor (RNF-054) e os estados de carregamento, vazio, erro e sucesso (RNF-062). |
| RNF-FE-28 | CPF, CNPJ, telefone e valores monetários são exibidos e digitados com máscara em pt-BR, mas enviados à API apenas como dígitos (documentos e telefones) e como decimal em *string* (valores, RNF-033). A formatação fica em funções únicas de `shared/lib`. |
| RNF-FE-29 | Erros de negócio DEVEM ser mostrados em mensagem clara e específica (por `code`, RNF-FE-14), nunca como texto técnico ou mensagem genérica; erros de campo (`errors[]`) são ligados ao campo correspondente. `409 VERSION_CONFLICT` oferece recarregar o registro. |

## 5. Segurança, desempenho e testes

| ID | Requisito |
| --- | --- |
| RNF-FE-30 | NÃO DEVE usar `dangerouslySetInnerHTML` com conteúdo não sanitizado, `eval` ou injeção manual de HTML. Dados da API são sempre tratados como não confiáveis. |
| RNF-FE-31 | Dependências auditadas (RNF-026) e *lockfile* versionado. Dependências sugeridas por IA DEVEM ser conferidas quanto à existência, versão, licença e manutenção antes da instalação. |
| RNF-FE-32 | Orçamento de desempenho (build de produção): carga inicial de JavaScript idealmente ≤ 200 kB comprimidos; LCP ≤ 2,5 s em conexão simulada de 4G para telas públicas. |
| RNF-FE-33 | Na fase de testes, consultas por papel e acessibilidade (`getByRole`, `getByLabelText`) são o padrão em testes. `data-testid` só como último recurso. |
| RNF-FE-34 | Na fase de testes, a API é simulada com **MSW**, usando respostas fiéis ao contrato do backend (inclusive `problem+json`). Não usar *mocks* manuais do `fetch`. |
| RNF-FE-35 | Na fase de testes, toda página nova DEVE ter pelo menos uma verificação automática de acessibilidade com `vitest-axe`, sem violações. |
