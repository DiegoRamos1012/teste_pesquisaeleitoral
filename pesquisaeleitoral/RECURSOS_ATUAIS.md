# Recursos atuais da aplicação

Este documento resume o que a aplicação possui atualmente em termos de funcionalidades, endpoints e componentes principais.

## Visão geral

A aplicação é uma API Spring Boot para:
- sincronizar estados e municípios (com população) a partir de bases do IBGE;
- importar arquivo CSV de pesquisa eleitoral;
- calcular intenção de voto ponderada por população dos municípios;
- expor documentação OpenAPI/Swagger.

## Stack atual

- Java 25
- Spring Boot 3.5.11
- Spring Web
- Spring Data JPA
- Flyway
- H2/PostgreSQL
- OpenCSV
- Springdoc OpenAPI (Swagger UI)

## Recursos implementados

### 1) Sincronização de estados e municípios (IBGE)

**Serviço principal:** `IbgeService`

- Busca estados na API de localidades do IBGE.
- Busca municípios por UF.
- Atualiza/cria estados na base local.
- Atualiza/cria municípios na base local.
- Busca população estimada por município via SIDRA/IBGE.
- Atualiza o campo de população dos municípios sincronizados.

**Agendamento mensal:** `IbgeSyncScheduler`
- Executa sincronização automaticamente via cron configurável.
- Cron atual em `application.properties`: `0 0 3 1 * *`.

**Como ler esse cron (`segundo minuto hora dia-do-mes mes dia-da-semana`):**
- `0` (segundo): dispara no segundo 0;
- `0` (minuto): dispara no minuto 0;
- `3` (hora): dispara às 03:00;
- `1` (dia-do-mes): apenas no dia 1 de cada mês;
- `*` (mes): qualquer mês;
- `*` (dia-da-semana): qualquer dia da semana.

**Resumo prático:** a sincronização roda **uma vez por mês, no dia 1, às 03:00** (horário do servidor onde a aplicação estiver rodando).

**Observação de operação:**
- em homologação/produção, valide timezone do servidor (ex.: UTC vs America/Sao_Paulo) para evitar execução em horário inesperado;
- se precisar alterar a janela, basta trocar `ibge.sync.cron` no `application.properties` ou via variável de ambiente;
- para execução imediata/manual, use o endpoint `POST /api/ibge/sync?force=true`.

**Disparo manual:** `IbgeSyncController`
- Endpoint: `POST /api/ibge/sync?force=false|true`
- Retorna contadores da sincronização:
  - `statesCreated`
  - `statesUpdated`
  - `municipalitiesCreated`
  - `forced`

### 2) Importação de pesquisa eleitoral via CSV

**Controller:** `PollImportController`
- Endpoint: `POST /api/polls/import`
- Tipo de consumo: `multipart/form-data`
- Campo esperado: `file`

**Serviço:** `PollImportService`

Fluxo atual:
- valida arquivo vazio;
- lê CSV em UTF-8;
- mapeia colunas por cabeçalho (com aliases);
- valida linhas e tipos (UUID, percentual, data, obrigatoriedade);
- valida que o arquivo contém apenas uma pesquisa (mesmo `pollId` e `pollDate`);
- resolve estado/municipio/candidato na base local;
- cria/reaproveita `Poll`;
- persiste `PollResult` por linha;
- calcula percentual ponderado por população municipal;
- retorna resultado ordenado por maior percentual.

**Resultado da importação:** `PollImportResponseDTO`
- `pollId`
- `pollDate`
- `weightedPopulation`
- `candidates[]` contendo:
  - `candidateId`
  - `candidateName`
  - `weightedPercentage`

### 3) Swagger / OpenAPI

- Configuração: `OpenApiConfig`
- Metadados da API definidos (título, descrição, versão, contato).
- Endpoints de documentação:
  - JSON OpenAPI: `/v3/api-docs`
  - Swagger UI: `/swagger-ui.html`

## Entidades de domínio presentes

- `State`
- `Municipality`
- `Candidate`
- `Poll`
- `PollResult`
- `BaseEntity` (id, name, auditoria)

## Repositories existentes

- `StateRepository`
- `MunicipalityRepository`
- `CandidateRepository`
- `PollRepository`
- `PollResultRepository`

## Tratamento de erros

A aplicação possui `GlobalExceptionHandler` com tratamento para:
- `BusinessException` (regra de negócio);
- erros de validação;
- erros de integridade de dados;
- erros de acesso a banco;
- fallback para exceções não tratadas.

## Configurações relevantes (application.properties)

- `ibge.api.base-url`
- `ibge.sidra.base-url`
- `ibge.sync.cron`
- `springdoc.api-docs.path`
- `springdoc.swagger-ui.path`

## Observações atuais

- A API já está preparada para sincronizar base territorial e populacional e calcular ponderação por população.
- O foco atual está em importação e cálculo; dashboard web ainda não foi implementado neste backend.
- O projeto possui testes de contexto e teste de controller para o endpoint de importação.


