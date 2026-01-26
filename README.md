# Studora

Studora é uma plataforma de estudos **em desenvolvimento (WIP)** focada na construção de um **banco de questões estruturado e de alta qualidade** para concursos públicos.

O projeto é guiado por uma abordagem **content-first**: correção, estrutura e escalabilidade vêm antes de interface, gamificação ou métricas artificiais.

---

## Status do Projeto

**Fase 1 — API e Modelo de Dados (atual)**

* Definição e estabilização do schema do banco de dados
* Construção da API para questões, alternativas, taxonomia e respostas

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
* Schema definido em `schema.sql`
* Chaves estrangeiras habilitadas explicitamente

O schema é propositalmente conservador e evita restrições prematuras.

---

## O que Este Projeto *Ainda Não É*

* Não possui interface gráfica
* Não possui ranking ou gamificação
* Não implementa regras oficiais de pontuação
* Não possui gerenciamento de usuários

Esses pontos poderão ser avaliados no futuro, se fizerem sentido.

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
