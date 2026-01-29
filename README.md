# Studora

Studora é uma plataforma de estudos **em desenvolvimento (WIP)** focada na construção de um **banco de questões estruturado e de alta qualidade** para concursos públicos.

O projeto é guiado por uma abordagem **content-first**: correção, estrutura e escalabilidade vêm antes de interface, gamificação ou métricas artificiais.

---

## Tecnologias Utilizadas

* **Linguagem**: Java 17+
* **Framework**: Spring Boot 3.x
* **Banco de Dados**: SQLite
* **Build Tool**: Maven
* **Arquitetura**: REST API com padrão MVC

---

## Setup do Projeto

### Pré-requisitos

* Java 17 ou superior
* Maven 3.6 ou superior

### Execução Local

1. Clone o repositório:
   ```bash
   git clone <url-do-repositorio>
   cd studora-back
   ```

2. Compile e execute o projeto:
   ```bash
   mvn spring-boot:run
   ```

3. A aplicação estará disponível em: `http://localhost:4534`

### Build do Projeto

Para construir o JAR executável:
```bash
mvn clean package
```

Para executar o JAR gerado:
```bash
java -jar target/studora-<versao>.jar
```

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

* Banco atual: **SQLite** (simples, portátil e suficiente para as fases iniciais)
* Schema definido em `src/main/resources/db/schema.sql`
* Arquivo de banco de dados localizado em `db/studora.db`
* Chaves estrangeiras habilitadas explicitamente

O schema é propositalmente conservador e evita restrições prematuras.

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
* Configuração de banco de dados SQLite funcional
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
