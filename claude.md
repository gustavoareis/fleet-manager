## Padrão de Commits (Conventional Commits)
Sempre que criar ou sugerir um commit, siga estritamente estas regras:
- Formato: `<tipo>(<escopo opcional>): <descrição curta>`
- Tipos permitidos: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`.
- Idioma: português do Brasil.
- Descrição: use verbo no imperativo (ex.: "adiciona", "corrige", "remove"), no máximo 72 caracteres e sem ponto final.
- Corpo (opcional): use apenas se a alteração for complexa, após uma linha em branco, com tópicos curtos explicando o porquê da mudança.
- Breaking changes: adicione `!` após o tipo/escopo e detalhe em `BREAKING CHANGE:` no rodapé.
- Nunca adicione mensagens genéricas como "atualiza código" ou assinatura de coautoria, a menos que solicitado.
