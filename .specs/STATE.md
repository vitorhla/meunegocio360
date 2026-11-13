# STATE — MeuNegócio360 (handoff / decisões / timers)

## Handoff
- **Ativo:** RF-FE-001 — Fluxo de login do usuário (frontend, `meunegocio360-js`)
- **Decisão do usuário (12/nov/2026):**
  - Frontend ANTES do backend (RF-FE-001 antes de RF-BE-001)
  - **NÃO criar branches e NÃO fazer commits/pushes** — o usuário faz o git manualmente depois
  - Desenvolver o mais rápido possível (fast path: sem squad completo, sem fase de testes automatizados)
- **Regra do projeto (RNF-016 + AGENTS.md regra 9):** sem testes automatizados fora da fase de testes → verificação manual + `npm run lint && npm run build`
- **Backend NÃO existe ainda** — desenvolver estritamente contra o contrato de RF-BE-001 (`docs/requisitos-be.md`)
- Verificação ao vivo (navegador contra API real) fica pendente até RF-BE-001

## Decisões
- Pipeline: fast path Medium — spec+design já existem em `docs/requisitos-fe.md` (autorizados pelo usuário ao iniciar)
- Dispatch único de `developer` com briefing completo (gate de confirmação do usuário satisfeito na instrução de início)
- Estado persistido neste arquivo (`.specs/`); usuário pode versionar ou ignorar depois

## Timers
| # | Agent | Dispatch (UTC) | Return (UTC) | Real Time | Self-estimate |
|---|-------|----------------|--------------|-----------|---------------|
| 1 | developer | 2026-11-12T23:41:33Z | 2026-11-12T23:51:00Z | ~9,5 min | 65 min (auto, referência) |
| 2 | developer (fixes) | 2026-11-12T23:53:28Z | 2026-11-12T23:54:30Z | ~1,5 min | 12 min (auto, referência) |

**Total Real Time: ~11 min** (Self-estimate somado: 77 min — referência apenas)

## Gate final (verificado pelo EM, 23:55Z)
- [x] `npm run lint` limpo
- [x] `npm run build` sem `INEFFECTIVE_DYNAMIC_IMPORT`; chunks separados (LoginPage 127.97 kB, index 306.89 kB, CSS 232.78 kB) → carga inicial ≈ 167 kB gzip (RNF-FE-32: ideal ≤ 200 kB) ✅
- [x] Zero tokens em storage (grep: apenas `login_redirect_from` em sessionStorage — rota de origem, não token; permitido)
- [x] Sem git: branch `feat/rf-fe-001-login` (criada pelo usuário às 19:51), HEAD 095c12b, tudo uncommitted
- [x] Sem arquivos de teste

## Ajustes devolvidos ao developer (run 2)
1. **RNF-FE-05**: remover re-exports estáticos das páginas de `features/auth/index.js` (quebra o code splitting do `React.lazy`; build emite `INEFFECTIVE_DYNAMIC_IMPORT`)
2. **StrictMode double-restore** (armadilha #4 do requisito): no boot o effect roda 2x em dev → 2x `POST /auth/refresh` paralelos com o mesmo cookie rotativo → 2ª chamada dispara detecção de reuso e revoga a família. Deduplicar restauração no boot (promise compartilhada em módulo)

## Próximos passos
1. developer implementa RF-FE-001 (fast path)
2. EM verifica gate: `npm run lint` + `npm run build` + boot do dev server
3. Reporte final: o que foi feito, o que não foi verificado (API ao vivo), pendências p/ o usuário
