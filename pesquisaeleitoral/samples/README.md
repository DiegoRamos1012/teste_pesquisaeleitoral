# Dados de teste (CSV)

Arquivos desta pasta para validar o fluxo de importacao da pesquisa:

- `pesquisa-teste.csv`: CSV pronto para `POST /api/polls/import`.
- `seed-candidatos-teste.sql`: cria os candidatos usados no CSV.

## Ordem sugerida de teste

1. Subir a aplicacao.
2. Sincronizar base de estados/municipios (`POST /api/ibge/sync?force=true`).
3. Executar o SQL de seed para inserir candidatos.
4. Importar `samples/pesquisa-teste.csv` no endpoint `POST /api/polls/import`.

> Observacao: o CSV usa municipios `Campinas` e `Santos` no estado `SP`.

