# Dados de teste (CSV)

Arquivos desta pasta para validar o fluxo de importacao da pesquisa:

- `pesquisa-teste.csv`: CSV base para teste rapido de importacao.
- `pesquisa-teste-v2-balanceado.csv`: CSV maior, com cobertura explicita dos 4 grupos de porte e 5 candidatos por municipio.
- `seed-candidatos-teste.sql`: cria os candidatos usados nos CSVs.

## Ordem sugerida de teste

1. Subir a aplicacao.
2. Sincronizar base de estados/municipios (`POST /api/ibge/sync?force=true`).
3. Executar o SQL de seed para inserir candidatos.
4. Importar `samples/pesquisa-teste.csv` (teste rapido) ou `samples/pesquisa-teste-v2-balanceado.csv` (teste completo).

## Observacoes

- Ambos os CSVs usam uma unica pesquisa por arquivo (um `poll_id` e um `poll_date`).
- Em cada municipio, os percentuais dos 5 candidatos fecham em `100.00`.
- O CSV balanceado depende de nomes oficiais de municipios sincronizados com o IBGE.
