# RF-BE-001 — Autenticação de usuário (login)

> **Branch:** `feat/rf-be-001-login`  
> **Serviço:** Núcleo (`meunegocio360-core`), emissor de identidade (RNF-094).  
> **Consumido por:** [RF-FE-001](../frontend/RF-FE-001.md) e por todos os requisitos seguintes (que passam a exigir autenticação).  
> **Evolução:** os perfis `ADMIN` e `USUARIO` definidos aqui são substituídos em [RF-BE-002](./RF-BE-002.md) por `ADMIN`, `OPERADOR_COMERCIAL` e `OPERADOR_FINANCEIRO`.

## História de usuário

Como **usuário cadastrado do sistema**, quero **entrar com meu e-mail e minha senha e continuar autenticado sem precisar entrar de novo a cada recarregamento da página**, para **acessar as funcionalidades protegidas do MeuNegocio360 de forma segura e prática**.

## Escopo

**Dentro do escopo (obrigatório):** entidade e tabela de usuário e de refresh token, *seed* de desenvolvimento, `POST /api/v1/auth/login`, `POST /api/v1/auth/refresh`, `POST /api/v1/auth/logout`, `GET /api/v1/auth/me`, emissão e validação de JWT de acesso, refresh token rotativo com detecção de reuso, proteção *deny-by-default* dos demais endpoints, bloqueio temporário por tentativas falhas, tratamento padronizado de erros e testes.

**Fora do escopo deste requisito:** cadastro/edição de usuários (CRUD), recuperação e troca de senha, MFA, login social, autorização granular por perfil (somente o papel é gravado no token), limitação de requisições por IP, validação dos tokens por outros serviços (RS256/JWKS).

## Dependências introduzidas

O `pom.xml` gerado já contém a **base**: web MVC, Spring Data JPA, Spring Security, validação, springdoc, DevTools, driver MySQL e os *starters* de teste. Este requisito introduz (RNF-015):

| Dependência | Para quê |
| --- | --- |
| Flyway (`flyway-core` + `flyway-mysql`) | Migrations de banco (RNF-BE-20) |
| `spring-boot-starter-oauth2-resource-server` | Validação de JWT (Nimbus) no `SecurityFilterChain` e emissão com `JwtEncoder` |
| Spring Boot Actuator | *Health check* (RNF-051, RNF-104) |
| Spotless | Formatação (RNF-082) |

Dependências de teste (Testcontainers, JaCoCo, ArchUnit) entram na fase de testes.

## Regras de negócio

| ID | Regra |
| --- | --- |
| RN-01 | A identificação é por **e-mail** (único, comparado sem diferenciar maiúsculas/minúsculas e armazenado em minúsculas, com *trim*) e **senha**. |
| RN-02 | Somente usuários **ativos** podem autenticar e renovar sessão. |
| RN-03 | Credenciais inválidas (e-mail inexistente, senha incorreta ou usuário inativo) retornam **a mesma resposta genérica** `401`, para impedir enumeração de usuários. O tempo de resposta também DEVE ser equivalente: para e-mail inexistente, executar uma verificação de hash fictícia. |
| RN-04 | Após **5 falhas consecutivas** para o mesmo usuário, a conta fica **bloqueada por 15 minutos** (`429`, com `Retry-After`). Um login bem-sucedido zera o contador. Os valores DEVEM ser configuráveis (`app.auth.max-tentativas`, `app.auth.bloqueio-minutos`). |
| RN-05 | O **token de acesso** é um JWT assinado, com validade padrão de **15 minutos** (`app.auth.jwt.expiracao-minutos`). Ele é devolvido no corpo da resposta e **nunca** em cookie. |
| RN-06 | O **refresh token** é um valor opaco, aleatório e imprevisível (mínimo de 256 bits, gerado com `SecureRandom`, codificado em Base64 URL-safe), com validade padrão de **7 dias** (`app.auth.refresh.expiracao-dias`). Ele é entregue **somente** em cookie `HttpOnly` e **nunca** aparece no corpo da resposta. |
| RN-07 | O refresh token é **rotativo**: cada uso válido de `/auth/refresh` revoga o token apresentado e emite um novo, na mesma *família* (`familia_id`), junto com um novo token de acesso. |
| RN-08 | **Detecção de reuso:** se um refresh token já revogado for apresentado, toda a família é revogada e a resposta é `401`, pois o token pode ter sido roubado. |
| RN-09 | No banco, apenas o **hash SHA-256** do refresh token é armazenado, nunca o valor em claro. |
| RN-10 | `/auth/logout` revoga a família do refresh token apresentado e limpa o cookie. É **idempotente**: sempre `204`, mesmo sem cookie ou com token inválido. |
| RN-11 | Senha no login: mínimo de 8 e máximo de 72 caracteres (limite do BCrypt). A política de complexidade ao *criar* senha pertence ao futuro requisito de cadastro. |
| RN-12 | O último login bem-sucedido (`ultimo_login_em`) é registrado. A renovação por refresh NÃO altera esse campo. |
| RN-13 | Desativar um usuário ou bloquear sua conta revoga as suas famílias de refresh token ativas e impede novas renovações. |

## Contrato da API

Todos os caminhos abaixo são relativos a `/api/v1`.

### `POST /auth/login` (público)

Requisição:

```json
{ "email": "admin@meunegocio360.com.br", "senha": "********" }
```

Resposta `200 OK`, com o cookie de refresh em `Set-Cookie`:

```json
{
  "tokenType": "Bearer",
  "accessToken": "eyJhbGciOi...",
  "expiresIn": 900,
  "usuario": {
    "id": "0b6c6a8e-3a1f-4a4b-9a64-2d0f1c3f6b11",
    "nome": "Administrador",
    "email": "admin@meunegocio360.com.br",
    "perfil": "ADMIN"
  }
}
```

### `POST /auth/refresh` (público, exige o cookie de refresh)

Sem corpo. O navegador envia o cookie automaticamente. Resposta `200 OK`: **mesmo formato da resposta do login** (inclui `usuario`, o que dispensa uma chamada a `/me` ao restaurar a sessão) e um **novo** cookie de refresh em `Set-Cookie`.

### `POST /auth/logout` (público, usa o cookie de refresh)

Sem corpo. Resposta `204 No Content`, com `Set-Cookie` que expira o cookie imediatamente.

### `GET /auth/me` (autenticado)

Cabeçalho: `Authorization: Bearer <accessToken>`. Resposta `200 OK`: o objeto `usuario` acima.

### Cookie de refresh

| Atributo | Valor |
| --- | --- |
| Nome | `mn360_refresh` |
| `HttpOnly` | Sempre |
| `Secure` | Sim; configurável por `app.auth.cookie.secure`, podendo ser `false` apenas no perfil `dev` quando o ambiente local usar HTTP. |
| `SameSite` | `Strict` |
| `Path` | `/api/v1/auth` (o navegador só o envia para os endpoints de autenticação) |
| `Max-Age` | Igual à validade do refresh token |

Os endpoints `/auth/refresh` e `/auth/logout` DEVEM rejeitar com `403` (`code: "ORIGIN_NOT_ALLOWED"`) requisições cujo cabeçalho `Origin` esteja presente e fora da lista de `CORS_ALLOWED_ORIGINS` (RNF-BE-40).

### Erros (todos em `application/problem+json`)

| Status | `code` | Endpoint | Quando |
| --- | --- | --- | --- |
| 400 | `VALIDATION_ERROR` | login | JSON malformado, e-mail inválido, campo ausente ou fora dos limites (com a lista `errors`). |
| 401 | `INVALID_CREDENTIALS` | login | Credenciais inválidas ou usuário inativo (mensagem genérica: "E-mail ou senha inválidos."). |
| 401 | `INVALID_REFRESH_TOKEN` | refresh | Cookie ausente, desconhecido, expirado, revogado (reuso), ou usuário inativo/bloqueado. O cookie é limpo na resposta. |
| 401 | `UNAUTHORIZED` | me e demais protegidos | Token de acesso ausente, inválido ou expirado; inclui `WWW-Authenticate: Bearer`. |
| 403 | `ORIGIN_NOT_ALLOWED` | refresh, logout | `Origin` não permitido. |
| 429 | `ACCOUNT_TEMPORARILY_LOCKED` | login | Bloqueio por tentativas excedidas; inclui `Retry-After` em segundos. |
| 500 | `INTERNAL_ERROR` | todos | Erro inesperado, sem detalhes internos. |

## Token de acesso (JWT)

| Item | Definição |
| --- | --- |
| Algoritmo | HS256, com segredo de pelo menos 256 bits vindo de `JWT_SECRET` (a migração para RS256 + JWKS está em RNF-094). |
| Claims | `iss` (`meunegocio360-core`), `sub` (id do usuário), `iat`, `exp`, `jti` (UUID) e `perfil`. |
| Validação | Assinatura, algoritmo esperado, `iss` e `exp`. Token com `alg: none` ou outro algoritmo DEVE ser rejeitado. |
| Conteúdo | NÃO DEVE conter senha, *hash*, nem dados pessoais além do necessário. |

## Modelo de dados

Migration `V1__criar_tabela_usuario.sql` (nomes ilustrativos):

| Coluna | Tipo | Restrições |
| --- | --- | --- |
| `id` | `BINARY(16)` (UUID) | PK |
| `nome` | `VARCHAR(120)` | `NOT NULL` |
| `email` | `VARCHAR(160)` | `NOT NULL`, `UNIQUE` |
| `senha_hash` | `VARCHAR(100)` | `NOT NULL` (BCrypt, formato `{bcrypt}...`) |
| `perfil` | `VARCHAR(20)` | `NOT NULL` (`ADMIN`, `USUARIO`) |
| `ativo` | `BOOLEAN` | `NOT NULL`, padrão `TRUE` |
| `tentativas_falhas` | `INT` | `NOT NULL`, padrão `0` |
| `bloqueado_ate` | `DATETIME(6)` | nulo (UTC) |
| `ultimo_login_em` | `DATETIME(6)` | nulo (UTC) |
| `criado_em`, `atualizado_em` | `DATETIME(6)` | `NOT NULL` (UTC) |
| `versao` | `BIGINT` | `NOT NULL` (controle otimista) |

Migration `V2__criar_tabela_refresh_token.sql`:

| Coluna | Tipo | Restrições |
| --- | --- | --- |
| `id` | `BINARY(16)` (UUID) | PK |
| `usuario_id` | `BINARY(16)` | `NOT NULL`, FK para `usuario`, indexada |
| `familia_id` | `BINARY(16)` | `NOT NULL`, indexada (agrupa a cadeia de rotações de uma sessão) |
| `token_hash` | `CHAR(64)` | `NOT NULL`, `UNIQUE` (SHA-256 em hexadecimal) |
| `expira_em` | `DATETIME(6)` | `NOT NULL` (UTC) |
| `revogado_em` | `DATETIME(6)` | nulo (UTC); preenchido na rotação, no logout ou na revogação da família |
| `criado_em` | `DATETIME(6)` | `NOT NULL` (UTC) |

Tokens expirados ou revogados há mais de 30 dias DEVEM ser removidos por uma rotina agendada (`@Scheduled`). Não registrar IP nem *user-agent* (minimização de dados, RNF-025).

**Seed de desenvolvimento (somente perfil `dev`):** um usuário `ADMIN` e um `USUARIO`, com e-mails fictícios e senhas lidas de variáveis de ambiente (`SEED_ADMIN_SENHA`, `SEED_USUARIO_SENHA`). Senhas de seed NÃO DEVEM ser gravadas em texto no repositório, nem existir em `prod`.

## Critérios de aceite

```gherkin
Cenário: Login com credenciais válidas
  Dado um usuário ativo com e-mail "admin@meunegocio360.com.br" e senha válida
  Quando envia POST /api/v1/auth/login com essas credenciais
  Então recebe 200 com accessToken JWT, tokenType "Bearer" e expiresIn 900
  E o token contém sub, perfil, iss, iat, exp e jti
  E recebe o cookie mn360_refresh com HttpOnly, SameSite=Strict e Path=/api/v1/auth
  E o refresh token não aparece no corpo da resposta
  E o banco guarda apenas o hash do refresh token
  E o contador de tentativas falhas é zerado e ultimo_login_em é atualizado

Cenário: Senha incorreta
  Dado um usuário ativo
  Quando envia a senha errada
  Então recebe 401 com code "INVALID_CREDENTIALS" e a mensagem genérica
  E o contador de tentativas falhas é incrementado
  E nenhum cookie é emitido

Cenário: E-mail inexistente
  Quando envia um e-mail que não existe
  Então recebe exatamente a mesma resposta 401 do cenário de senha incorreta

Cenário: Usuário inativo
  Dado um usuário com ativo = false e senha correta
  Quando tenta autenticar
  Então recebe a mesma resposta 401 genérica

Cenário: Bloqueio por tentativas excessivas
  Dado um usuário com 4 falhas consecutivas
  Quando erra a senha pela quinta vez
  Então a conta é bloqueada por 15 minutos
  E qualquer tentativa seguinte, mesmo com a senha correta, recebe 429 "ACCOUNT_TEMPORARILY_LOCKED" com Retry-After
  E, passado o período de bloqueio, o login com a senha correta volta a funcionar

Cenário: Entrada inválida
  Quando envia e-mail malformado, senha vazia ou corpo ausente
  Então recebe 400 "VALIDATION_ERROR" com a lista de campos inválidos

Cenário: Renovação de sessão
  Dado que fiz login e tenho o cookie de refresh
  Quando envio POST /api/v1/auth/refresh
  Então recebo 200 com novo accessToken, o objeto usuario e um novo cookie
  E o refresh token anterior fica revogado e o novo pertence à mesma família

Cenário: Refresh sem cookie, expirado ou desconhecido
  Quando envio POST /api/v1/auth/refresh sem cookie, com token expirado ou inexistente
  Então recebo 401 "INVALID_REFRESH_TOKEN" e o cookie é limpo

Cenário: Detecção de reuso de refresh token
  Dado um refresh token que já foi rotacionado (revogado)
  Quando ele é apresentado novamente
  Então recebo 401 "INVALID_REFRESH_TOKEN"
  E toda a família é revogada, inclusive o token mais recente

Cenário: Usuário desativado durante a sessão
  Dado um usuário com sessão ativa
  Quando ele é desativado
  Então POST /auth/refresh retorna 401 "INVALID_REFRESH_TOKEN"

Cenário: Logout
  Dado que tenho uma sessão ativa
  Quando envio POST /api/v1/auth/logout com o cookie
  Então recebo 204 e o cookie é expirado
  E o refresh token não pode mais ser usado
  E chamar logout novamente, ou sem cookie, também retorna 204

Cenário: Origem não permitida
  Quando POST /auth/refresh ou /auth/logout chega com Origin fora da lista permitida
  Então recebo 403 "ORIGIN_NOT_ALLOWED" e nenhum token é rotacionado

Cenário: Acesso a endpoint protegido
  Quando chama GET /api/v1/auth/me sem token, com token expirado, adulterado ou assinado por outra chave
  Então recebe 401 em formato problem+json
  E com um token válido recebe 200 com os dados do usuário autenticado

Cenário: Endpoints desconhecidos ou não liberados
  Quando chama qualquer outro endpoint sem token
  Então recebe 401 (deny-by-default)
```

## Requisitos não funcionais com destaque neste requisito

RNF-021, RNF-022, RNF-023, RNF-024, RNF-025, RNF-050, RNF-094, RNF-BE-14, RNF-BE-40, RNF-BE-42, RNF-BE-43 e RNF-BE-44.

## Testes previstos

Implementados na fase de testes (RNF-016). Até lá, os critérios de aceite são verificados manualmente.

- **Unitários** (sem Spring): casos de uso de login, refresh e logout (sucesso, senha errada, usuário inexistente, inativo, bloqueio e desbloqueio por tempo, rotação, expiração, reuso e revogação de família), usando um `Clock` injetável; emissão e validação do JWT (expirado, adulterado, `alg` errado).
- **Web** (`MockMvc`): contrato de todos os endpoints, atributos do cookie, formato `problem+json`, cabeçalhos `Retry-After` e `WWW-Authenticate`, verificação de `Origin`.
- **Integração** (Testcontainers MySQL + Flyway): migrations aplicam-se do zero, unicidade de e-mail e de `token_hash`, fluxo completo login → `me` → refresh → logout, reuso de token.
- **Segurança:** mesma resposta (corpo e status) para e-mail inexistente e senha incorreta; endpoint arbitrário sem token retorna 401; o valor do refresh token nunca é gravado em claro no banco nem aparece em logs.
- **Concorrência:** duas renovações simultâneas com o mesmo refresh token não podem produzir dois tokens válidos (apenas uma vence; a outra recebe 401).
- **Arquitetura:** regra de camadas (ArchUnit) para a feature `auth`.

## Pontos de atenção ao revisar código gerado por IA

Assistentes de IA costumam errar justamente aqui. Verifique, um a um:

- Mensagens ou status diferentes para "e-mail não existe" e "senha errada" (enumeração de usuários).
- Segredo JWT *hardcoded*, fraco ou com valor padrão no `application.yaml`.
- Refresh token devolvido no corpo da resposta, gravado em claro no banco, ou cookie sem `HttpOnly`/`SameSite`/`Path` restrito.
- Refresh token **sem rotação** ou sem detecção de reuso; rotação sem transação ou sem bloqueio de concorrência (dois usos simultâneos gerando dois tokens válidos).
- Cookie válido por mais tempo que o registro no banco, ou logout que apenas apaga o cookie sem revogar no servidor.
- `allowedOrigins("*")` junto com credenciais, ou verificação de `Origin` ausente em `/auth/refresh`.
- Senhas, tokens ou e-mails completos em logs; `toString()` de entidade ou DTO exibindo `senhaHash`/`tokenHash`.
- Comparação de senha feita manualmente, em vez de usar `PasswordEncoder`; uso de MD5/SHA sem *salt* para senha (SHA-256 só é adequado para o refresh token, que já é aleatório e de alta entropia).
- Entidade JPA devolvida diretamente na resposta; DTO com campos de hash.
- `permitAll()` amplos demais (por exemplo, `/**`), ou rotas do Swagger/Actuator abertas em produção.
- Validação do JWT sem checar algoritmo, emissor e expiração; uso de API ou biblioteca desatualizada ou inexistente na versão do Spring Boot em uso.
- Contador de tentativas com condição de corrida, ou uso espalhado de `LocalDateTime.now()`/`Instant.now()`, o que torna os testes frágeis.
- Testes que apenas verificam `status == 200`, sem validar o conteúdo do token, o cookie e os efeitos no banco.

## Extensões opcionais (desafios)

1. Tolerância curta (por exemplo, 10 s) para renovações simultâneas legítimas feitas por abas diferentes, sem abrir mão da detecção de reuso.
2. Listagem e revogação de sessões ativas do usuário.
3. Limite de requisições por IP (por exemplo, Bucket4j) em `/auth/login`.
4. Auditoria de eventos de autenticação em tabela própria.
5. Troca do algoritmo para RS256 com publicação de JWKS (`/.well-known/jwks.json`).
