# BemDoar — backend

MVP web para uma única instituição, com Java 21, Spring Boot 3.5, JPA, H2 em arquivo e autenticação JWT. O nome oficial da instituição parceira está pendente de confirmação pelo PO; o cadastro inicial é fictício para demonstração. O aditivo de 04/11/2026 define o escopo: necessidades e doações físicas. Campanhas, voluntariado, ações e painel de transparência não fazem parte deste MVP.

## Rodar

```sh
./mvnw spring-boot:run
```

No Windows: `mvnw.cmd spring-boot:run`. API em `http://localhost:8080`; `http://localhost:8080/api/necessidades` deve retornar JSON. O H2 é criado em `./data/bemdoar.mv.db`. O console H2 está desabilitado. `./mvnw test` executa os testes de integração.

Contas de demonstração: `admin@bemdoar.com` / `admin123` (administrador) e `maria@email.com` / `maria123` (doadora). São apenas para execução local.

## Fluxo do MVP

1. Administrador cria ou edita necessidade com justificativa. A API grava responsável, data e auditoria. A quantidade recebida é calculada de doações recebidas; não é digitável.
2. Doador autenticado cria uma intenção ligada a uma necessidade aberta. Recebe protocolo `BD-...`, estado `PENDENTE` e prazo de análise de três dias corridos a partir do registro.
3. Administrador confirma manualmente. A entrega deve ocorrer em até 15 dias corridos desde a confirmação. Uma rotina cancela pendências vencidas e entregas não recebidas; as transições também verificam o prazo.
4. Administrador confere o item e registra a quantidade efetiva, que pode ser menor que a prometida. Só ela entra no progresso. Um segundo recebimento é rejeitado.
5. Administrador pode corrigir ou anular o recebimento com justificativa. O progresso é recalculado e a auditoria preserva antes/depois, autor e horário.
6. Ao chegar à meta, a necessidade fica `ATENDIDA` e bloqueia novas intenções. Intenções já confirmadas podem ser recebidas, inclusive acima de 100%. `ENCERRADA` continua encerrada.

O protocolo e o histórico da doação são privados do titular e da administração; a busca por protocolo é administrativa. Relógio de prazos: `America/Sao_Paulo`, com vencimento no mesmo horário após `+3` ou `+15` dias. A anulação excepcional usa `CANCELADA` com motivo `RECEBIMENTO_INDEVIDO`.

O `DataSeeder` só cria dados quando o banco está vazio. `DadosLegados` preenche protocolos e quantidades efetivas de registros antigos sem apagar histórico. Dados de instituição existentes não são substituídos automaticamente: antes de uma apresentação oficial, confirme a parceira com o PO e ajuste o cadastro persistido.

## Código

`controller → service → repository → entity`, com `dto` para entrada/saída. Endpoints relevantes: `GET/POST/PUT /api/necessidades`, `GET /api/necessidades/{id}/admin`, `GET /api/necessidades/{id}/auditoria`, `POST /api/doacoes`, `GET /api/doacoes/minhas`, `GET /api/doacoes/protocolo/{protocolo}` (admin), `PATCH /api/doacoes/{id}/confirmar|receber|retificar|anular|cancelar`. As requisições de recebimento usam `quantidadeRecebida` e `itemConfere`; correção e anulação exigem `justificativa`.
