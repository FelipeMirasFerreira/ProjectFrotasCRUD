# Controle de Frota

Sistema de controle de frota com **veículos, motoristas, abastecimentos e manutenções**, e com **alerta de revisão por quilometragem**.

Foi construído a partir do projeto de aula [aulas_umc_2026_1_spring_boot](https://github.com/itodoconhecer-cmyk/aulas_umc_2026_1_spring_boot) e mantém a mesma arquitetura: `model` com Value Objects, `DTO`, `UseCase`, repositórios em **JDBC puro**, controllers REST e frontend em HTML/CSS/JS puro.

## Como rodar

**Opção 1: sem instalar banco (H2)**

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```

Os dados ficam em `./data/frota`. O console do H2 fica em http://localhost:8080/h2-console (JDBC URL `jdbc:h2:file:./data/frota`, usuário `sa`).

**Opção 2: PostgreSQL (padrão)**

```bash
docker compose up -d
./mvnw spring-boot:run
```

As tabelas são criadas automaticamente pelo `schema.sql`, que é idempotente.

Depois de subir a aplicação, estão disponíveis:

- Interface: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html
- Testes: `./mvnw test` (usam H2 em memória)

Requer JDK 17 ou superior.

## Regras de negócio

| Regra | Onde fica |
|---|---|
| A placa aceita o padrão antigo (`ABC-1234`) e o Mercosul (`ABC1D23`), e não pode se repetir | `Placa`, `SalvarVeiculoUseCase` |
| A CNH tem 11 dígitos, não se repete e tem categoria válida | `Cnh`, `CategoriaCnh`, `SalvarMotoristaUseCase` |
| **O hodômetro não volta:** o abastecimento precisa ter km maior ou igual ao atual do veículo | `Veiculo.registrarQuilometragem` |
| O abastecimento atualiza o km do veículo **na mesma transação** | `RegistrarAbastecimentoUseCase` (`@Transactional`) |
| Motorista com CNH vencida na data não pode abastecer | `RegistrarAbastecimentoUseCase` |
| Não se aceita data futura em abastecimentos e manutenções | UseCases |
| A manutenção pode ser lançada com atraso: km menor que o atual é aceito, mas não reduz o hodômetro | `Veiculo.atualizarQuilometragemSeMaior` |
| **Uma manutenção do tipo REVISAO reinicia o ciclo do alerta** | `Veiculo.registrarRevisao` |
| O consumo (km/L) é calculado pelo método do tanque cheio: km rodados desde o abastecimento anterior ÷ litros | `ConsultarAbastecimentosUseCase` |
| A exclusão é lógica (`status = 3`), como no projeto base | `JdbcRepository.excluirLogico` |

### Alerta de revisão

```
próxima revisão = km da última revisão + intervalo (padrão 10.000 km)
km restante     = próxima revisão − km atual
```

| Situação | Condição |
|---|---|
| `VENCIDA` | km restante ≤ 0 |
| `PROXIMA` | km restante ≤ `frota.revisao.antecedencia-km` (padrão 1.000) |
| `EM_DIA` | caso contrário |

A antecedência é configurada no `application.properties`.

## API

| Método | Rota | Descrição |
|---|---|---|
| `GET/POST` | `/api/veiculos` | Lista ou cadastra veículos |
| `GET/PUT/DELETE` | `/api/veiculos/{id}` | Busca, atualiza ou exclui um veículo |
| `GET/POST` | `/api/motoristas` | Lista ou cadastra motoristas |
| `GET/PUT/DELETE` | `/api/motoristas/{id}` | Busca, atualiza ou exclui um motorista |
| `GET/POST` | `/api/abastecimentos?veiculoId=` | Lista (com filtro opcional) ou registra abastecimentos |
| `GET/DELETE` | `/api/abastecimentos/{id}` | Busca ou exclui um abastecimento |
| `GET/POST` | `/api/manutencoes?veiculoId=` | Lista (com filtro opcional) ou registra manutenções |
| `GET/DELETE` | `/api/manutencoes/{id}` | Busca ou exclui uma manutenção |
| `GET` | `/api/alertas/revisao?todos=false` | Veículos com revisão vencida ou próxima, do mais urgente ao menos urgente |

Os erros voltam como JSON, com mensagem: `{"status":422,"mensagem":"CNH de Carlos Souza vencida em 15/01/2026."}`. Os códigos usados são:

- **400**: dado inválido
- **404**: registro não encontrado
- **422**: regra de negócio violada

Abastecimentos e manutenções são eventos e não podem ser editados. Para corrigir um lançamento, exclua e registre de novo. A exclusão não desfaz o km do veículo. Se precisar, corrija o km e a última revisão pela edição do veículo.

## Diferenças em relação ao projeto base

- Os repositórios e UseCases são injetados pelo Spring (no base eram criados com `new` no controller).
- O `TratamentoErrosController` devolve a mensagem do erro, em vez de um 400 vazio.
- Operações que alteram mais de uma tabela usam `@Transactional`, e o `SELECT ... FOR UPDATE` no veículo evita que dois abastecimentos simultâneos sobrescrevam o km um do outro.
- Os pacotes dos Value Objects batem com as pastas (`aulas.umc.frota.model.valueObjects`).
- O frontend é servido pelo próprio Spring (`src/main/resources/static`), sem servidor Node separado.
