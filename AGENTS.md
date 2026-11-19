# AGENTS.md — MeuNegocio360

Instruções para assistentes de IA (Copilot, Gemini Code Assist, Continue, Claude Code e similares) que trabalham neste repositório. Este arquivo **não repete** o conteúdo dos demais documentos: ele aponta para eles e acrescenta apenas regras de comportamento para a IA.

## Onde encontrar cada informação

| Preciso de… | Consulte |
| --- | --- |
| Arquitetura, stack, estrutura de pastas, comandos, variáveis de ambiente, fluxo de contribuição, convenções de branch/commit/PR e implantação | [`README.md`](./README.md) |
| O que implementar e como (RF, regras de negócio, contratos, critérios de aceite, pontos de atenção) | [`docs/requisitos-be.md`](./docs/requisitos-be.md) e [`docs/requisitos-fe.md`](./docs/requisitos-fe.md) |
| Regras válidas para todo o projeto (segurança, qualidade, microsserviços, API, acessibilidade, implantação) e template de novos requisitos | [`docs/requisitos-gerais.md`](./docs/requisitos-gerais.md) |

## Regras de comportamento para a IA

1. **Leia antes de gerar.** Identifique o requisito (`RF-BE-xxx` / `RF-FE-xxx`) e leia a seção inteira, mais os RNFs que ela destaca. Cite o ID do requisito em commits e PRs.
2. **Planeje antes de codar.** Proponha um plano em passos pequenos e verificáveis e aguarde a revisão antes de executá-lo.
3. **Pergunte quando houver ambiguidade ou conflito** entre documentos ou com o código existente. Não presuma.
4. **Faça só o que o requisito pede.** Não crie funcionalidades, refatorações nem arquivos extras. Respeite o que está "fora do escopo".
5. **Não adicione dependências por conta própria.** Use apenas as listadas em "Dependências introduzidas" do requisito em andamento ([RNF-015](./docs/requisitos-gerais.md#33-dependências)). Confira que a biblioteca existe e use a versão do `pom.xml`/`package.json`.
6. **Não invente APIs.** Na dúvida sobre uma biblioteca ou sobre uma versão, consulte a documentação oficial da versão instalada e diga o que não pôde confirmar.
7. **Segredos e dados sensíveis:** nunca os escreva no código, em arquivos versionados ou em logs, nem os peça ao usuário em texto claro ([RNF-022](./docs/requisitos-gerais.md#34-segurança-e-privacidade)). Use variáveis de ambiente.
8. **Seja explícito sobre o que gerou.** Ao entregar, informe o que foi criado/alterado e o que não foi verificado, para que o autor registre no PR o que aceitou, ajustou ou rejeitou.
9. **Fase de testes:** não gere testes automatizados fora da fase de testes do projeto, a menos que seja pedido ([RNF-016](./docs/requisitos-gerais.md#33-dependências)).
10. **Documente no Swagger/OpenAPI.** Todo endpoint novo DEVE incluir `@Tag` + `@Operation` (summary e descrição) + `@ApiResponse` para cada código possível (erros em `application/problem+json` com o `code` do contrato), e os DTOs de entrada/saída DEVE usar `@Schema` nos campos (descrição + exemplo). O padrão está em `auth/api/AuthController.java` e `shared/config/OpenApiConfig.java`.
