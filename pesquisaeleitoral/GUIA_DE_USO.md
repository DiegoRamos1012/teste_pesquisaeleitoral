# Guia de uso do projeto

Este documento mostra como subir a aplicação e executar o fluxo principal: sincronizar base IBGE e calcular intenção de voto ponderada via importação de CSV.

## 1) Pré-requisitos

- Java 25 instalado e no `PATH`
- Acesso à internet para consultar APIs do IBGE
- Maven Wrapper do projeto (`mvnw.cmd`)

## 2) Subir a aplicação

No diretório do projeto (`pesquisaeleitoral/pesquisaeleitoral`), execute:

```powershell
.\mvnw.cmd spring-boot:run
```

Se a aplicação subir com sucesso, ela ficará disponível em `http://localhost:8080`.

## 3) Conferir Swagger

Abra no navegador:

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

### Como usar o Swagger na prática

1. Abra `http://localhost:8080/swagger-ui.html`.
2. Na tag **IBGE**, execute `POST /api/ibge/sync` com `force=true` para carregar a base local.
3. Na tag **Pesquisa**, execute `POST /api/polls/import` e envie o arquivo CSV no campo `file`.
4. Confira no Swagger os exemplos de retorno 200 e os possíveis erros documentados (400/500).

O Swagger agora documenta:

- descrição funcional de cada endpoint;
- parâmetros esperados (`force` e arquivo `file`);
- modelos de resposta com exemplos (`PollImportResponseDTO`, `IbgeSyncResultDTO`);
- estrutura de erro padrão (`ApiErrorResponseDTO`).

## 4) Passo a passo do fluxo da aplicação

### Passo 1 - Sincronizar estados e municípios (IBGE)

A sincronização pode ocorrer de duas formas:

- Manual, via endpoint:
  - `POST /api/ibge/sync?force=true`
- Automatizada por cron:
  - Propriedade: `ibge.sync.cron=0 0 3 1 * *`
  - Significado: roda todo dia 1 de cada mês, às 03:00 (horário do servidor)

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

A importação de pesquisa exige `candidate_id` existente na base (UUID válido).

Hoje o projeto não expõe endpoint de cadastro de candidato. Então, antes de importar CSV, garanta que existam registros em `candidate`.

Campos importantes na entidade:

- `id` (UUID)
- `name`
- `political_party`

### Passo 3 - Preparar arquivo CSV da pesquisa

Formato esperado por linha (uma linha por candidato em um município):

- `poll_id`
- `poll_date`
- `estado`
- `municipio`
- `candidate_id`
- `percentual`

Cabeçalhos aceitos por alias:

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

- O arquivo deve conter apenas uma pesquisa por importação (mesmo `poll_id` e `poll_date` em todas as linhas).
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

## 5) Como o cálculo ponderado funciona

Para cada linha da pesquisa:

1. Busca a população do município
2. Calcula peso da linha: `percentual * populacao_municipio / 100`
3. Soma os pesos por candidato
4. Soma a população total dos municípios únicos considerados
5. Calcula resultado final por candidato:
   - `resultado = (soma_pesos_candidato / populacao_total) * 100`
6. Ordena candidatos do maior para o menor percentual

Isso permite ponderar o impacto de cada município pela sua população.

## 6) Erros comuns e como resolver

- `Estado nao encontrado na base`:
  - Execute primeiro `POST /api/ibge/sync?force=true`.
- `Municipio nao encontrado para o estado`:
  - Verifique nome do municipio no CSV e no IBGE (grafia e estado).
- `Candidato nao encontrado`:
  - Confirme se o UUID existe na tabela `candidate`.
- `percentual deve estar entre 0 e 100`:
  - Corrija valores inválidos no CSV.
- `Arquivo deve conter apenas uma pesquisa por importacao`:
  - Garanta um único `poll_id` e `poll_date` no arquivo.

## 7) Ordem recomendada para operação

1. Subir aplicação
2. Sincronizar base IBGE
3. Garantir candidatos na base
4. Importar CSV
5. Analisar resultado ponderado retornado pela API

Com esses passos, o fluxo principal do teste (estimativa de intenção de voto por média ponderada) fica operacional de ponta a ponta.


