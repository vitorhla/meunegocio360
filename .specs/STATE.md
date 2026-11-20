# STATE — MeuNegócio360 (handoff / decisões / timers)

## Handoff
- **Ativo:** RF-FE-002 — Cadastro de empresa, layout da aplicação e guardas de acesso (frontend, `meunegocio360-js`)
- **Decisões do usuário (19/nov/2026):**
  - RF-FE-001 foi mergiado pelo usuário (PR #1, `f340430`)
  - Implementar RF-FE-002 na branch `development` (NÃO criar branch nova por enquanto)
  - **NÃO fazer operações git** — o usuário versiona manualmente
  - Fast path (mesmo pipeline do FE-001): developer → verifier independente → rodadas de correção
- **Regra do projeto (RNF-016 + AGENTS.md regra 9):** sem testes automatizados → verificação manual + `npm run lint && npm run build`
- **Documentação reorganizada:** requisitos agora em `docs/requisitos/` (`GERAIS.md`, `frontend/RF-FE-00N.md`, `backend/RF-BE-00N.md`, `RNF-FE.md`, `RNF-BE.md`). A reorg dos docs está uncommitted (antigos `docs/requisitos-*.md` deletados). **AGENTS.md atualizado** (autorizado pelo usuário): tabela e âncoras RNF-015/016/022 agora apontam para `docs/requisitos/GERAIS.md` (âncora do RNF-016 corrigida de 3.3 → 3.6).
- **RF-BE-002 está no ar** (usuário desenvolveu em paralelo, uncommitted; BE roda da IntelliJ) → verificação runtime completa foi feita no navegador (14/14 cenários Gherkin exercitados ao vivo contra o BE real).

## Decisões (RF-FE-002)
- Guardas em `src/app/guards/` (RequireAuth movido de `shared/ui` — corrigiu violação de RNF-FE-01: shared não importa features). `RequireRole` e `RequireEmpresa` novas; `AppLayout` em `src/app/layout/`.
- `features/empresa/` package-by-feature: api / hooks (EmpresaProvider c/ TanStack Query) / components (EmpresaForm RHF+Zod, EmpresaDados) / pages (EmpresaPage, CadastroEmpresaPage).
- Máscaras e validações em `shared/lib` (cnpj.js, telefone.js, fusos.js) + inputs mascarados via `Controller` em `shared/ui` (CnpjInput, TelefoneInput) — reutilizáveis por FE-003/004.
- Perfis centralizados em `shared/lib/perfis.js` (RNF: nunca texto solto).
- **F-03 RESOLVIDO (decisão do usuário, 19/nov — "alinhar com a spec"):** `/empresa` perdeu a `RequireEmpresa` (rota "Autenticado" da tabela de rotas); `EmpresaPage` trata `carregando`/`erro`/`ausente` (ausente: ADMIN → mensagem + botão "Cadastrar empresa" → `/empresa/cadastro`; não-ADMIN → "Aguardando o cadastro da empresa" idêntico ao anterior, título "Aguardando a empresa"). `RequireEmpresa` continua no `/` (e futuras rotas de negócio). **Reativação:** proposta de endpoint `GET /empresa/{id}` (`ADMIN`, 404 `COMPANY_NOT_FOUND`) adicionada a `docs/requisitos/backend/RF-BE-002.md` (seção "pendente de implementação") — botão Reativar só ganha vida quando o BE implementar.
- **Logos restauradas (pedido do usuário, 19/nov):** login ganhou `logo_meunegocio360_v2_sem_fundo.png` acima do "Entrar" (como no commit `0ddc5c7`, que NUNCA foi mergiado — o login mergiado em PR #1 nunca teve logo); navbar ganhou `icone_meunegocio360_branca.png` ao lado do "MeuNegócio360" (todas as telas, incl. home). Ambas copiadas de `assets/` (raiz, intatas) para `meunegocio360-js/src/assets/`; sizing em `theme.scss` (§10).
- Mensagens de UI exatamente como na tabela "Comportamentos e mensagens" do RF-FE-002 (RNF-FE-14: decisão por `code`).
- Alvos ≥44px e texto preto na navbar via regras em `theme.scss` (zero `style={{` novo — RNF-FE-08/RNF-FE-23).
- `usePageTitle` (novo, `shared/lib`) para título por rota (RNF-FE-24; Gherkin exige título de `/empresa/cadastro`).
- Flash de sucesso "Empresa cadastrada com sucesso." via `location.state.flash` no `/` (HomePage) — **corrigido na rodada de correções para ficar visível ~5 s** (estado local + limpeza do state de navegação de imediato).

## Veredito do verifier (1ª rodada) — NÃO CONFORME (4 MAJOR / 3 MINOR / 5 INFO)
- **F-01 MAJOR:** flash invisível (useEffect limpava em 1 frame) + raiz confirmada ao vivo: `EmpresaForm` chamava a API crua (bypass do `useEmpresaAcoes`) → cache nunca atualizado → `staleTime 5min` mantinha `ausente` → `RequireEmpresa` devolvia o ADMIN para `/empresa/cadastro` (navegação para `/` nunca estabilizava). Spec linha 76: "POST 201 → Atualiza o EmpresaProvider".
- **F-02 MAJOR:** "Recarregar dados" (409 VERSION_CONFLICT) não refetchava → loop de conflito sem saída (confirmado ao vivo: leitura continuava com `nome_fantasia` stale).
- **F-03 MAJOR:** Reativar inalcançável (guarda + contrato BE); tabela de rotas da spec diz `/empresa` = "Autenticado" (contradição). → decisão do usuário.
- **F-04 MAJOR:** `src/shared/ui/RequireAuth.jsx` órfão importando `features/` (violação RNF-FE-01) → **deletado**.
- **F-05 MINOR:** feedback de campo sem `role="alert"` → **corrigido** (6 `Form.Control.Feedback`).
- **F-06 MINOR:** `navigate()` durante render em `CadastroEmpresaPage` → **corrigido** (`<Navigate to="/empresa" replace />`).
- **F-07 MINOR:** telefone `type="text"` → **corrigido** (`type="tel"`).
- **F-08/09/10/11/12 INFO:** nome da empresa oculto no mobile 360px (decisão de design p/ registro); menu ainda hardcoded (FE-003+); `actionLabel` por substring → **corrigido** (estado `{message, acao}`); `.empresa-navbar` inline sem max-width efetivo → **corrigido** (`d-md-inline-block`); anel de foco ≈1,9:1 (pré-existente FE-001, fora de escopo).
- **Achados novos da verificação ao vivo (fora do veredito):** (a) foco ao ABRIR o ConfirmModal ficava no DIV do diálogo (react-bootstrap rouba o foco após o rAF) → **corrigido** (setTimeout 200 ms, pós-fade); foco ao FECHAR restaurava corretamente (verificado ao vivo); (b) `setFocus('cnpj')` no 409 duplicado sem `setTimeout` → foco ficava no BODY (corrida) → **corrigido** (setTimeout 100 ms, mesmo padrão do VALIDATION_ERROR); (c) `<main>` aninhado (AppLayout + AcessoNegadoPage/AguardandoEmpresaPage/HomePage) → **corrigido** (páginas viram `div`).
- **Gates após correções:** lint 0 erros; build OK (chunks por página, sem INEFFECTIVE_DYNAMIC_IMPORT); `style={` só os 8 pré-existentes; `shared/` zero import de `features/`; órfão removido.

## Veredito do verifier (2ª rodada — revisão das correções) — CONFORME ✅
- Todos os 11 itens confirmados com evidência arquivo:linha; mensagens da spec conferidas string-a-string; `role="alert"` confirmado eficaz (react-bootstrap `Feedback.js` faz spread de props no DOM).
- Regressão de maior risco descartada com evidência na fonte da lib: `mutateAsync` (TanStack 5.107.1) rejeita com o erro original (sem wrapper) → `err.code` chega intacto ao catch. Sem ciclos de import; sem loop no effect da HomePage; StrictMode inofensivo.
- Novos achados: **R-1 (Minor) RESOLVIDO** — `LoadingSpinner`/`ErrorScreen` renderizavam `<main>` (aninhamento transitório dentro do AppLayout) → viraram `<div>`; **R-2 (INFO, ignorado)** — flicker de título de 1 frame em `CadastroEmpresaPage` no early-return `ativa`; **R-3 (INFO, sem ação)** — flash só no mount (inalcançável no roteamento atual).
- Gates da rodada: lint 0; build OK; `style={` = 7 (caiu 1: `<p>` redundante do login removido ao restaurar a logo); `shared/` sem import de `features/`; `package.json`/lock inalterados.
- Verdicto registrado pelo EM aqui (o verifier não altera arquivos, por instrução).

## Verificação ao vivo (navegador + BE real) — resumo
14/14 cenários Gherkin exercitados: 11 ✅; 3 com defeito corrigido na rodada (cadastro→flash+navbar; edição→navbar stale; 409→recarregar sem refetch) + foco no modal ao abrir + foco no 409 duplicado. Payloads confirmados na rede (CNPJ/telefone só dígitos, PATCH `{ativa,versao}` exato). Dados de teste **limpos** (tabela `empresa` restaurada a vazia; o usuário depois cadastrou "wewqeqweqweqweqwe" por conta própria e está testando).

## Timers
| # | Agent | Dispatch (UTC) | Return (UTC) | Real Time | Self-estimate |
|---|-------|----------------|--------------|-----------|---------------|
| 1 | developer | 2026-11-19T23:10:00Z | 2026-11-19T23:24:00Z | ~14 min | — |
| 2 | verifier | 2026-11-19T23:26:00Z | 2026-11-19T23:52:00Z | ~26 min | ~2h |
| 3 | verifier (revisão correções) | 2026-11-19T23:58:00Z | 2026-11-20T00:40:00Z | ~40 min | ~2h |

**Total Real Time: ~80 min de agents + verificação ao vivo do EM (~40 min) + rodada logo/F-03/AGENTS.md**

## Pendências para o usuário
1. **BE-002:** implementar `GET /empresa/{id}` (proposta já no `docs/requisitos/backend/RF-BE-002.md`) para o botão **Reativar** ganhar vida.
2. **Defeitos no BE-002 do usuário (reportar, não corrigir):** (a) `PATCH /empresa/{id} {ativa:true}` → **500** no build rodando (inativar funciona; SQL bruto funciona → bug do app/build em execução, logs no console da IntelliJ); (b) seed dev **não idempotente** → duplicou usuários (3× "Administrador") → refresh tokens órfãos → logout involuntário de sessões ativas; (c) `/actuator/health` → 500.
3. **Verificação ao vivo pendente (usuário estava com o app aberto):** fluxos F-03 `ausente` (logar como OPERADOR sem empresa → "Aguardando…"; ADMIN sem empresa em `/empresa` → "Cadastrar empresa") e logo da tela de login (aparece no próximo login). O HMR já aplicou o ícone da navbar na sessão ativa.
4. 7 `style={{` pré-existentes em `features/auth/**` + `shared/ui/ErrorScreen` (fora de escopo do FE-002; caiu de 8 ao remover o `<p>` redundante do login).
5. F-08 (nome da empresa oculto no mobile 360px) — decisão de design a registrar.
6. F-12 (contraste do anel de foco ≈1,9:1, pré-existente FE-001) — pauta de a11y.
7. Logo PNG no bundle pesa 258 kB (mesmo arquivo usado em `0ddc5c7`) — candidata a otimização (webp/redimensionar) na pauta de performance.
8. Favicon da aba ainda é o padrão (0ddc5c7 usava `public/favicon.png`; `index.html` não tem `<link rel="icon">`) — sugerido, não executado.

## Próximos passos
1. ✅ developer → verifier (NÃO CONFORME) → verificação ao vivo 14/14 → rodada de correções (9 itens)
2. ✅ verifier 2ª rodada: CONFORME + R-1 resolvido (main aninhado nos 2 componentes shared)
3. ✅ F-03 alinhado à spec + endpoint `GET /empresa/{id}` proposto no RF-BE-002 + AGENTS.md atualizado + logos restauradas
4. ⏳ usuário: revisar/aceitar o diff uncommitted (branch `development`), versionar, e validar os fluxos pendentes acima
