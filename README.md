# Studora

Studora é uma plataforma de estudos **em desenvolvimento (WIP)** focada na construção de um **banco de questões estruturado e de alta qualidade** para concursos públicos.

O projeto é guiado por uma abordagem **content-first**: correção, estrutura e escalabilidade vêm antes de interface, gamificação ou métricas artificiais.

---

## Tecnologias Utilizadas

* **Linguagem**: Java 17+
* **Framework**: Spring Boot 3.x
* **Banco de Dados**: PostgreSQL 17 (testes usam Testcontainers)
* **Build Tool**: Maven
* **Arquitetura**: REST API com padrão MVC

---

## Setup do Projeto

### Pré-requisitos

* Java 17 ou superior
* Maven 3.6 ou superior
* Docker (para o banco PostgreSQL e para os testes)

### Execução Local

1. Clone o repositório:
   ```bash
   git clone <url-do-repositorio>
   cd studora-back
   ```

2. Suba o banco de dados e configure as variáveis de ambiente:
   ```bash
   cp .env.example .env  # ajuste se necessário
   docker compose up -d db
   ```
   
   A aplicação lê a conexão exclusivamente das variáveis `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` — nada é fixado em código ou properties.

3. Compile e execute o projeto:
   ```bash
   mvn spring-boot:run
   ```

4. A aplicação estará disponível em: `http://localhost:4534`

### Executando com Docker

Para subir banco + aplicação juntos:

```bash
docker compose --profile app up --build
```

Ou apenas construa a imagem da aplicação:

```bash
docker build -t studora .
docker run --rm -p 4534:4534 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/studora \
  -e DB_USERNAME=studora \
  -e DB_PASSWORD=studora \
  studora
```

### Build do Projeto

Para construir o JAR executável:
```bash
mvn clean package
```

Para executar o JAR gerado:
```bash
DB_URL=jdbc:postgresql://localhost:5433/studora DB_USERNAME=studora DB_PASSWORD=studora java -jar target/studora-<versao>.jar
```

---

## Observabilidade (Prometheus + Grafana)

A aplicação expõe métricas Micrometer em `/actuator/prometheus` (JVM, HTTP, HikariCP, etc.).

Para subir o stack completo de observação:

```bash
docker compose --profile observability up -d
```

* **Grafana**: `http://localhost:3000` (login padrão `admin`/`admin`, configurável via `GRAFANA_*` no `.env`)
  * O datasource Prometheus e um dashboard de Spring Boot já vêm provisionados em `docker/grafana/`
* **Prometheus** roda apenas na rede interna do compose, coletando métricas de duas origens:
  * `studora-app:4534` — app rodando como serviço compose (`--profile app`)
  * `host.docker.internal:4534` — app rodando da sua máquina via `mvn spring-boot:run`

Não é necessário autenticar `/actuator/prometheus` localmente; se expor publicamente algum dia, proteja o endpoint antes.

---

## Status do Projeto

**Fase 1 — API e Modelo de Dados (atual)**

* Definição e estabilização do schema do banco de dados
* Construção da API para questões, alternativas, taxonomia e respostas
* Camadas de controller, service e repository implementadas

**Fase 2 — Interface (planejada)**

* Interface web para navegação, resolução e análise de questões
* Fluxos de estudo orientados por disciplina, tema e subtema

---

## Conceitos Centrais

O Studora modela questões de prova de forma fiel à realidade dos concursos públicos, mantendo flexibilidade para evoluções futuras.

Ideias-chave:

* Toda questão é respondida por meio da escolha de uma alternativa
* Questões de Certo/Errado são modeladas como duas alternativas
* Não há acoplamento rígido com regras de pontuação
* A classificação do conteúdo é explícita e hierárquica

---

## Visão Geral do Modelo de Dados

### Entidades principais

* **Concurso**: metadados do certame (banca, ano, cargo, nível, área)
* **Questão**: enunciado da questão
* **Alternativa**: alternativas ordenadas, com indicação de correção e justificativa
* **Resposta**: registro de uma resposta dada (sem lógica de pontuação)
* **Disciplina → Tema → Subtema**: taxonomia hierárquica de conteúdo

### Decisões de design

* Uma questão pode estar associada a **múltiplos subtemas**
* Regras de validação e pontuação ficam na camada de serviço (API), não no banco
* Suporte a mídia limitado a **imagens**, refletindo provas presenciais

---

## Banco de Dados

* Banco: **PostgreSQL** (via Docker Compose para desenvolvimento)
* Schema gerenciado pelo **Flyway** (`src/main/resources/db/migration`)
* Testes de integração rodam contra um PostgreSQL efêmero via **Testcontainers**
* Busca case/accent-insensitive suportada por colunas `*_normalized` mantidas pela aplicação

---

## O que Este Projeto *Ainda Não É*

* Não possui interface gráfica completa
* Não possui ranking ou gamificação
* Não implementa regras oficiais de pontuação
* Não possui gerenciamento completo de usuários

Esses pontos poderão ser avaliados no futuro, se fizerem sentido.

---

## Funcionalidades Atuais

* API REST funcional para gerenciamento de questões, alternativas e taxonomia
* Estrutura de banco de dados estável com relacionamentos bem definidos
* Camadas de controller, service e repository implementadas
* Persistência em PostgreSQL com migrações Flyway
* Endpoint de health check disponível

---

## Filosofia

> Construir o modelo de dados correto uma única vez.
> Todo o resto pode mudar.

O Studora prioriza:

* clareza em vez de esperteza
* flexibilidade em vez de rigidez
* manutenibilidade de longo prazo em vez de atalhos

---

## Licença

A definir
