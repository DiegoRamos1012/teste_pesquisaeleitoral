# Guia de uso do projeto

Este documento mostra como subir a aplicacao e executar o fluxo principal: sincronizar base IBGE e calcular intencao de voto ponderada via importacao de CSV.

## 1) Pre-requisitos

- Java 25 instalado e no `PATH`
- Acesso a internet para consultar APIs do IBGE
- Maven Wrapper do projeto (`mvnw.cmd`)

## 2) Subir a aplicacao

No diretorio do projeto (`pesquisaeleitoral/pesquisaeleitoral`), execute:

```powershell
.\mvnw.cmd spring-boot:run
```

Se a aplicacao subir com sucesso, ela ficara disponivel em `http://localhost:8080`.

## 3) Conferir Swagger

Abra no navegador:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

### Como usar o Swagger na pratica

1. Abra `http://localhost:8080/swagger-ui.html`.
2. Na tag **IBGE**, execute `POST /api/ibge/sync` com `force=true` para carregar a base local.
3. Na tag **Pesquisa**, execute `POST /api/polls/import` e envie o arquivo CSV no campo `file`.
4. Confira no Swagger os exemplos de retorno 200 e os possiveis erros documentados (400/500).

O Swagger agora documenta:

- descricao funcional de cada endpoint;
- parametros esperados (`force` e arquivo `file`);
- modelos de resposta com exemplos (`PollImportResponseDTO`, `IbgeSyncResultDTO`);
- estrutura de erro padrao (`ApiErrorResponseDTO`).

## 4) Passo a passo do fluxo da aplicacao

### Passo 1 - Sincronizar estados e municipios (IBGE)

A sincronizacao pode ocorrer de duas formas:

- Manual, via endpoint:
  - `POST /api/ibge/sync?force=true`
- Automatizada por cron:
  - Propriedade: `ibge.sync.cron=0 0 3 1 * *`
  - Significado: roda todo dia 1 de cada mes, as 03:00 (horario do servidor)

Exemplo PowerShell:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/ibge/sync?force=true"
```

Resposta esperada (exemplo):

```json
{
  "statesCreated": 0,
  "statesUpdated": 0,
  "municipalitiesCreated": 0,
  "forced": true
}
```

### Passo 2 - Garantir candidatos cadastrados

A importacao de pesquisa exige `candidate_id` existente na base (UUID valido).

Hoje o projeto nao expoe endpoint de cadastro de candidato. Entao, antes de importar CSV, garanta que existam registros em `candidate`.

Campos importantes na entidade:

- `id` (UUID)
- `name`
- `political_party`

### Passo 3 - Preparar arquivo CSV da pesquisa

Formato esperado por linha (uma linha por candidato em um municipio):

- `poll_id`
- `poll_date`
- `estado`
- `municipio`
- `candidate_id`
- `percentual`

Cabecalhos aceitos por alias:

- `poll_id`, `id_pesquisa`
- `poll_date`, `data_pesquisa`
- `estado`, `uf`, `state`
- `municipio`, `municipality`
- `candidate_id`, `id_candidato`, `candidato_id`
- `percentual`, `percentage`, `intencao`

Exemplo de CSV:

```csv
poll_id,poll_date,estado,municipio,candidate_id,percentual
PESQ-2026-01,2026-03-01,SP,Sao Paulo,11111111-1111-1111-1111-111111111111,42.3
PESQ-2026-01,2026-03-01,SP,Sao Paulo,22222222-2222-2222-2222-222222222222,37.7
PESQ-2026-01,2026-03-01,RJ,Rio de Janeiro,11111111-1111-1111-1111-111111111111,40.0
PESQ-2026-01,2026-03-01,RJ,Rio de Janeiro,22222222-2222-2222-2222-222222222222,39.0
```

Regras importantes:

- O arquivo deve conter apenas uma pesquisa por importacao (mesmo `poll_id` e `poll_date` em todas as linhas).
- `estado` e `municipio` precisam existir na base sincronizada do IBGE.
- `candidate_id` precisa existir na base local.
- `percentual` deve estar entre 0 e 100.

### Passo 4 - Importar CSV da pesquisa

Endpoint:

- `POST /api/polls/import`
- `Content-Type`: `multipart/form-data`
- Campo do arquivo: `file`

Exemplo PowerShell:

```powershell
$form = @{ file = Get-Item "C:\caminho\pesquisa.csv" }
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/polls/import" -Form $form
```

Resposta esperada (exemplo):

```json
{
  "pollId": "PESQ-2026-01",
  "pollDate": "2026-03-01",
  "weightedPopulation": 12700000,
  "candidates": [
    {
      "candidateId": "11111111-1111-1111-1111-111111111111",
      "candidateName": "Maria Silva",
      "weightedPercentage": 41.82
    },
    {
      "candidateId": "22222222-2222-2222-2222-222222222222",
      "candidateName": "Joao Souza",
      "weightedPercentage": 38.44
    }
  ]
}
```

## 5) Como o calculo ponderado funciona

Para cada linha da pesquisa:

1. Busca a populacao do municipio
2. Calcula peso da linha: `percentual * populacao_municipio / 100`
3. Soma os pesos por candidato
4. Soma a populacao total dos municipios unicos considerados
5. Calcula resultado final por candidato:
   - `resultado = (soma_pesos_candidato / populacao_total) * 100`
6. Ordena candidatos do maior para o menor percentual

Isso permite ponderar o impacto de cada municipio pela sua populacao.

## 6) Erros comuns e como resolver

- `Estado nao encontrado na base`:
  - Execute primeiro `POST /api/ibge/sync?force=true`.
- `Municipio nao encontrado para o estado`:
  - Verifique nome do municipio no CSV e no IBGE (grafia e estado).
- `Candidato nao encontrado`:
  - Confirme se o UUID existe na tabela `candidate`.
- `percentual deve estar entre 0 e 100`:
  - Corrija valores invalidos no CSV.
- `Arquivo deve conter apenas uma pesquisa por importacao`:
  - Garanta um unico `poll_id` e `poll_date` no arquivo.

## 7) Ordem recomendada para operacao

1. Subir aplicacao
2. Sincronizar base IBGE
3. Garantir candidatos na base
4. Importar CSV
5. Analisar resultado ponderado retornado pela API

Com esses passos, o fluxo principal do teste (estimativa de intencao de voto por media ponderada) fica operacional de ponta a ponta.


