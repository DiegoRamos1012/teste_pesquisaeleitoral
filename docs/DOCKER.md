# Docker - como executar

Este guia cobre a execução da API com Docker usando os arquivos:

- `docker-compose.yml` (raiz do workspace)
- `pesquisaeleitoral/Dockerfile`
- `pesquisaeleitoral/.dockerignore`

## Serviços

`docker-compose.yml` sobe 2 containers:

- `postgres` (`pesquisa_db`): banco PostgreSQL 17
- `api` (`pesquisa_api`): backend Spring Boot

Persistência:
- volume `postgres_data` armazena os dados do banco

Rede:
- `pesquisa_network` conecta API e banco

## Subir ambiente

No diretório `C:\Users\Windows\IdeaProjects\pesquisaeleitoral`:

```powershell
docker-compose up --build -d
```

Verificar status:

```powershell
docker-compose ps
```

Acessos:
- Swagger: `http://localhost:8080/swagger-ui.html`
- OpenAPI: `http://localhost:8080/v3/api-docs`

## Fluxo de teste após subir

### 1) Sync IBGE

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/ibge/sync?force=true"
```

### 2) Seed de candidatos no container PostgreSQL

```powershell
$sql = Get-Content "C:\Users\Windows\IdeaProjects\pesquisaeleitoral\pesquisaeleitoral\samples\seed-candidatos-teste.sql" -Raw
docker exec -i pesquisa_db psql -U postgres -d pesquisaeleitoral -c $sql
```

### 3) Importar CSV

```powershell
$form = @{ file = Get-Item "C:\Users\Windows\IdeaProjects\pesquisaeleitoral\pesquisaeleitoral\samples\pesquisa-teste.csv" }
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/polls/import" -Form $form
```

## Parar ambiente

```powershell
docker-compose down
```

Para remover também os dados persistidos:

```powershell
docker-compose down -v
```

## Logs

```powershell
docker-compose logs -f api
docker-compose logs -f postgres
```

## Variáveis principais

No serviço `api` (compose):
- `DB_HOST=postgres`
- `DB_PORT=5432`
- `DB_NAME=pesquisaeleitoral`
- `DB_USER=postgres`
- `DB_PASSWORD=postgres`
- `SPRING_PROFILES_ACTIVE=local`

## Troubleshooting rápido

- `No static resource actuator/health`: healthcheck antigo; o atual usa `/swagger-ui.html`.
- `Port 8080 already in use`: altere para `"9090:8080"` em `docker-compose.yml`.
- API não conecta no banco: valide `docker-compose ps` e aguarde `postgres` ficar `healthy`.
