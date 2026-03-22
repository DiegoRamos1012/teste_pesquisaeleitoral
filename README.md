# 📊 Pesquisa Eleitoral API

Este projeto foi desenvolvido para o processo seletivo de desenvolvedor júnior da empresa **Konatus**. Repositório do dashboard para análise dos dados: https://github.com/DiegoRamos1012/dashboard-pesquisa-eleitoral.

---

## 🚀 Sobre o projeto

API Spring Boot para:

* Sincronizar estados e municípios (IBGE/SIDRA)
* Importar CSV de pesquisa eleitoral
* Calcular intenção de voto ponderada por população, com detalhamento por porte municipal

---

## 🛠️ Stack

* Java 25
* Spring Boot 3.5.11
* Spring Data JPA
* Flyway
* PostgreSQL (perfil local) ou H2
* OpenCSV
* Swagger (`/swagger-ui.html`)

---

## 🧱 Estrutura do código

Pacotes principais em:

```
src/main/java/com/diegoramos/konatus/pesquisaeleitoral
```

* **controller**: endpoints REST (`/api/ibge/sync`, `/api/polls/import`)
* **service/ibge**: sincronização de estados, municípios e população
* **service/poll**: importação CSV, validações, persistência e cálculo ponderado
* **domain**: entidades JPA (State, Municipality, Candidate, Poll, PollResult)
* **repository**: acesso ao banco
* **dto**: contratos de request/response
* **exceptions**: tratamento global de erros

---

## 🔗 Endpoints principais

* `POST /api/ibge/sync?force=true|false` → sincroniza base territorial
* `POST /api/polls/import` → importa CSV (`multipart/form-data`, campo `file`)

---

## 📄 Documentação da API

* http://localhost:8080/swagger-ui.html
* http://localhost:8080/v3/api-docs

---

## 📁 Arquivos de teste

* `samples/pesquisa-teste.csv` → massa de teste rápida
* `samples/pesquisa-teste-v2-balanceado.csv` → massa mais completa e equilibrada
* `samples/seed-candidatos-teste.sql` → seed de 5 candidatos

---

## ▶️ Como executar localmente (sem Docker)

### 1. Pré-requisitos

* Java 25 no PATH
* PostgreSQL rodando
* Banco criado com nome igual a `DB_NAME`

---

### 2. Configurar variáveis (PowerShell)

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "pesquisaeleitoral"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "sua_senha"
```

---

### 3. Subir a aplicação

No diretório:

```
pesquisaeleitoral/pesquisaeleitoral
```

```bash
.\mvnw.cmd spring-boot:run
```

---

### 4. Sincronizar base IBGE

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/ibge/sync?force=true"
```

---

### 5. Inserir candidatos de teste

```bash
psql -h $env:DB_HOST -p $env:DB_PORT -U $env:DB_USER -d $env:DB_NAME -f ".\samples\seed-candidatos-teste.sql"
```

---

### 6. Importar CSV de teste

```powershell
$form = @{ file = Get-Item ".\samples\pesquisa-teste.csv" }

Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8080/api/polls/import" `
  -Form $form
```

---

## 📑 Formato CSV esperado

```csv
poll_id,poll_date,estado,municipio,candidate_id,percentual
```

### Regras

* Uma importação aceita apenas uma pesquisa (poll_id e poll_date únicos)
* Estado e município precisam existir na base sincronizada
* `candidate_id` precisa existir na tabela `candidate`
* `percentual` deve estar entre 0 e 100

---

## ⚠️ Erros comuns

* **Estado não encontrado** → execute sync do IBGE
* **Município não encontrado** → ajuste grafia/UF no CSV
* **Candidato não encontrado** → rode o seed SQL e valide UUIDs
* **Erro de autenticação PostgreSQL** → revise `DB_USER` e `DB_PASSWORD`

---

## 🐳 Docker

Para executar com containers (API + PostgreSQL), consulte:

```
DOCKER.md
```

---

## 🧪 Testes

### Testes unitários

* **PollCsvParserTest** → valida cabeçalhos do CSV, parsing de data e percentual inválido
* **IbgeServiceTest** → valida sincronização e idempotência
* **PollWeightAccumulatorTest** → valida cálculo da média ponderada

---

### Outros testes

* `PollImportServiceIntegrationTest` (integração)
* `PollImportControllerTest` (controller)
* `PesquisaeleitoralApplicationTests` (context load)

---

## ▶️ Executar testes

### Apenas unitários

```bash
.\mvnw.cmd "-Dtest=PollCsvParserTest,IbgeServiceTest,PollWeightAccumulatorTest" test
```

### Suíte completa

```bash
.\mvnw.cmd test
```
