## Padrão de Commits (Conventional Commits)
Sempre que criar ou sugerir um commit, siga estritamente estas regras:
- Formato: `<tipo>(<escopo opcional>): <descrição curta>`
- Tipos permitidos: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`.
- Idioma: português do Brasil.
- Descrição: use verbo no imperativo (ex.: "adiciona", "corrige", "remove"), no máximo 72 caracteres e sem ponto final.
- Corpo (opcional): use apenas se a alteração for complexa, após uma linha em branco, com tópicos curtos explicando o porquê da mudança.
- Breaking changes: adicione `!` após o tipo/escopo e detalhe em `BREAKING CHANGE:` no rodapé.
- Nunca adicione mensagens genéricas como "atualiza código" ou assinatura de coautoria, a menos que solicitado.



Prompt inicial do projeto:
# Contexto
Você vai me ajudar a construir um projeto de portfólio chamado **MedAgenda**, um sistema
de agendamento de consultas para clínicas. O objetivo é demonstrar domínio de Angular
moderno, Java/Spring, JSP e APIs REST, simulando um cenário real em que um sistema
legado (JSP) convive com um front moderno (Angular), ambos consumindo o mesmo backend.

Use sempre as **versões estáveis mais recentes** de cada tecnologia. Antes de começar,
verifique quais são e me informe.

# Stack
- **Frontend:** Angular (última versão), apenas Standalone Components (sem NgModules),
  Signals (`signal`, `computed`, `effect`, `input()`, `output()`), nova sintaxe de
  controle de fluxo (`@if`, `@for`), RxJS, roteamento com lazy loading e guards,
  formulários reativos tipados, SCSS, Angular Material (com tema customizado em SCSS).
- **Backend:** Java (última LTS), Spring Boot (última versão), Spring Security com JWT,
  Spring Data JPA, Bean Validation, Flyway, PostgreSQL, OpenAPI/Swagger.
- **Legado:** módulo administrativo em JSP + JSTL servido pelo mesmo backend
  (Spring MVC com `@Controller` retornando views JSP).
- **Testes:** testes unitários no Angular, E2E com Playwright, JUnit 5 + Mockito no backend.
- **Infra:** Docker Compose (postgres + backend + frontend), GitHub Actions rodando build
  e testes.

# Estrutura do projeto
medagenda/
├── backend/   (pacotes por feature: auth, paciente, medico, especialidade,
│               convenio, agenda, admin; JSPs em WEB-INF/jsp)
├── frontend/  (core/, shared/, features/paciente, features/medico, features/auth)
└── docker-compose.yml

# Domínio
Entidades:
- Usuario: id, email, senhaHash, perfil (PACIENTE, MEDICO, RECEPCAO, ADMIN)
- Paciente: nome, cpf, telefone, dataNascimento, convenio, usuario
- Medico: nome, crm, especialidade, usuario
- Especialidade: nome
- Convenio: nome, ativo
- DisponibilidadeMedico: medico, diaSemana, horaInicio, horaFim, duracaoConsultaMin
- Consulta: paciente, medico, dataHora, status (AGENDADA, CONFIRMADA, CANCELADA,
  REALIZADA, FALTOU), observacoes

Regras de negócio:
- Não permitir dois agendamentos no mesmo horário para o mesmo médico.
- Só permitir agendar dentro da disponibilidade do médico e em datas futuras.
- Paciente não pode ter duas consultas no mesmo horário.
- Cancelamento só com no mínimo 24h de antecedência.
- Endpoint que retorna os horários livres de um médico em uma data.

Perfis e telas:
- Paciente (Angular): cadastro/login, buscar médico por especialidade (busca com
  debounceTime + switchMap), agendar em stepper (especialidade → médico → data →
  horário), ver/cancelar consultas, histórico.
- Médico (Angular): agenda diária/semanal, marcar consulta como realizada ou falta,
  anotações, contadores com computed().
- Recepção (JSP): CRUD de convênios e especialidades, lista de consultas do dia com
  botão "confirmar chegada", relatório simples.

# Requisitos de qualidade
- Código limpo, em inglês nos identificadores, com comentários só onde agregam.
- Tratamento global de erros no backend (@RestControllerAdvice) com respostas padronizadas.
- Interceptor no Angular para anexar o JWT e tratar 401/403.
- Layout responsivo e acessível (labels, foco visível, navegação por teclado, contraste).
- Paginação nas listagens.
- Seeds com dados de exemplo e usuários de teste para cada perfil.

# Forma de trabalho
Trabalhe em etapas. Ao final de cada uma, pare, resuma o que foi feito, diga como
testar e aguarde minha confirmação antes de seguir:
1. Setup do backend + entidades + migrations + CRUDs básicos + Swagger
2. Autenticação JWT e autorização por perfil
3. Setup do Angular + tema + login + interceptor + guards + estado de auth com Signals
4. Fluxo de agendamento (backend + frontend)
5. Área do médico
6. Módulo JSP da recepção
7. Testes (unitários, E2E, backend)
8. Docker Compose, GitHub Actions e README com instruções e diagrama

Explique as decisões importantes de forma breve, porque quero entender o código para
poder apresentá-lo em entrevista. Comece pela etapa 1.