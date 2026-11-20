# MeuNegocio360 — Requisitos Gerais

| | |
| --- | --- |
| **Versão** | 3.0 |
| **Atualizado em** | 18/11/2026 |
| **Aplica-se a** | Todos os serviços de backend (`meunegocio360-*`) e ao frontend (`meunegocio360-js`) |
| **Requisitos por camada** | [`backend/`](./backend) · [`frontend/`](./frontend) |
## 1. Como ler este documento

Este documento reúne os **requisitos não funcionais (RNF) válidos para todo o projeto** e o **índice dos requisitos funcionais (RF)**. Os requisitos de cada camada ficam em [`backend/`](./backend) e [`frontend/`](./frontend): cada pasta tem um arquivo de requisitos não funcionais da camada ([`RNF-BE.md`](./backend/RNF-BE.md), [`RNF-FE.md`](./frontend/RNF-FE.md)) e **um arquivo por requisito funcional** (`RF-BE-NNN.md`, `RF-FE-NNN.md`). Todos herdam o que está aqui e acrescentam apenas o que é específico da camada.

Visão do produto, arquitetura, stack, estrutura do repositório, comandos, fluxo de contribuição e implantação estão no [`README.md`](../../README.md) e não são repetidos aqui.

Convenções de leitura:

- As palavras **DEVE**, **NÃO DEVE**, **DEVERIA** e **PODE** seguem o sentido da RFC 2119: *DEVE* é obrigatório, *DEVERIA* é fortemente recomendado (desvios exigem justificativa no *Pull Request* - PR) e *PODE* é opcional.
- Cada requisito tem um identificador estável e rastreável: `RNF-xxx` (não funcionais, válidos em todo o projeto), `RNF-BE-xx` e `RNF-FE-xx` (não funcionais de cada camada), `RF-BE-xxx` (funcional de backend) e `RF-FE-xxx` (funcional de frontend). Commits, PRs e documentação **DEVEM** citar o ID do requisito atendido.
- Os arquivos `pom.xml` e `package.json` são a fonte da verdade para versões de bibliotecas.

## 2. Requisitos funcionais

| Funcionalidade | ID | Camada | Título | Arquivo |
| --- | --- | --- | --- | --- |
| F01 | RF-BE-001 | Backend (Núcleo) | Autenticação de usuário (login) e sessão com refresh token | [`RF-BE-001.md`](./backend/RF-BE-001.md) |
| F01 | RF-FE-001 | Frontend | Fluxo de login do usuário, com persistência de sessão | [`RF-FE-001.md`](./frontend/RF-FE-001.md) |
| F02 | RF-BE-002 | Backend (Núcleo) | Cadastro de empresa, parâmetros básicos e perfis de acesso | [`RF-BE-002.md`](./backend/RF-BE-002.md) |
| F02 | RF-FE-002 | Frontend | Cadastro de empresa, layout da aplicação e guardas de acesso | [`RF-FE-002.md`](./frontend/RF-FE-002.md) |
| F03 | RF-BE-003 | Backend (Núcleo) | Cadastro de clientes | [`RF-BE-003.md`](./backend/RF-BE-003.md) |
| F03 | RF-FE-003 | Frontend | Cadastro e busca de clientes | [`RF-FE-003.md`](./frontend/RF-FE-003.md) |
| F04 | RF-BE-004 | Backend (Núcleo) | Cadastro de fornecedores | [`RF-BE-004.md`](./backend/RF-BE-004.md) |
| F04 | RF-FE-004 | Frontend | Cadastro e busca de fornecedores | [`RF-FE-004.md`](./frontend/RF-FE-004.md) |
| F05 | RF-BE-005 | Backend (Núcleo) | Catálogo de produtos e serviços | [`RF-BE-005.md`](./backend/RF-BE-005.md) |
| F05 | RF-FE-005 | Frontend | Catálogo de produtos e serviços | [`RF-FE-005.md`](./frontend/RF-FE-005.md) |

Novos requisitos DEVEM ser acrescentados a esta tabela e criados como um arquivo na pasta de sua camada.

### 2.1 Escopo de um requisito funcional

Cada RF é entregue de ponta a ponta (especificação, implementação, verificação dos critérios de aceite) e DEVE explicitar o que está **dentro** e **fora** de seu escopo, as dependências que introduz (RNF-015) e as regras de negócio aplicáveis. Cada RF é desenvolvido em **uma branch própria** (seção 2.3).

### 2.2 Template de requisito

Todo novo requisito DEVE usar esta estrutura, para que a IA e as pessoas recebam a mesma informação:

```markdown
# RF-<BE|FE>-NNN — <Título curto>

**Branch:** `feat/rf-<be|fe>-NNN-<resumo>`

**História de usuário:** Como <perfil>, quero <ação>, para <benefício>.

**Atores e precondições:** <perfis autorizados e o que já deve existir>

**Contexto e dependências:** <o que já existe; requisitos dos quais depende>

**Escopo:** <dentro do escopo / fora do escopo>

**Regras de negócio:** RN-01 …, RN-02 …

**Contrato / Interface:** <endpoints e payloads (BE) ou telas e fluxos (FE)>

**Modelo de dados / Estado:** <tabelas e migrations (BE) ou estado e rotas (FE)>

**Critérios de aceite (Gherkin):**
Cenário: …
  Dado …
  Quando …
  Então …

**Dependências introduzidas:** <bibliotecas e ferramentas que este requisito traz>

**Requisitos não funcionais com destaque:** RNF-0xx, RNF-0yy

**Testes previstos:** <lista objetiva, implementada na fase de testes>

**Pontos de atenção ao revisar código gerado por IA:** <armadilhas conhecidas>

**Extensões opcionais:** <desafios que NÃO fazem parte do requisito>
```

### 2.3 Branch por requisito

Cada RF é desenvolvido em uma branch própria, criada a partir da `main` atualizada do fork e nomeada `<tipo>/<id-do-requisito>-<resumo>` em minúsculas, conforme as [convenções do README](../../README.md#63-convenções). A branch do requisito é registrada no campo **Branch** de cada arquivo (ex.: `feat/rf-be-003-clientes`) e um PR NÃO DEVE misturar mais de um RF.

## 3. Requisitos não funcionais (válidos para todo o projeto)

Os requisitos desta seção são **aplicados progressivamente**: valem desde o início, exceto os que dependem de ferramentas ainda não introduzidas (RNF-016).

### 3.1 Arquitetura e qualidade de código

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-010 | O código DEVE seguir arquitetura em camadas com **regra de dependência única**: `api → application → domain`, e `infra` implementa interfaces do domínio. Nenhuma camada interna conhece uma externa. | Revisão de PR (e teste de arquitetura, na fase de testes). |
| RNF-011 | Cada funcionalidade DEVE ser agrupada em seu próprio pacote/pasta (*package by feature*), e não por tipo técnico global. | Revisão de PR. |
| RNF-012 | Código DEVE seguir princípios SOLID e *Clean Code*: nomes em português de domínio ou inglês técnico, de forma consistente; métodos curtos; sem duplicação relevante; sem código comentado. | Linters e revisão. |
| RNF-013 | Todo código gerado por IA DEVE ser **lido, entendido e revisado** por quem o submete. O autor do PR responde pelo código, independentemente de quem o escreveu. | Checklist do PR. |
| RNF-014 | Dependências novas DEVEM ser justificadas no PR (necessidade, licença, manutenção ativa). A IA NÃO DEVE ser aceita como única justificativa. | Revisão de PR. |

### 3.2 Microsserviços

| ID | Requisito |
| --- | --- |
| RNF-090 | **Um banco de dados por serviço** (*database per service*). Nenhum serviço lê ou escreve nas tabelas de outro, nem existem chaves estrangeiras entre bancos de serviços diferentes. Referências a dados de outro serviço são feitas por identificador (UUID) e consultadas por API. |
| RNF-091 | A comunicação entre serviços é por **contrato versionado**: REST síncrono (OpenAPI) ou eventos assíncronos. A escolha de *broker* e do formato de eventos será registrada em ADR quando surgir a primeira necessidade. |
| RNF-092 | Chamadas a outro serviço DEVEM ter *timeout*, tratamento de falha (e, quando fizer sentido, *retry* com *backoff* e *circuit breaker*) e propagar `X-Request-Id`. Um serviço NÃO DEVE ficar indisponível só porque outro está. |
| RNF-093 | Cada serviço DEVE ser construído, testado e implantado de forma independente (configuração e variáveis próprias) e NÃO DEVE compartilhar código de negócio com outro. Bibliotecas compartilhadas se limitam a contratos (DTOs/OpenAPI) e utilitários técnicos sem regra de negócio. |
| RNF-094 | O Núcleo é o único serviço que autentica usuários e emite tokens. Os demais serviços apenas **validam** o token de acesso (assinatura via JWKS, emissor, expiração e `perfil`) e nunca recebem nem armazenam senhas. Antes de o primeiro serviço adicional validar tokens, a assinatura DEVE migrar de segredo compartilhado (HS256) para chaves assimétricas (RS256) com publicação de JWKS. |
| RNF-095 | Cada serviço DEVE publicar o próprio OpenAPI, *health check* e logs com o mesmo formato e identificador de correlação (RNF-030, RNF-050, RNF-051). |

### 3.3 Dependências

| ID | Requisito |
| --- | --- |
| RNF-015 | Bibliotecas e ferramentas são acrescentadas ao `pom.xml` e ao `package.json` **no requisito que as exige**, e não antecipadamente. O projeto parte da base mínima gerada (Spring Initializr e Vite). Cada requisito lista, em "Dependências introduzidas", o que traz consigo. |
| RNF-016 | Requisitos não funcionais que dependem de uma ferramenta ainda não introduzida (por exemplo, testes automatizados, cobertura ou verificação automática de acessibilidade) passam a ser exigíveis **a partir do requisito ou da fase que introduz a ferramenta**. Os demais valem desde o início. |

### 3.4 Segurança e privacidade

Referência: **OWASP ASVS nível 1** e **OWASP Top 10**.

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-005 | O repositório NÃO DEVE conter segredos, credenciais, dumps de dados reais, `target/` nem `node_modules/`. O `.gitignore` DEVE cobrir esses itens e todo arquivo `.env*`, exceto `.env.example`. | Revisão do *diff*. |
| RNF-020 | Toda entrada externa DEVE ser validada no servidor (tipo, tamanho, formato). Validação no frontend é apenas experiência de uso. | Verificação dos critérios de aceite. |
| RNF-021 | Endpoints DEVEM ser **negados por padrão**; só são públicos os explicitamente liberados. | Verificação dos critérios de aceite. |
| RNF-022 | Segredos (chaves JWT, senhas de banco, tokens) DEVEM vir de variáveis de ambiente ou dos painéis do provedor de implantação. NUNCA do código, de arquivos versionados ou de prompts enviados à IA. | Revisão do *diff*. |
| RNF-023 | Senhas DEVEM ser armazenadas apenas como *hash* adaptativo com *salt* (BCrypt via `DelegatingPasswordEncoder`). NÃO DEVEM aparecer em logs, respostas, exceções ou testes versionados com valores reais. | Revisão. |
| RNF-024 | Mensagens de erro NÃO DEVEM vazar detalhes internos (stack trace, SQL, nomes de classe) nem permitir enumeração de usuários. | Verificação dos critérios de aceite. |
| RNF-025 | Dados pessoais seguem a **LGPD**: coletar o mínimo necessário, não registrar dados pessoais em logs e permitir, no futuro, exclusão/anonimização. | Revisão de modelo de dados e logs. |
| RNF-026 | Dependências DEVEM ser auditadas antes de cada PR (`npm audit` no frontend; no backend, atenção aos avisos de segurança das versões escolhidas). Vulnerabilidades críticas conhecidas bloqueiam o merge. | Revisão de PR. |

### 3.5 Contrato de API e integração

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-030 | Toda API DEVE ser documentada em OpenAPI (springdoc), com exemplos de sucesso e de erro, e disponível em `/swagger-ui.html` fora de produção. | Revisão; abertura da UI. |
| RNF-032 | Mudanças incompatíveis no contrato DEVEM gerar nova versão de caminho (`/api/v2`). Mudanças compatíveis (campos opcionais novos) não. | Revisão. |
| RNF-033 | Datas e horas trafegam em **ISO 8601 UTC** (ex.: `2026-11-11T14:30:00Z`); valores monetários como inteiros em centavos ou decimais em *string*, nunca `float`. | Revisão; testes de serialização. |

O formato de erros, a paginação e demais convenções REST estão em [`RNF-BE.md`](./backend/RNF-BE.md#2-convenções-da-api-rest).

### 3.6 Testes e qualidade contínua

Os requisitos de testes automatizados são exigíveis a partir da **fase de testes** do projeto (RNF-016; ver [Contribuições](../../README.md#6-contribuições)). Até lá, os critérios de aceite de cada RF são verificados manualmente (Swagger UI e navegador) e o resultado é registrado no PR.

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-040 | Todo requisito DEVE ter testes automatizados que cubram seus critérios de aceite. Teste que "só passa" sem validar a regra de negócio NÃO conta. | Revisão de PR. |
| RNF-041 | Seguir a **pirâmide de testes**: muitos unitários, alguns de integração (com banco real no backend e API simulada no frontend), poucos de ponta a ponta. | Estrutura de testes. |
| RNF-042 | Cobertura de linhas do **código novo** DEVERIA ser ≥ 80%. A cobertura é indicador, não objetivo: a qualidade das asserções prevalece. | Relatórios de cobertura. |
| RNF-043 | Testes DEVEM ser determinísticos, independentes entre si e executáveis offline (sem depender de serviços externos reais). | Execução local. |

### 3.7 Observabilidade, operação e desempenho

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-050 | Logs DEVERIAM ser estruturados (JSON) fora do ambiente de desenvolvimento, com nível adequado e **identificador de correlação** (`X-Request-Id`) propagado entre frontend e backend. | Inspeção de logs. |
| RNF-051 | O backend DEVE expor *health checks* (Actuator `/actuator/health`) sem detalhes sensíveis. | Verificação manual. |
| RNF-052 | A configuração DEVE seguir *12-factor*: mesmo artefato para todos os ambientes, diferenças apenas por variáveis de ambiente e perfis (`dev`, `test`, `prod`; arquivos `application.yaml` e `application-{perfil}.yaml`). | Revisão. |
| RNF-053 | Endpoints de leitura DEVERIAM responder em p95 ≤ 500 ms e o login em p95 ≤ 800 ms (excluindo o custo intencional do hash de senha) em ambiente local com dados de teste. | Medição manual. |
| RNF-054 | Listagens DEVEM ser **paginadas** no servidor; nunca retornar coleções ilimitadas. | Verificação dos critérios de aceite. |

### 3.8 Usabilidade e acessibilidade

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-060 | A interface DEVE atender **WCAG 2.2 nível AA**: contraste, foco visível, navegação por teclado, rótulos e mensagens associados a campos, alvos de toque adequados. | Auditoria com axe DevTools ou Lighthouse e roteiro manual (automatizada na fase de testes). |
| RNF-061 | A interface DEVE ser responsiva (a partir de 360 px de largura, sem rolagem horizontal) e em **português do Brasil**. | Revisão visual. |
| RNF-062 | Toda tela com dados remotos DEVE tratar explicitamente os estados de **carregamento, vazio, erro e sucesso**. | Verificação dos critérios de aceite. |

### 3.9 Documentação

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-070 | Decisões arquiteturais relevantes DEVEM ser registradas como ADR curto em `docs/adr/NNNN-titulo.md` (contexto, decisão, consequências). | Revisão de PR. |
| RNF-071 | Cada requisito entregue DEVE atualizar o README e o OpenAPI quando alterar como rodar ou como consumir o sistema. | Revisão de PR. |
| RNF-072 | O `README.md` DEVE permitir que um novo desenvolvedor suba o ambiente completo (banco, backend e frontend) em menos de 15 minutos. | Execução do passo a passo. |

### 3.10 Ambiente de desenvolvimento

| ID | Requisito |
| --- | --- |
| RNF-080 | IDE padrão: VS Code, com extensões de linguagem (Java e ESLint) e de assistente de IA. |
| RNF-081 | O ambiente local DEVE subir com `docker compose up -d` (MySQL do Núcleo; cada novo serviço acrescenta seu próprio banco) e variáveis definidas a partir do `.env.example`. O arquivo `.env` NUNCA é versionado. |
| RNF-082 | Formatação automática obrigatória: Spotless (google-java-format) no backend e Prettier + ESLint no frontend (ferramentas introduzidas por `RF-BE-001` e `RF-FE-001`, conforme RNF-015). |
| RNF-083 | O arquivo `AGENTS.md` na raiz DEVE orientar os assistentes de IA sobre como trabalhar no projeto e apontar para o README e para estes requisitos, sem repeti-los. |

### 3.11 Implantação

Frontend no Netlify e backend no Render (passo a passo no [`README.md`](../../README.md#7-implantação)).

| ID | Requisito |
| --- | --- |
| RNF-100 | Cada serviço de backend DEVE ter um `Dockerfile` *multi-stage* na própria pasta, que gera a imagem executável sem depender de arquivos fora dela (o Render executa Java via Docker). |
| RNF-101 | A aplicação DEVE escutar na porta informada pela variável `PORT` (padrão 8080 em desenvolvimento). |
| RNF-102 | Toda configuração de produção DEVE vir de variáveis de ambiente configuradas nos painéis do Netlify e do Render (RNF-022, RNF-052). Valores de `dev` (como o *seed*) NUNCA são habilitados em produção. |
| RNF-103 | O frontend DEVE ser um build estático (`npm run build`, saída `dist/`) servido com *fallback* para as rotas da SPA e DEVE chamar a API **sempre por caminho relativo** (`/api/...`, mesma origem do frontend): em desenvolvimento, pelo `server.proxy` do Vite; em produção, por regra de *rewrite* do Netlify para o Render. O cookie de refresh NUNCA DEVE exigir `SameSite=None`. |
| RNF-104 | O backend DEVE responder a `/actuator/health` sem autenticação e sem detalhes (RNF-051), para o *health check* do Render. |
| RNF-105 | O ambiente oficial só DEVE ser implantado a partir da `main` do repositório principal. Branches de forks são implantadas apenas nas contas de seus autores. |

### 3.12 Dados mestres e contexto da empresa

| ID | Requisito |
| --- | --- |
| RNF-096 | Toda entidade de negócio DEVE pertencer a uma empresa (`empresa_id`). O valor é resolvido **no servidor**, a partir da empresa ativa ([RF-BE-002](./backend/RF-BE-002.md)), e NUNCA é informado nem aceito do cliente. Sem empresa ativa, nenhuma entidade de negócio é criada. |
| RNF-097 | Registros mestres (clientes, fornecedores, produtos, serviços) que possuem histórico NÃO DEVEM ser excluídos: são **inativados**. A exclusão só é permitida sem histórico e, quando negada, retorna erro de negócio claro e específico, nunca um erro técnico genérico. |
