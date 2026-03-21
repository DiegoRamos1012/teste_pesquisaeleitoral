# Pesquisa Eleitoral API

API Spring Boot para:
- sincronizar estados e municipios (IBGE/SIDRA);
- importar CSV de pesquisa eleitoral;
- calcular intencao de voto ponderada por populacao.

## Stack

- Java 25
- Spring Boot 3.5.11
- Spring Data JPA
- Flyway
- PostgreSQL (perfil `local`) ou H2
- OpenCSV
- Swagger (`/swagger-ui.html`)

## Arquivos de teste ja prontos

- `samples/pesquisa-teste.csv`
- `samples/seed-candidatos-teste.sql`

## Passo a passo (ambiente local)

### 1) Pre-requisitos

- Java 25 no `PATH`
- PostgreSQL rodando
- Banco criado (nome igual ao valor de `DB_NAME`)

### 2) Configurar variaveis de ambiente (PowerShell)

> O perfil `local` usa `application-local.properties` e espera variaveis `DB_*`.

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "pesquisaeleitoral"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "sua_senha"
```

### 3) Subir a aplicacao

No diretorio `pesquisaeleitoral/pesquisaeleitoral`:

```powershell
.\mvnw.cmd spring-boot:run
```

API e docs:
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs`

### 4) Sincronizar base territorial do IBGE

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/ibge/sync?force=true"
```

Resposta esperada: objeto JSON com campos como `statesCreated`, `statesUpdated`, `municipalitiesCreated` e `forced`.

### 5) Inserir candidatos de teste

Use o SQL em `samples/seed-candidatos-teste.sql` no mesmo banco da aplicacao.

Exemplo com `psql`:

```powershell
psql -h $env:DB_HOST -p $env:DB_PORT -U $env:DB_USER -d $env:DB_NAME -f ".\samples\seed-candidatos-teste.sql"
```

### 6) Importar o CSV de teste

```powershell
$form = @{ file = Get-Item ".\samples\pesquisa-teste.csv" }
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/polls/import" -Form $form
```

Resposta esperada (estrutura):
- `pollId`
- `pollDate`
- `weightedPopulation`
- `candidates[]` com `candidateId`, `candidateName`, `weightedPercentage`

## Formato CSV esperado

Cabecalho base:

```csv
poll_id,poll_date,estado,municipio,candidate_id,percentual
```

Regras importantes:
- o arquivo deve conter apenas uma pesquisa (`poll_id` e `poll_date` iguais em todas as linhas);
- `estado` e `municipio` devem existir na base sincronizada;
- `candidate_id` deve existir na tabela `candidate`;
- `percentual` deve estar entre `0` e `100`.

## Testes automatizados (rapido)

```powershell
.\mvnw.cmd "-Dtest=PollImportServiceIntegrationTest,PollImportControllerTest" test
```

## Erros comuns

- `Estado nao encontrado na base`: rode `POST /api/ibge/sync?force=true`.
- `Municipio nao encontrado para o estado`: revise nome do municipio e UF no CSV.
- `Candidato nao encontrado`: rode o seed SQL e confira os UUIDs do CSV.
- erro de autenticacao PostgreSQL: revise `DB_USER`/`DB_PASSWORD`.

## Documentacao complementar

- `DOCUMENTACAO_PASSO_A_PASSO.md`: fluxo completo do sistema (do setup ao resultado final)
- `GUIA_DE_USO.md`: guia detalhado do fluxo
- `RECURSOS_ATUAIS.md`: capacidades implementadas
- `RISCOS_E_PENDENCIAS.md`: riscos e pendencias

