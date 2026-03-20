# Riscos, possíveis problemas e pendências

Este documento lista **possíveis problemas atuais** da aplicação e o que ainda falta para ficar mais completa para o teste prático.

## 1) Possíveis problemas atuais

## Funcionais

- **Importação repetida pode duplicar resultados**: hoje a importação salva `PollResult` por linha e não há regra explícita anti-duplicidade por (`poll`, `municipality`, `candidate`).
- **Dependência forte da base sincronizada**: se estado/municipio/candidato não estiverem no banco local, a importação falha.
- **População zerada impacta cálculo**: se SIDRA não retornar dado, município pode ficar com `population = 0`, o que pode distorcer a ponderação.
- **Uma pesquisa por arquivo**: o serviço exige mesmo `pollId` e `pollDate` em todas as linhas, o que pode surpreender quem enviar múltiplas pesquisas no mesmo CSV.

## Integração com APIs externas (IBGE/SIDRA)

- **Sem estratégia de retry/backoff**: falhas temporárias de rede podem interromper sincronização.
- **Chamadas em alto volume**: buscar população por municipio pode gerar muitas requisições e tornar a sincronização lenta.
- **Dependência de formato externo**: mudanças no payload das APIs do IBGE/SIDRA podem quebrar parsing.
- **Time zone do scheduler**: cron roda no horário do servidor; sem alinhamento de timezone pode executar fora da janela desejada.

## Dados e persistência

- **Sem migrations versionadas ainda**: o projeto usa Flyway, mas os testes indicam ausência de scripts de migração.
- **Nome da tabela `municipiality`**: há chance de typo de nomenclatura (se for intencional, ok; se não, vira débito técnico).
- **Campo `name` herdado em `PollResult`**: foi preenchido para atender `BaseEntity`, mas modelagem pode merecer revisão futura.

## Qualidade e operação

- **Cobertura de testes ainda baixa**: há teste de contexto e controller principal, mas faltam testes de regras de negócio e integrações críticas.
- **Sem controles de segurança**: endpoints de sincronização/importação estão sem autenticação/autorização.
- **Observabilidade básica**: faltam métricas, logs estruturados e alertas operacionais.

## 2) O que ainda falta inserir (gap para o desafio)

## Requisitos do desafio ainda pendentes

- **Dashboard temporal**: não há frontend/dashboard para evolução das intenções de voto ao longo do tempo.
- **Consulta de histórico**: faltam endpoints de leitura para listar pesquisas, resultados agregados e série temporal.
- **Regra de porte explícita (4 grupos)**: hoje há ponderação por população, mas falta expor claramente o agrupamento por porte (até 20k, 20k-100k, 100k-1M, acima de 1M) no domínio/relatórios.
- **Fluxo de demonstração completo**: falta roteiro automatizado fim a fim (sync -> import -> consulta resultados).

## Reforços técnicos recomendados

- **Idempotência de importação**: criar chave única de `PollResult` e comportamento de upsert.
- **Migrations Flyway**: versionar schema inicial e evoluções para reprodutibilidade.
- **Validações de CSV mais explícitas**: documentar template oficial e mensagens de erro por coluna/linha.
- **Performance de sync**: reduzir chamadas externas com cache, batch, limite de concorrência e/ou fila.
- **Fallback de população**: registrar quando valor veio nulo e permitir reprocessamento posterior.
- **Documentação de contrato**: adicionar exemplos de request/response no Swagger.

## 3) Próxima priorização sugerida

1. **Persistência confiável**: migrations + constraints únicas + idempotência.
2. **Consulta e análise**: endpoints de histórico e agregações por candidato/tempo/porte.
3. **Dashboard**: tela simples com série temporal e filtros.
4. **Confiabilidade operacional**: retry, timeout, observabilidade e segurança.
5. **Teste de entrega**: script de seed/demo e vídeo com fluxo completo.

