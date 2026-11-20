# RF-FE-001 — Fluxo de login do usuário

> **Branch:** `feat/rf-fe-001-login`  
> **Dependências:** `RF-BE-001` (contrato de `POST /api/v1/auth/login`, `/auth/refresh`, `/auth/logout` e `GET /api/v1/auth/me`). O frontend é desenvolvido contra o backend em execução local.

## História de usuário

Como **usuário cadastrado**, quero **informar meu e-mail e minha senha em uma tela de login simples e acessível e continuar autenticado ao recarregar a página**, para **usar o sistema sem precisar entrar de novo a cada atualização**, vendo somente as áreas protegidas quando estiver autenticado.

## Escopo

**Dentro do escopo (obrigatório):** tela `/login`, validação do formulário, integração com a API, contexto de autenticação com token de acesso em memória, **restauração de sessão por refresh token (cookie `HttpOnly`)** na inicialização, renovação automática diante de `401`, rota protegida `/` (página inicial provisória), redirecionamento pós-login, botão de sair, tratamento de sessão expirada e de todos os estados de erro, remoção do conteúdo de exemplo do template Vite (`App.jsx`, contador, logos), aplicação do tema do Bootstrap (seção 2) e telas construídas com componentes do Bootstrap.

**Fora do escopo:** cadastro de usuário, "esqueci minha senha", "lembrar de mim" (a duração da sessão é definida pelo backend), controle de acesso por perfil e menus definitivos.

## Dependências introduzidas

O `package.json` gerado já contém a **base**: React, ReactDOM e a toolchain Vite/ESLint (scripts `dev`, `build`, `lint` e `preview`). Este requisito introduz (RNF-015):

| Dependência | Para quê |
| --- | --- |
| `react-router` | Roteamento e rotas protegidas |
| `@tanstack/react-query` | Estado de servidor (cache, carregamento, erro) |
| `react-hook-form`, `zod`, `@hookform/resolvers` | Formulários e validação |
| `bootstrap`, `react-bootstrap`, `sass` | Estilos e componentes (RNF-FE-07 a RNF-FE-09) |
| `@fontsource/barlow`, `@fontsource/barlow-condensed` | Fontes auto-hospedadas |
| `prettier` | Formatação (RNF-082), com o script `format` |

Dependências de teste (Vitest, Testing Library, MSW, `vitest-axe`) entram na fase de testes. A variável `VITE_API_BASE_URL` é documentada em `meunegocio360-js/.env.example`.

## Telas e rotas

| Rota | Acesso | Descrição |
| --- | --- | --- |
| `/login` | Público (usuário já autenticado é redirecionado para `/`) | Formulário de login centralizado, dentro de um `card`, com o título "Entrar" (Barlow Condensed) e o nome do produto. |
| `/` | Protegido | Página inicial provisória: saudação com o nome do usuário, perfil e botão **Sair**. Será substituída pelo dashboard em requisito futuro. |
| `*` | Público | Página "não encontrada" com link para a página inicial. |

## Ciclo de vida da sessão

1. **Inicialização do app:** o `AuthProvider` entra no estado `carregando` e chama `POST /auth/refresh` (com `credentials: "include"`). Se responder `200`, guarda `accessToken` e `usuario` em memória e passa a `autenticado`; se responder `401`, passa a `anônimo`. Durante `carregando`, o app exibe um indicador de carregamento acessível (`role="status"`) e **não** redireciona nem renderiza telas protegidas ou de login, para evitar "piscar" a tela de login a quem já tem sessão.
2. **Login:** `POST /auth/login` → `autenticado`.
3. **Requisição autenticada com `401`:** renovação e repetição da requisição conforme RNF-FE-11 e RNF-FE-15. Se a renovação falhar, a sessão é encerrada com a mensagem de sessão expirada.
4. **Renovação preventiva (recomendada):** agendar a renovação cerca de 60 s antes de `expiresIn`, cancelando o agendamento no logout e ao desmontar o provider.
5. **Logout:** `POST /auth/logout` (o resultado é ignorado em caso de falha de rede), descarte do token e do cache do TanStack Query, e redirecionamento a `/login`.

## Campos e validações do formulário

| Campo | Controle | Regras (Zod) | Mensagens |
| --- | --- | --- | --- |
| E-mail | `input type="email"`, `autocomplete="username"` | Obrigatório; *trim*; formato de e-mail; máx. 160 caracteres | "Informe seu e-mail." / "Informe um e-mail válido." |
| Senha | `input type="password"`, `autocomplete="current-password"` | Obrigatório; 8 a 72 caracteres | "Informe sua senha." / "A senha deve ter entre 8 e 72 caracteres." |

Validação no envio e, depois do primeiro envio, a cada alteração. Botão **Entrar** com estado de carregamento. O campo de senha NÃO DEVE bloquear colar/gerenciadores de senha.

## Comportamentos e mensagens

| Situação (resposta da API) | Comportamento na interface |
| --- | --- |
| Login `200` | Guarda `accessToken` e `usuario` **em memória** (contexto), limpa o formulário e redireciona para a rota de origem (`from`) ou `/`. |
| Login `400 VALIDATION_ERROR` | Mostra os erros do servidor junto aos campos indicados em `errors[].field`. |
| Login `401 INVALID_CREDENTIALS` | Alerta no topo do formulário: "E-mail ou senha inválidos." Mantém o e-mail digitado, limpa a senha e devolve o foco ao campo de senha. |
| Login `429 ACCOUNT_TEMPORARILY_LOCKED` | Alerta: "Muitas tentativas. Tente novamente em X minutos." (calculado a partir de `Retry-After`). Botão desabilitado durante a contagem. |
| Falha de rede / *timeout* / `5xx` no login | Alerta: "Não foi possível entrar agora. Verifique sua conexão e tente novamente." com opção de tentar de novo. |
| Falha de rede / `5xx` na restauração de sessão (inicialização) | NÃO é tratada como sessão inválida: exibir tela de erro com "Tentar novamente", sem apagar nada. Somente `401` leva ao estado `anônimo`. |
| `401` em chamada autenticada e renovação bem-sucedida | Transparente para o usuário: a requisição é repetida com o novo token. |
| `401` em chamada autenticada e renovação falha (`INVALID_REFRESH_TOKEN`) | Encerra a sessão, redireciona para `/login` com a mensagem "Sua sessão expirou. Entre novamente." e preserva a rota de origem. |
| Clique em **Sair** | Conforme o ciclo de vida (item 5). |

Mensagens de erro NÃO DEVEM revelar se o e-mail existe (RN-03 do backend).

## Critérios de aceite

```gherkin
Cenário: Login com sucesso
  Dado que estou na tela /login e não estou autenticado
  Quando informo e-mail e senha válidos e envio o formulário
  Então vejo o botão em estado de carregamento durante a requisição
  E sou redirecionado para "/"
  E vejo meu nome na página inicial
  E nenhum token foi gravado em localStorage nem sessionStorage
  E o JavaScript da página não consegue ler o cookie de refresh (document.cookie não o contém)

Cenário: Sessão restaurada ao recarregar
  Dado que estou autenticado em "/"
  Quando recarrego a página
  Então vejo um indicador de carregamento, sem passar pela tela de login
  E volto a ver minha página inicial já autenticado

Cenário: Visitante sem sessão ao abrir o app
  Dado que não tenho cookie de refresh válido
  Quando abro "/"
  Então a restauração de sessão resulta em 401 e sou redirecionado para "/login"

Cenário: Renovação transparente do token de acesso
  Dado que estou autenticado e o token de acesso expirou
  Quando uma chamada autenticada recebe 401
  Então o app renova a sessão uma única vez e repete a chamada com sucesso
  E o usuário não percebe a renovação

Cenário: Várias chamadas expiradas ao mesmo tempo
  Dado que várias chamadas autenticadas recebem 401 simultaneamente
  Então é feita apenas uma chamada a /auth/refresh
  E todas as chamadas são repetidas com o novo token

Cenário: Sessão expirada de vez
  Dado que estou autenticado
  Quando a renovação responde 401 INVALID_REFRESH_TOKEN
  Então volto a "/login" com a mensagem de sessão expirada
  E, após entrar, sou levado à rota em que estava

Cenário: Falha de rede ao restaurar sessão
  Quando abro o app e a API está indisponível
  Então vejo uma tela de erro com "Tentar novamente"
  E não sou enviado ao login nem perco a sessão

Cenário: Validação no cliente
  Quando envio o formulário vazio ou com e-mail inválido
  Então nenhuma requisição é feita
  E as mensagens de erro aparecem associadas aos campos
  E o foco vai para o primeiro campo inválido

Cenário: Credenciais inválidas
  Quando a API responde 401 INVALID_CREDENTIALS
  Então vejo "E-mail ou senha inválidos." em um alerta anunciado por leitores de tela
  E o e-mail permanece preenchido e a senha é limpa

Cenário: Conta bloqueada
  Quando a API responde 429 com Retry-After
  Então vejo o tempo de espera estimado e não consigo reenviar antes do fim da contagem

Cenário: Rota protegida sem sessão
  Dado que não estou autenticado
  Quando acesso "/" diretamente
  Então sou redirecionado para "/login"
  E após entrar sou levado de volta a "/"

Cenário: Sair
  Dado que estou autenticado
  Quando clico em "Sair"
  Então a API de logout é chamada e sou levado a "/login"
  E, ao recarregar a página, continuo sem sessão

Cenário: Acessibilidade
  Quando uso apenas o teclado (Tab, Shift+Tab, Enter)
  Então consigo preencher e enviar o formulário com foco sempre visível
  E a tela não tem violações no axe DevTools ou no Lighthouse
  E o contraste de todos os textos atende ao WCAG 2.2 AA

Cenário: Responsividade
  Quando a largura da tela é 360 px
  Então o formulário é totalmente utilizável sem rolagem horizontal
```

## Requisitos não funcionais com destaque neste requisito

RNF-020, RNF-060, RNF-061, RNF-062, RNF-103, RNF-FE-07 a RNF-FE-15, RNF-FE-20 a RNF-FE-26 e RNF-FE-30.

## Testes previstos

Implementados na fase de testes (RNF-016). Até lá, os critérios de aceite são verificados manualmente.

- **Componente/integração (Vitest + RTL + MSW):** formulário (validações, estados de carregamento e de erro por tipo de resposta), `LoginPage` completa, `AuthProvider` (restauração de sessão com sucesso, `401` e falha de rede), `RequireAuth`, redirecionamento com `from`, logout.
- **Cliente HTTP (unitário):** injeção de cabeçalhos, *timeout*, conversão de `problem+json` em `ApiError`, **renovação única e repetição da requisição**, **agrupamento (single-flight) de renovações simultâneas** e encerramento de sessão quando a renovação falha. Os handlers MSW devem simular a rotação do refresh token.
- **Unitários:** *schemas* Zod e cálculo do tempo de espera a partir de `Retry-After`.
- **Acessibilidade:** `vitest-axe` na página de login e na página inicial.
- **Opcional recomendado:** um teste de ponta a ponta com Playwright (login real contra o backend em ambiente local, incluindo recarregar a página).

## Pontos de atenção ao revisar código gerado por IA

- Token de acesso ou de refresh salvo em `localStorage`/`sessionStorage` ou em cookie acessível por JavaScript (a IA sugere isso com muita frequência). Aqui o refresh é exclusivamente cookie `HttpOnly`, controlado pelo servidor.
- Apontar a URL base direto para o backend (outra origem) ou omitir o envio do cookie: a sessão "não persiste" e a IA tenta "consertar" guardando token no navegador, ou enfraquecendo o `SameSite` do cookie.
- **Laço infinito de renovação:** a chamada a `/auth/refresh` receber `401` e disparar outra renovação. A rota de refresh nunca deve entrar no fluxo de "renovar e repetir".
- Renovações paralelas sem *single-flight*, o que derruba a sessão por reuso do token rotativo; renovação disparada duas vezes pelo modo estrito do React em desenvolvimento (`StrictMode`) sem controle adequado.
- Tratar falha de rede na restauração de sessão como "sessão inválida" e enviar o usuário ao login.
- Renderizar telas protegidas ou o formulário de login antes do fim da restauração de sessão ("flash" de conteúdo).
- Mensagens distintas para "usuário não existe" e "senha incorreta", ou exibição do `detail` bruto do servidor.
- Validação feita só com `required` do HTML, ou apenas no cliente confiando nela como segurança.
- Chamada `fetch` solta dentro do componente, sem passar pelo cliente HTTP, e `useEffect` usado para buscar dados.
- CSS próprio ou cores e fontes escritas direto nos componentes, fora do tema do Bootstrap, outro framework de CSS, ou JavaScript do Bootstrap manipulando o DOM do React; ou cor de texto que não atende ao contraste (ver seção 2.2), `outline: none` sem substituto, `navbar-dark`/`text-white` sobre o Laranja.
- Rótulos substituídos por *placeholder*, falta de `aria-live`/`role="alert"`, foco perdido após erro.
- Redirecionamento aberto: `from` aceitando URLs externas. Só caminhos internos começando com `/` (e não `//`) são permitidos.
- Pacotes inexistentes, desatualizados ou com API antiga (ex.: versões anteriores do React Router) sugeridos pela IA.
- Testes que usam `data-testid` para tudo, *mocks* manuais de `fetch` e `waitFor` arbitrários sem asserção significativa.

## Extensões opcionais (desafios)

1. **Sincronização entre abas:** ao sair ou expirar em uma aba, as demais também encerram a sessão (`BroadcastChannel`), e a renovação é coordenada entre abas para não gerar reuso de token.
2. Botão "mostrar/ocultar senha" acessível (com `aria-pressed` e rótulo dinâmico).
3. Alternância de tema claro/escuro derivada do tema do Bootstrap (`data-bs-theme`), mantendo AA.
4. Teste de ponta a ponta com Playwright e relatório de acessibilidade automatizado.
