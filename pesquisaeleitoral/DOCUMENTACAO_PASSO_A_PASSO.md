# Documentacao passo a passo (fim a fim)

Este guia explica o funcionamento completo do sistema, da primeira interacao ate a validacao final do resultado de uma pesquisa eleitoral.

## 1) O que o sistema faz

A API recebe um CSV de pesquisa eleitoral (por municipio e candidato), cruza com a base territorial (estado + municipio) e calcula o resultado ponderado por populacao.

Fluxo de negocio, em alto nivel:

1. Atualiza a base de estados/municipios via IBGE/SIDRA.
2. Garante candidatos cadastrados no banco.
3. Importa o CSV da pesquisa.
4. Valida regras de negocio.
5. Persiste os dados da pesquisa.
6. Calcula intencao de voto ponderada (total e por grupo de porte).
7. Retorna JSON consolidado para analise.

---

## 2) Primeira interacao: preparar ambiente

No diretorio `pesquisaeleitoral/pesquisaeleitoral`, configure variaveis de banco no PowerShell:

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "5432"
$env:DB_NAME = "pesquisaeleitoral"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "sua_senha"
```

Suba a aplicacao:

```powershell
.\mvnw.cmd spring-boot:run
```

Acesse:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## 3) Etapa obrigatoria: sincronizar base IBGE

Sem a base sincronizada, a importacao pode falhar (estado/municipio nao encontrados).

Execute:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/ibge/sync?force=true"
```

O endpoint `POST /api/ibge/sync`:

- busca estados no IBGE;
- busca municipios por UF;
- busca populacao municipal no SIDRA;
- atualiza a base local.

Resposta tipica:

```json
{
  "statesCreated": 0,
  "statesUpdated": 27,
  "municipalitiesCreated": 0,
  "forced": true
}
```

> Observacao: a sincronizacao tambem pode rodar mensalmente por scheduler (`ibge.sync.cron`).

---

## 4) Cadastrar candidatos (seed)

O CSV usa `candidate_id` (UUID). Esses IDs precisam existir na tabela `candidate`.

Rode a seed:

```powershell
psql -h $env:DB_HOST -p $env:DB_PORT -U $env:DB_USER -d $env:DB_NAME -f ".\samples\seed-candidatos-teste.sql"
```

Se preferir, pode executar o SQL diretamente no client do banco.

---

## 5) Preparar CSV da pesquisa

Cabecalho esperado:

```csv
poll_id,poll_date,estado,municipio,candidate_id,percentual
```

Regras principais:

- uma importacao deve conter apenas uma pesquisa (`poll_id` e `poll_date` unicos no arquivo);
- `estado` e `municipio` devem existir na base sincronizada;
- `candidate_id` deve existir na tabela `candidate`;
- `percentual` deve estar entre 0 e 100;
- use grafia correta do municipio (acentuacao e nome oficial importam).

Arquivo de exemplo no projeto:

- `samples/pesquisa-teste.csv`

---

## 6) Importar CSV

Envie o arquivo para `POST /api/polls/import`:

```powershell
$form = @{ file = Get-Item ".\samples\pesquisa-teste.csv" }
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/polls/import" -Form $form
```

O sistema:

1. faz parse do CSV;
2. valida campos e regras de negocio;
3. resolve estado, municipio e candidato no banco;
4. cria/reaproveita a pesquisa (`poll`);
5. grava linhas em `poll_result`;
6. calcula consolidado ponderado.

---

## 7) Entender o retorno final

Exemplo de resposta:

```json
{
  "pollId": "PESQ-2026-DEMO",
  "pollDate": "2026-03-10",
  "weightedPopulation": 20372310,
  "candidates": [
    {
      "candidateId": "22222222-2222-2222-2222-222222222222",
      "candidateName": "Joao Souza",
      "weightedPercentage": 38.49
    }
  ],
  "groupBreakdown": [
    {
      "stateAcronym": "SP",
      "municipalityGroup": "GROUP_4",
      "population": 11451999,
      "candidates": [
        {
          "candidateId": "22222222-2222-2222-2222-222222222222",
          "candidateName": "Joao Souza",
          "weightedPercentage": 39.10
        }
      ]
    }
  ]
}
```

Significado dos campos:

- `weightedPopulation`: populacao total considerada no calculo;
- `candidates`: ranking geral ponderado da pesquisa;
- `groupBreakdown`: detalhamento por estado e por porte de municipio.

Grupos de porte (`MunicipalitySizeGroup`):

- `GROUP_1`: ate 20 mil habitantes;
- `GROUP_2`: 20 mil ate 100 mil;
- `GROUP_3`: 100 mil ate 1 milhao;
- `GROUP_4`: acima de 1 milhao.

---

## 8) Como conferir no banco de dados

Tabelas principais:

- `state`: estados;
- `municipality`: municipios (com populacao);
- `candidate`: candidatos;
- `poll`: cabecalho da pesquisa;
- `poll_result`: linhas por municipio + candidato.

Cada linha de `poll_result` representa a intencao de voto de 1 candidato em 1 municipio para aquela pesquisa.

---

## 9) Erros comuns e como resolver

1. `Estado nao encontrado na base`
   - rode `POST /api/ibge/sync?force=true`.

2. `Municipio nao encontrado para o estado ...`
   - ajuste grafia/acentuacao no CSV para o nome oficial do IBGE.

3. `Candidato nao encontrado`
   - rode a seed ou confira UUID na tabela `candidate`.

4. `Arquivo deve conter apenas uma pesquisa por importacao`
   - mantenha um unico `poll_id` e `poll_date` por arquivo.

5. Sync do IBGE muito lento
   - em geral e esperado em primeira carga (muitas chamadas externas), principalmente com consulta de populacao municipio a municipio.

---

## 10) Validacao final (checklist rapido)

- [ ] Aplicacao sobe sem erro.
- [ ] `POST /api/ibge/sync?force=true` concluido.
- [ ] Candidatos presentes na tabela `candidate`.
- [ ] CSV valido importado com sucesso.
- [ ] JSON retornado com `candidates` e `groupBreakdown`.
- [ ] Resultado coerente com os dados de entrada.

Com isso, o fluxo completo (primeira interacao ate resultado final) fica operacional e auditavel.

