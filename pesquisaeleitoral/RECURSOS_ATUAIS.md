# Recursos atuais da aplicacao

Este documento resume o que a aplicacao possui atualmente em termos de funcionalidades, endpoints e componentes principais.

## Visao geral

A aplicacao e uma API Spring Boot para:
- sincronizar estados e municipios (com populacao) a partir de bases do IBGE;
- importar arquivo CSV de pesquisa eleitoral;
- calcular intencao de voto ponderada por populacao dos municipios;
- expor documentacao OpenAPI/Swagger.

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

### 1) Sincronizacao de estados e municipios (IBGE)

**Servico principal:** `IbgeService`

- Busca estados na API de localidades do IBGE.
- Busca municipios por UF.
- Atualiza/cria estados na base local.
- Atualiza/cria municipios na base local.
- Busca populacao estimada por municipio via SIDRA/IBGE.
- Atualiza o campo de populacao dos municipios sincronizados.

**Agendamento mensal:** `IbgeSyncScheduler`
- Executa sincronizacao automaticamente via cron configuravel.
- Cron atual em `application.properties`: `0 0 3 1 * *`.

**Como ler esse cron (`segundo minuto hora dia-do-mes mes dia-da-semana`):**
- `0` (segundo): dispara no segundo 0;
- `0` (minuto): dispara no minuto 0;
- `3` (hora): dispara as 03:00;
- `1` (dia-do-mes): apenas no dia 1 de cada mes;
- `*` (mes): qualquer mes;
- `*` (dia-da-semana): qualquer dia da semana.

**Resumo pratico:** a sincronizacao roda **uma vez por mes, no dia 1, as 03:00** (horario do servidor onde a aplicacao estiver rodando).

**Observacao de operacao:**
- em homologacao/producao, valide timezone do servidor (ex.: UTC vs America/Sao_Paulo) para evitar execucao em horario inesperado;
- se precisar alterar a janela, basta trocar `ibge.sync.cron` no `application.properties` ou via variavel de ambiente;
- para execucao imediata/manual, use o endpoint `POST /api/ibge/sync?force=true`.

**Disparo manual:** `IbgeSyncController`
- Endpoint: `POST /api/ibge/sync?force=false|true`
- Retorna contadores da sincronizacao:
  - `statesCreated`
  - `statesUpdated`
  - `municipalitiesCreated`
  - `forced`

### 2) Importacao de pesquisa eleitoral via CSV

**Controller:** `PollImportController`
- Endpoint: `POST /api/polls/import`
- Tipo de consumo: `multipart/form-data`
- Campo esperado: `file`

**Servico:** `PollImportService`

Fluxo atual:
- valida arquivo vazio;
- le CSV em UTF-8;
- mapeia colunas por cabecalho (com aliases);
- valida linhas e tipos (UUID, percentual, data, obrigatoriedade);
- valida que o arquivo contem apenas uma pesquisa (mesmo `pollId` e `pollDate`);
- resolve estado/municipio/candidato na base local;
- cria/reaproveita `Poll`;
- persiste `PollResult` por linha;
- calcula percentual ponderado por populacao municipal;
- retorna resultado ordenado por maior percentual.

**Resultado da importacao:** `PollImportResponseDTO`
- `pollId`
- `pollDate`
- `weightedPopulation`
- `candidates[]` contendo:
  - `candidateId`
  - `candidateName`
  - `weightedPercentage`

### 3) Swagger / OpenAPI

- Configuracao: `OpenApiConfig`
- Metadados da API definidos (titulo, descricao, versao, contato).
- Endpoints de documentacao:
  - JSON OpenAPI: `/v3/api-docs`
  - Swagger UI: `/swagger-ui.html`

## Entidades de dominio presentes

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

A aplicacao possui `GlobalExceptionHandler` com tratamento para:
- `BusinessException` (regra de negocio);
- erros de validacao;
- erros de integridade de dados;
- erros de acesso a banco;
- fallback para excecoes nao tratadas.

## Configuracoes relevantes (application.properties)

- `ibge.api.base-url`
- `ibge.sidra.base-url`
- `ibge.sync.cron`
- `springdoc.api-docs.path`
- `springdoc.swagger-ui.path`

## Observacoes atuais

- A API ja esta preparada para sincronizar base territorial e populacional e calcular ponderacao por populacao.
- O foco atual esta em importacao e calculo; dashboard web ainda nao foi implementado neste backend.
- O projeto possui testes de contexto e teste de controller para o endpoint de importacao.


