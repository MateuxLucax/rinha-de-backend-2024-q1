# Rinha de Backend - 2024 - Q1

![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Quarkus](https://img.shields.io/badge/quarkus-%234794EB.svg?style=for-the-badge&logo=quarkus&logoColor=white)
![Postgres](https://img.shields.io/badge/postgres-%23316192.svg?style=for-the-badge&logo=postgresql&logoColor=white)
![Nginx](https://img.shields.io/badge/nginx-%23009639.svg?style=for-the-badge&logo=nginx&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
![TypeScript](https://img.shields.io/badge/typescript-%23007ACC.svg?style=for-the-badge&logo=typescript&logoColor=white)
![Bun](https://img.shields.io/badge/Bun-%23000000.svg?style=for-the-badge&logo=bun&logoColor=white)
![Dart](https://img.shields.io/badge/dart-%230175C2.svg?style=for-the-badge&logo=dart&logoColor=white)

You can find details about the main competition here: [zanfranceschi/rinha-de-backend-2024-q1](https://github.com/zanfranceschi/rinha-de-backend-2024-q1)

## My results

| participante | multa SLA (> 249ms) | multa SLA (inconsistência saldo) | multa total | valor a receber | relatório |
| --           | --                  | --                               | --          | --              | --        |
| [mateuxlucax-dart](./participantes/mateuxlucax-dart) | USD 0.0 | USD 0.0 | USD 0.0 | **USD 100000.0** | [link](resultados/mateuxlucax-dart/rinhabackendcrebitossimulation-20240311020848436) |
| [mateuxlucax-quarkus](./participantes/mateuxlucax-quarkus) | USD 0.0 | USD 0.0 | USD 0.0 | **USD 100000.0** | [link](resultados/mateuxlucax-quarkus/rinhabackendcrebitossimulation-20240311021325158) |

### Kotlin + Quarkus + Postgres

| Requests     | Executions | Response Time (ms) |            |        |           |           |           |           |           |           |           |           |
|--------------|------------|--------------------|------------|--------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|
|              | Total      | OK                 | KO         | % KO   | Cnt/s     | Min       | 50th pct  | 75th pct  | 95th pct  | 99th pct  | Max       | Mean      | Std Dev   |
| All Requests | 61503      | 61503              | 0          | 0%     | 251.033   | 0         | 1         | 1         | 2         | 2         | 123       | 1         | 1         |
| créditos     | 19860      | 19860              | 0          | 0%     | 81.061    | 0         | 1         | 1         | 2         | 2         | 12        | 1         | 1         |
| débitos      | 39660      | 39660              | 0          | 0%     | 161.878   | 0         | 1         | 1         | 2         | 2         | 21        | 1         | 1         |
| extratos     | 1860       | 1860               | 0          | 0%     | 7.592     | 0         | 1         | 2         | 2         | 5         | 7         | 1         | 1         |
| validações   | 123        | 123                | 0          | 0%     | 0.502     | 1         | 9         | 17        | 78        | 106       | 123       | 17        | 25        |

### Dart + Postgres

| Requests     | Executions | Response Time (ms) |            |        |           |           |           |           |           |           |           |           |
|--------------|------------|--------------------|------------|--------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|-----------|
|              | Total      | OK                 | KO         | % KO   | Cnt/s     | Min       | 50th pct  | 75th pct  | 95th pct  | 99th pct  | Max       | Mean      | Std Dev   |
| All Requests | 61503      | 61503              | 0          | 0%     | 251.033   | 0         | 1         | 2         | 5         | 48        | 249       | 3         | 10        |
| créditos     | 19860      | 19860              | 0          | 0%     | 81.061    | 0         | 1         | 4         | 5         | 48        | 214       | 3         | 10        |
| débitos      | 39660      | 39660              | 0          | 0%     | 161.878   | 0         | 1         | 2         | 5         | 47        | 249       | 3         | 10        |
| extratos     | 1860       | 1860               | 0          | 0%     | 7.592     | 1         | 3         | 5         | 6         | 9         | 121       | 3         | 5         |
| validações   | 123        | 123                | 0          | 0%     | 0.502     | 0         | 9         | 19        | 60        | 73        | 74        | 16        | 19        |
