# Studora — Backend

Studora é uma plataforma de estudos focada em um **banco de questões estruturado e de alta qualidade** para concursos públicos brasileiros.

> Este repositório contém **apenas a API**. O frontend vive em repositório separado: **[robsonswe/studora-frontend](https://github.com/robsonswe/studora-frontend)**.

---

## Stack

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.5.13 |
| Build | Maven 3.9 |
| Banco de Dados | PostgreSQL 17 |
| Migrações | Flyway |
| ORM | Spring Data JPA / Hibernate |
| Mapeamento | MapStruct 1.5.5 + Lombok |
| Documentação API | SpringDoc OpenAPI 2.8.16 (`/swagger-ui.html`, `/api-docs`) |
| Métricas | Micrometer + Prometheus (`/actuator/prometheus`) |
| Testes | JUnit 5 + Testcontainers |

---

## Arquitetura

```
controller/v1/*  →  service/*  →  repository/* (+ specification/*)  →  entity/*
       ↕                  ↕                    ↕
   dto/* + Views.java  mapper/* (MapStruct)  db/migration/V1__*.sql
       ↕
  GlobalExceptionHandler (RFC 7807 ProblemDetail)
```

- **Controllers** em `com.studora.controller.v1` expostos sob prefixo `/api/v1` (`config/WebConfig.java`), mais `controller/operational/HealthController` em `/health`.
- **Services** concentram regras de negócio e validações (ex: edital, visibilidade de gabarito, numeração de prova).
- **Repositories** com Specifications para filtros dinâmicos + `Custom Repository` para geração de simulados (scoring ponderado).
- **Mappers** MapStruct convertem `entity ↔ dto` sem código manual.
- **DTOs/Views** controlam payload via `@JsonView` (`Views.QuestaoOculta/Visivel`, `Geracao`, `Summary`, `RespostaVisivel`) — a doc OpenAPI reflete a view graças a `springdoc.writer-with-json-view=true`.
- **Cross-cutting:** `WebConfig` (CORS `*`, `StringUtils.normalizeSpace` no `InitBinder`), `StringNormalizationDeserializer`, `JpaAuditingConfig` (`BaseEntity` com `createdAt/updatedAt`), `AsyncConfig`, `LoggingInterceptor`, timezone UTC forçado em Jackson e Hibernate (`application.properties`).

### Estrutura de pastas

```
src/main/java/com/studora/
├── config/                  # WebConfig, OpenApiConfig, JpaAuditingConfig, AsyncConfig
├── common/constants/        # AppConstants (paginação, validações)
├── common/interceptor/      # LoggingInterceptor
├── controller/
│   ├── v1/                  # 11 controllers REST
│   └── operational/         # HealthController
├── service/                 # Regras de negócio + StatsAssembler, SimuladoService, etc.
├── repository/
│   └── specification/       # Filtros dinâmicos (Questao, Concurso, Tema, Subtema)
├── entity/                  # 22 entidades JPA
├── dto/                     # DTOs + Views + PageResponse/PostResponseDto
├── mapper/                  # MapStruct mappers
├── exception/               # GlobalExceptionHandler, ResourceNotFound, Validation, Conflict
└── util/                    # StringUtils, PaginationUtils
src/main/resources/
├── application.properties
└── db/migration/V1__Initial_Schema.sql
docker/
├── prometheus/prometheus.yml
└── grafana/{provisioning,dashboards}/
```

---

## Modelo de Dados

Schema único `V1__Initial_Schema.sql` (361 linhas, 22 tabelas). Índices únicos em colunas `*_normalized` garantem unicidade case/accent-insensitive (mantidas pela aplicação via `StringUtils.normalizeForSearch`).

**Núcleo institucional:**
`instituicao` → `banca` → `concurso` (instituicao+banca+ano+mes único) → `concurso_cargo` (concurso+cargo único, flag `inscrito`) → `cargo` (nome+nivel+area_normalized único)

**Taxonomia:**
`disciplina` → `tema` (disciplina+nome_norm único) → `subtema` (tema+nome_norm único) → `questao_subtema` (questao+subtema único, flag `principal`)

**Questão e prova:**
`questao` (`anulada`, `desatualizada`, `autoral`, `image_url`) → `alternativa` (questao+ordem único, `correta`, `justificativa`) + `questao_prova_secao` (questao+prova_secao único, `numero_questao`) → `prova` (concurso+concurso_cargo) → `prova_secao` (prova+ordem único) → `secao_cargo` (concurso_cargo) → `secao_disciplina` + `secao_disciplina_subtema` (edital: quais subtemas por disciplina/seção)

**Execução e análise:**
`resposta` (questao+alternativa+`dificuldade_id`/`tempo_resposta_segundos`/`simulado_id`, `ON DELETE SET NULL` para simulado) → `simulado` (filtros materializados em `simulado_area/disciplina/tema/subtema` + `simulado_questao` com `ordem`) → `estudo_subtema` (registro de sessão de estudo por subtema)

Relações com `ON DELETE CASCADE` preservam integridade (ex: deletar concurso deleta `concurso_cargo` e cascata até provas; deletar simulado preserva respostas desvinculadas).
- **Paginação e ordenação:** `page`/`size` (padrão `20` — `AppConstants.DEFAULT_PAGE_SIZE`), `sort` + `direction` via `PaginationUtils`.

---

## API Reference

Prefixo `/api/v1` para `controller/v1/*` (`WebConfig`). Operacional sem prefixo. Documentação viva em **`/swagger-ui.html`** e **`/api-docs`** (`application.properties`).

| Recurso | Endpoints |
|---------|-----------|
| **Bancas** | `GET /bancas` · `GET /bancas/{id}` · `POST /bancas` · `PUT /bancas/{id}` · `DELETE /bancas/{id}` |
| **Instituições** | `GET /instituicoes` · `GET /instituicoes/areas` · `GET /instituicoes/{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` |
| **Cargos** | `GET /cargos` · `GET /cargos/areas` · `GET /cargos/{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` |
| **Concursos** | `GET /concursos` (filtros: banca, instituicao, ano, mes, finalizado) · `GET /concursos/{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` · `PATCH /concursos/cargos/{concursoCargoId}/inscricao` · `PATCH /concursos/{id}/finalizado` |
| **Disciplinas** | `GET /disciplinas` · `GET /disciplinas/{id}` · `GET /disciplinas/{id}/completo` (com temas/subtemas) · `POST` · `PUT /{id}` · `DELETE /{id}` |
| **Temas** | `GET /temas` · `GET /temas/{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` |
| **Subtemas** | `GET /subtemas` · `GET /subtemas/{id}` · `POST` · `PUT /{id}` · `DELETE /{id}` · `POST /subtemas/{id}/estudos` · `GET /subtemas/{id}/estudos` · `DELETE /subtemas/{subtemaId}/estudos/{estudoId}` |
| **Questões** | `GET /questoes` · `GET /questoes/random` · `GET /questoes/{id}` · `POST /questoes` · `PUT /questoes/{id}` · `DELETE /questoes/{id}` · `PATCH /questoes/{id}/desatualizada` |
| **Respostas** | `GET /respostas` · `GET /respostas/{id}` · `GET /respostas/questao/{questaoId}` · `POST /respostas` · `DELETE /respostas/{id}` |
| **Simulados** | `GET /simulados` · `POST /simulados/gerar` · `GET /simulados/{id}` · `PATCH /simulados/{id}/iniciar` · `PATCH /simulados/{id}/finalizar` · `DELETE /simulados/{id}` |
| **Analytics** | `GET /analytics/consistencia?days=30` · `GET /analytics/disciplinas` · `GET /analytics/disciplinas/{id}` · `GET /analytics/evolucao` · `GET /analytics/taxa-aprendizado` |
| **Operacional** | `GET /health` · `GET /actuator/health` · `GET /actuator/prometheus` · `GET /api-docs` · `GET /swagger-ui.html` |

**Padrões:** paginação `PageResponse<T>` (`content`, `pageNumber`, `pageSize`, `totalElements`, `totalPages`, `last`), criação retorna `PostResponseDto {id, message}` com `201`, erros em **RFC 7807 `ProblemDetail`** (`type`, `title`, `status`, `detail`, `instance`, `errors` por campo) via `GlobalExceptionHandler` + `spring.mvc.problemdetails.enabled=true`.

Exemplo:

```bash
# Listar questões de uma disciplina com gabarito forçado (admin)
curl "http://localhost:4534/api/v1/questoes?disciplinaId=1&admin=true&page=0&size=20&sort=id&direction=DESC"

# Questão aleatória filtrada (exclui respondidas <30 dias)
curl "http://localhost:4534/api/v1/questoes/random?disciplinaId=1&bancaId=2&includeAutoral=false"

# Criar resposta
curl -X POST http://localhost:4534/api/v1/respostas \
  -H "Content-Type: application/json" \
  -d '{"questaoId":10,"alternativaId":42,"dificuldade":"MEDIA","tempoRespostaSegundos":38,"justificativa":"..."}'
```

---

## Configuração & Variáveis de Ambiente

`application.properties` lê a conexão **exclusivamente** de variáveis — nada fixo em código.

| Variável | Usada por | Padrão (`.env.example`) | Descrição |
|----------|-----------|-------------------------|-----------|
| `DB_URL` | app (`spring.datasource.url`) | `jdbc:postgresql://localhost:5433/studora` | JDBC. Local host usa `localhost:5433`; dentro do compose o serviço `app` usa `jdbc:postgresql://db:5432/${POSTGRES_DB}` |
| `DB_USERNAME` | app | `studora` | Usuário PG |
| `DB_PASSWORD` | app | `studora` | Senha PG |
| `POSTGRES_DB` | `docker-compose db` | `studora` | Nome do banco criado |
| `POSTGRES_USER` | `docker-compose db` | `studora` | Usuário PG do container |
| `POSTGRES_PASSWORD` | `docker-compose db` | `studora` | Senha PG do container |
| `DB_HOST_PORT` | `docker-compose db` | `5433` | Porta publicada no host (`5433:5432` evita conflito com PG local) |
| `APP_HOST_PORT` | `docker-compose app` | `4534` | Porta da API no host |
| `GRAFANA_USER` | `docker-compose grafana` | `admin` | Login Grafana |
| `GRAFANA_PASSWORD` | `docker-compose grafana` | `admin` | Senha Grafana |
| `GRAFANA_HOST_PORT` | `docker-compose grafana` | `3000` | Porta Grafana no host |

Outras configs relevantes (`application.properties`): `server.port=4534`, `spring.jpa.hibernate.ddl-auto=none`, `spring.jpa.show-sql=true` + `format_sql`, `hibernate.generate_statistics=true` e `LOG_QUERIES_SLOWER_THAN_MS=100`, `spring.jackson.time-zone=UTC` + `hibernate.jdbc.time_zone=UTC`, `springdoc.*`, `management.endpoints.web.exposure.include=health,prometheus`, CORS `allowedOrigins=*` (`WebConfig`).

---

## Setup

### Pré-requisitos

- Java 17+
- Maven 3.6+ (ou use o Docker build)
- Docker + Docker Compose (para PG e para testes)

### Execução local (sem Docker para a app)

```bash
git clone https://github.com/robsonswe/studora-backend.git
cd studora-backend

cp .env.example .env   # ajuste se necessário
docker compose up -d db

mvn spring-boot:run
# API em http://localhost:4534
# Swagger em http://localhost:4534/swagger-ui.html
# Health em http://localhost:4534/actuator/health
```

### Executando com Docker (banco + app)

```bash
docker compose --profile app up --build
# API em http://localhost:4534 (ou ${APP_HOST_PORT})
```

Só a imagem:

```bash
docker build -t studora .
docker run --rm -p 4534:4534 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/studora \
  -e DB_USERNAME=studora \
  -e DB_PASSWORD=studora \
  studora
```

### Build

```bash
mvn clean package              # gera target/studora-*.jar
DB_URL=jdbc:postgresql://localhost:5433/studora \
DB_USERNAME=studora DB_PASSWORD=studora \
  java -jar target/studora-*.jar
```

---

## Observabilidade (Prometheus + Grafana)

A app expõe métricas Micrometer em `/actuator/prometheus` (JVM, HTTP, HikariCP `StudoraHikariPool`, etc.).

```bash
docker compose --profile observability up -d
```

- **Grafana:** `http://localhost:3000` (padrão `admin`/`admin`, via `GRAFANA_*` no `.env`) — datasource Prometheus e dashboard Spring Boot já provisionados em `docker/grafana/`.
- **Prometheus:** só na rede interna do compose, scrape de `studora-app:4534` (profile `app`) e `host.docker.internal:4534` (app via `mvn spring-boot:run`) — ver `docker/prometheus/prometheus.yml`.

> Localmente `/actuator/prometheus` não exige auth. Proteja o endpoint se expor publicamente.

---

## Testes

```bash
mvn test
```

Integração roda contra **PostgreSQL efêmero via Testcontainers** (`src/test/resources/application-test.properties`). Cobertura inclui: CRUD de todos os recursos, busca accent-insensitive, paginação/ordenação, visibilidade de gabarito, geração/filtros de simulado, analytics, normalização de strings, cascade de deleção e validações de edital.

---

## Status & Roadmap

**Feito:**
- Schema estável com Flyway + 22 tabelas e índices `*_normalized`
- CRUD completo para bancas, instituições, cargos, concursos, disciplinas, temas, subtemas, questões, respostas, simulados e estudos por subtema
- Provas e seções com numeração global sequencial
- Simulados com geração filtrada, ciclo iniciar/finalizar e preservação de histórico
- Analytics: consistência diária, domínio por disciplina, evolução semanal e taxa de aprendizado
- Busca case/accent-insensitive, paginação, visibilidade temporal de gabarito, docs OpenAPI e observabilidade

**Próximos passos:**
- Autenticação e gerenciamento de usuários

---

## Licença

A definir
