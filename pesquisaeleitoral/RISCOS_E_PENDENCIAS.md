# Riscos, possiveis problemas e pendencias

Este documento lista **possiveis problemas atuais** da aplicacao e o que ainda falta para ficar mais completa para o teste pratico.

## 1) Possiveis problemas atuais

## Funcionais

- **Importacao repetida pode duplicar resultados**: hoje a importacao salva `PollResult` por linha e nao ha regra explicita anti-duplicidade por (`poll`, `municipality`, `candidate`).
- **Dependencia forte da base sincronizada**: se estado/municipio/candidato nao estiverem no banco local, a importacao falha.
- **Populacao zerada impacta calculo**: se SIDRA nao retornar dado, municipio pode ficar com `population = 0`, o que pode distorcer a ponderacao.
- **Uma pesquisa por arquivo**: o servico exige mesmo `pollId` e `pollDate` em todas as linhas, o que pode surpreender quem enviar multiplas pesquisas no mesmo CSV.

## Integracao com APIs externas (IBGE/SIDRA)

- **Sem estrategia de retry/backoff**: falhas temporarias de rede podem interromper sincronizacao.
- **Chamadas em alto volume**: buscar populacao por municipio pode gerar muitas requisicoes e tornar a sincronizacao lenta.
- **Dependencia de formato externo**: mudancas no payload das APIs do IBGE/SIDRA podem quebrar parsing.
- **Time zone do scheduler**: cron roda no horario do servidor; sem alinhamento de timezone pode executar fora da janela desejada.

## Dados e persistencia

- **Sem migrations versionadas ainda**: o projeto usa Flyway, mas os testes indicam ausencia de scripts de migracao.
- **Nome da tabela `municipiality`**: ha chance de typo de nomenclatura (se for intencional, ok; se nao, vira debito tecnico).
- **Campo `name` herdado em `PollResult`**: foi preenchido para atender `BaseEntity`, mas modelagem pode merecer revisao futura.

## Qualidade e operacao

- **Cobertura de testes ainda baixa**: ha teste de contexto e controller principal, mas faltam testes de regras de negocio e integracoes criticas.
- **Sem controles de seguranca**: endpoints de sincronizacao/importacao estao sem autenticacao/autorizacao.
- **Observabilidade basica**: faltam metricas, logs estruturados e alertas operacionais.

## 2) O que ainda falta inserir (gap para o desafio)

## Requisitos do desafio ainda pendentes

- **Dashboard temporal**: nao ha frontend/dashboard para evolucao das intencoes de voto ao longo do tempo.
- **Consulta de historico**: faltam endpoints de leitura para listar pesquisas, resultados agregados e serie temporal.
- **Regra de porte explicita (4 grupos)**: hoje ha ponderacao por populacao, mas falta expor claramente o agrupamento por porte (ate 20k, 20k-100k, 100k-1M, acima de 1M) no dominio/relatorios.
- **Fluxo de demonstracao completo**: falta roteiro automatizado fim a fim (sync -> import -> consulta resultados).

## Reforcos tecnicos recomendados

- **Idempotencia de importacao**: criar chave unica de `PollResult` e comportamento de upsert.
- **Migrations Flyway**: versionar schema inicial e evolucoes para reproducibilidade.
- **Validacoes de CSV mais explicitas**: documentar template oficial e mensagens de erro por coluna/linha.
- **Performance de sync**: reduzir chamadas externas com cache, batch, limite de concorrencia e/ou fila.
- **Fallback de populacao**: registrar quando valor veio nulo e permitir reprocessamento posterior.
- **Documentacao de contrato**: adicionar exemplos de request/response no Swagger.

## 3) Proxima priorizacao sugerida

1. **Persistencia confiavel**: migrations + constraints unicas + idempotencia.
2. **Consulta e analise**: endpoints de historico e agregacoes por candidato/tempo/porte.
3. **Dashboard**: tela simples com serie temporal e filtros.
4. **Confiabilidade operacional**: retry, timeout, observabilidade e seguranca.
5. **Teste de entrega**: script de seed/demo e video com fluxo completo.

