# DEV 3 — Aprovação manual, consulta administrativa e prazos

**Prioridade:** P0. **Referência:** D05/D06/D12, RN30/RN31, RF12/RF13 revisados, páginas 2, 4–6 do aditivo. **Resultado:** pendências vencem após 3 dias sem análise, confirmações vencem após 15 dias sem entrega; o admin localiza uma intenção por protocolo e decide manualmente.

## Situação atual

`DoacaoService.confirmar` muda PENDENTE para CONFIRMADA e grava `dataConfirmacao`, mas não cria prazo. Não há rotina de vencimento nem motivo padronizado. `DoacaoAdminPage` lista por status, sem busca por protocolo. `NotificacaoService` cria notificações internas na confirmação e no recebimento; o novo aditivo exclui e-mail/push/lembretes de prazo e não exige notificações internas.

## Implementar

1. Seguir o contrato de campos do dev 2. Exibir prazo de análise da intenção PENDENTE e prazo de entrega da CONFIRMADA. Os 15 dias só começam em `dataConfirmacao`. Definir com PO/tech lead o instante exato de vencimento antes de fechar lógica (ver `README.md`). Guardar instantes em fuso coerente e testar limites.
2. Criar mecanismo automático simples e sem serviços externos: rotina periódica Spring `@Scheduled` **ou** verificação transacional equivalente. Verificar também no instante de confirmar/receber para não aprovar/receber registro já vencido que a rotina ainda não processou. PENDENTE vencida → CANCELADA, motivo `NAO_ANALISADA_NO_PRAZO`; CONFIRMADA vencida → CANCELADA, motivo `PRAZO_EXPIRADO`. Manter histórico visível ao titular. Não criar estado EXPIRADA.
3. Preservar cancelamento pelo próprio titular em PENDENTE/CONFIRMADA e admin com justificativa. RECEBIDA/CANCELADA não entram na rotina de prazo. Não enviar e-mail, push ou lembrete a cada cinco dias.
4. Criar busca administrativa por protocolo digitado. Tela mostra identidade do titular, necessidade, item prometido, quantidade prometida, status e prazo. API protegida por perfil admin e validação no serviço; consulta pública por protocolo é proibida. Busca inexistente deve dar 404 sem vazar registro de terceiros.
5. Na gestão admin, mostrar vencimento/contagem de tempo e ações compatíveis com status. Manter aprovação **manual**. Botão oculto não substitui validação do servidor.

## Arquivos-alvo

Backend: criar `service/AnaliseDoacaoService.java` e `service/VencimentoDoacaoService.java` (ou nomes equivalentes), adicionar métodos de busca ao `DoacaoRepository.java`, endpoints mínimos no `DoacaoController.java`; campos novos apenas após integrar contrato do dev 2. Frontend: `src/paginas/doacoes/DoacaoAdminPage.jsx` para busca por protocolo, aprovação e prazos. O histórico do titular (`MinhasDoacoesPage.jsx`) pertence ao dev 2, que consome os campos de prazo. Não editar `NecessidadeService` nem o recebimento.

## Critérios de aceite

- Intenção criada em `t0` permanece PENDENTE até o limite definido; vencida sem análise fica CANCELADA/NAO_ANALISADA_NO_PRAZO. Rodar a rotina duas vezes não altera o resultado.
- Admin confirma antes do limite: CONFIRMADA, `dataConfirmacao` preenchida e prazo de entrega a partir dessa data. Sem recebimento até o limite: CANCELADA/PRAZO_EXPIRADO.
- Admin não consegue confirmar PENDENTE vencida nem receber CONFIRMADA vencida, mesmo entre execuções da rotina. Titular ainda pode cancelar uma intenção válida própria.
- Admin busca protocolo e vê dados de conferência. Usuário comum não consegue usar a busca admin por chamada direta; resposta 403.
- Não existe aprovação automática de PENDENTE. Nenhum vencimento gera e-mail/push ou lembrete.

**Dependência:** integrar após o PR de contrato do dev 2. Coordene com dev 4 a interface de “validar prazo no ato de receber”; dev 4 implementa o recebimento, dev 3 fornece uma função simples de prazo que ele chama. Evitar editar os mesmos métodos de `DoacaoService` ao mesmo tempo.
