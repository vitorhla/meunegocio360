# MeuNegocio360 — Requisitos Não Funcionais do Backend

| | |
| --- | --- |
| **Versão** | 3.0 |
| **Atualizado em** | 18/11/2026 |
| **Projeto** | `meunegocio360-*` (módulo por serviço) |
| **Herda de** | [`GERAIS.md`](../GERAIS.md) (todos os RNF se aplicam; aqui só há o que é específico do backend) |
| **Requisitos funcionais** | [`RF-BE-001.md`](./RF-BE-001.md) a [`RF-BE-005.md`](./RF-BE-005.md) (índice em [`GERAIS.md`](../GERAIS.md#2-requisitos-funcionais)) |
## 1. Arquitetura

As regras de arquitetura entre serviços (banco por serviço, contratos, tokens) estão em [`GERAIS.md`](../GERAIS.md#32-microsserviços). A estrutura de pacotes está no [`README.md`](../../../README.md#backend-cada-serviço). Dentro de cada serviço:

| ID | Requisito |
| --- | --- |
| RNF-BE-01 | Controllers DEVEM ser finos: recebem DTO, delegam ao caso de uso e devolvem DTO. Regra de negócio NÃO DEVE ficar em controller nem em repositório. |
| RNF-BE-02 | Entidades JPA NUNCA DEVEM ser expostas na API. Entrada e saída usam DTOs (preferencialmente `record`). |
| RNF-BE-03 | Injeção de dependências DEVE ser por **construtor**; sem `@Autowired` em campos. |
| RNF-BE-04 | Fronteiras transacionais (`@Transactional`) ficam na camada `application`. |
| RNF-BE-05 | Uma feature NÃO DEVE acessar diretamente o repositório ou as entidades internas de outra feature. A comunicação é por interface pública do caso de uso. |

## 2. Convenções da API REST

| ID | Requisito |
| --- | --- |
| RNF-BE-10 | Prefixo `/api/v1`. Recursos no plural e em *kebab-case* (`/api/v1/ordens-de-servico`). Verbos HTTP com semântica correta: `GET` (leitura), `POST` (criação/ação), `PUT`/`PATCH` (atualização), `DELETE` (remoção). |
| RNF-BE-11 | JSON em `camelCase` (datas e valores conforme RNF-033). Identificadores públicos são **UUID**, nunca sequenciais. |
| RNF-BE-12 | Códigos de status: `200` leitura/ação, `201` criação (com `Location`), `204` sem conteúdo, `400` requisição inválida, `401` não autenticado, `403` sem permissão, `404` não encontrado, `409` conflito, `422` regra de negócio violada, `429` limite excedido, `500` erro inesperado. |
| RNF-BE-13 | Paginação com parâmetros `page` (a partir de 0), `size` (padrão 20, máximo 100) e `sort`. Resposta: `{ "content": [...], "page": 0, "size": 20, "totalElements": 0, "totalPages": 0 }`. |
| RNF-BE-14 | Todos os erros usam `application/problem+json` (RFC 9457) com o formato abaixo, tratados por um único `@RestControllerAdvice`. |
| RNF-BE-15 | Erros de regra de negócio DEVEM usar um `code` estável e específico (ex.: `CUSTOMER_HAS_HISTORY`), `detail` claro em português e status `422` (ou `409` para conflito de unicidade e de versão). Violações de integridade do banco NUNCA DEVEM chegar ao cliente como erro genérico `500`: são traduzidas para o erro de negócio correspondente. |
| RNF-BE-16 | Listagens com busca DEVEM aceitar `q` (texto livre) e filtros nomeados, combinados com a paginação (RNF-BE-13). Buscas por texto ignoram maiúsculas e acentos (RNF-BE-26) e o parâmetro `sort` só aceita campos de uma lista permitida. |

Formato padrão de erro:

```json
{
  "type": "https://meunegocio360.com.br/problemas/validacao",
  "title": "Dados inválidos",
  "status": 400,
  "detail": "Um ou mais campos são inválidos.",
  "instance": "/api/v1/auth/login",
  "code": "VALIDATION_ERROR",
  "traceId": "9f1c2e7a-...",
  "errors": [
    { "field": "email", "message": "deve ser um e-mail válido" }
  ]
}
```

O campo `code` é um identificador estável, em `UPPER_SNAKE_CASE`, usado pelo frontend para decidir o comportamento. O texto de `detail` e `message` NÃO DEVE ser usado como lógica pelo cliente.

## 3. Persistência

| ID | Requisito |
| --- | --- |
| RNF-BE-20 | Schema evoluído exclusivamente por migrations Flyway em `src/main/resources/db/migration`, nomeadas `V<n>__<descricao>.sql`. Migrations aplicadas NUNCA são editadas. |
| RNF-BE-21 | `spring.jpa.hibernate.ddl-auto` DEVE ser `validate` (ou `none`) em todos os perfis. |
| RNF-BE-22 | Tabelas em `snake_case` e no singular ou plural de forma consistente (padrão do projeto: **singular**, ex.: `usuario`). Toda tabela tem `id` (UUID), `criado_em`, `atualizado_em` (UTC) e `versao` (controle otimista com `@Version`). |
| RNF-BE-23 | Restrições de integridade (`NOT NULL`, `UNIQUE`, chaves estrangeiras, índices em colunas de busca) DEVEM existir no banco, e não apenas na aplicação. |
| RNF-BE-24 | Dados de seed apenas para o perfil `dev`, por migration ou *runner* dedicado. Nunca em `prod`. |
| RNF-BE-25 | Na fase de testes, testes de repositório e de integração DEVEM usar MySQL real via Testcontainers, e não um banco em memória com dialeto diferente. |
| RNF-BE-26 | Tabelas de negócio têm `empresa_id` (`NOT NULL`, chave estrangeira para `empresa`) e os índices de busca e de unicidade começam por `empresa_id` (RNF-096). Colunas de texto pesquisáveis usam *collation* `utf8mb4_0900_ai_ci` (sem distinção de maiúsculas e acentos). |
| RNF-BE-27 | CPF e CNPJ são armazenados **somente com dígitos** (11 e 14 posições) e validados pelos dígitos verificadores em um único validador reutilizável em `shared`. A máscara é responsabilidade do frontend. |
| RNF-BE-28 | Atualizações usam **controle otimista**: o cliente envia a `versao` lida e, se ela estiver desatualizada, a API responde `409` com `code` `VERSION_CONFLICT` (RNF-BE-22). |

## 4. Configuração e ambientes

| ID | Requisito |
| --- | --- |
| RNF-BE-31 | Em desenvolvimento, o `application-dev.yaml` DEVE importar o `.env` da raiz com `spring.config.import: optional:file:../.env[.properties]`; nos demais ambientes as variáveis vêm do ambiente (RNF-052). As variáveis são as documentadas em `.env.example`. A aplicação DEVE usar `server.port: ${PORT:8080}` (RNF-101). |
| RNF-BE-32 | A aplicação DEVE falhar na inicialização (*fail fast*) se uma variável obrigatória estiver ausente ou inválida (por exemplo, `JWT_SECRET` com menos de 32 bytes). |

## 5. Segurança, observabilidade e qualidade

| ID | Requisito |
| --- | --- |
| RNF-BE-40 | `SecurityFilterChain` explícito: sessão `STATELESS` e *deny-by-default* (`anyRequest().authenticated()`, RNF-021), com exceções listadas e documentadas. O CSRF do Spring PODE ficar desabilitado porque a autenticação das APIs é por cabeçalho `Authorization`; os dois únicos endpoints que usam cookie (`/auth/refresh` e `/auth/logout`) DEVEM ser protegidos por `SameSite=Strict`, cookie restrito ao seu caminho e verificação do cabeçalho `Origin` contra a lista de origens permitidas. |
| RNF-BE-41 | Cabeçalhos de segurança padrão do Spring Security mantidos (`X-Content-Type-Options`, `Cache-Control` em respostas autenticadas, HSTS em produção). |
| RNF-BE-42 | CORS configurado por *bean* único, lendo as origens permitidas de `CORS_ALLOWED_ORIGINS` (a mesma lista usada na verificação de `Origin`), com `allowCredentials = true` e, portanto, **sem** curinga `*`, jamais em produção. O frontend usa a mesma origem (RNF-103); o CORS atende a chamadas diretas de outras origens. |
| RNF-BE-43 | Um filtro DEVE ler ou gerar `X-Request-Id`, colocá-lo no MDC dos logs e devolvê-lo na resposta e no campo `traceId` dos erros (RNF-050). |
| RNF-BE-44 | Logs estruturados via suporte nativo do Spring Boot nos perfis `test` e `prod`. Eventos de segurança (login com sucesso, falha, bloqueio) DEVEM ser registrados **sem** senha, token ou e-mail completo (RNF-023, RNF-025). |
| RNF-BE-45 | `./mvnw verify` DEVE passar antes de cada PR. Ele executa a verificação de formatação (Spotless) e, a partir da fase de testes, os testes, as regras de arquitetura e a cobertura (JaCoCo). |
| RNF-BE-46 | Na fase de testes: JUnit 5, Mockito e AssertJ; `@WebMvcTest`/`MockMvc` para a camada web; `@DataJpaTest` ou `@SpringBootTest` com Testcontainers para persistência e integração; Spring Security Test para cenários autenticados. |
| RNF-BE-47 | A autorização por perfil DEVE ser aplicada nos *endpoints* (por exemplo, `@PreAuthorize`) conforme a matriz de perfis declarada em cada RF; falta de permissão responde `403` com `code` `ACCESS_DENIED`. A matriz é testada para cada perfil. |
