# DEV 4 — Recebimento físico, retificação e integridade do MVP

**Prioridade:** P0. **Referência:** D07–D09/D11/D12, RN32–RN34/RN36, RF10/RF12 revisados, páginas 3–6 do aditivo. **Resultado:** o admin recebe uma única vez pelo protocolo, registra a quantidade real, corrige/anula excepcionalmente com trilha de auditoria e o progresso reflete os valores válidos.

## Situação atual

`DoacaoService.receber(id)` marca RECEBIDA sem pedir quantidade real nem conferir o item. `DoacaoRepository.somarRecebidasDaNecessidade` soma `Doacao.quantidade` — hoje a prometida. Repetição de RECEBIDA é rejeitada pela transição de estado, mas não há proteção de concorrência nem auditoria. A gestão admin tem botão de receber sem formulário. `TransparenciaService`/front ainda exibem indicadores de campanhas, ações e voluntários; esses módulos ficaram fora do MVP. O seed institucional contém endereço fictício em São Paulo e a configuração expõe H2 Console e `/uploads/**` publicamente.

## Implementar

1. Após integrar o contrato do dev 2 e a função de vencimento do dev 3, criar recebimento administrativo por protocolo (ou ID obtido da busca), aceitando `quantidadeRecebida` positiva e não superior à prometida, salvo decisão formal do PO. Conferir item prometido com o doador; se o item entregue diverge, **não receber** no registro original: cancelar com motivo e orientar nova intenção para necessidade válida. Não inventar reconhecimento automático do produto.
2. Prometida 10, recebida 6 → status RECEBIDA, `quantidadeRecebida=6`, sem saldo pendente. Registrar data e admin responsável. Outro lote exige nova intenção. Só a quantidade efetiva entra no progresso. Repetição ou requisições concorrentes para o mesmo protocolo não podem contabilizar duas vezes; usar transação e bloqueio/versionamento adequado, além da validação de status.
3. Criar retificação excepcional admin: motivo obrigatório, valor anterior, novo, autor e instante em registro de auditoria append-only. Progresso é recalculado do banco e passa a refletir o novo valor. Não permitir edição manual do agregado em Necessidade.
4. Criar anulação excepcional admin de lançamento indevido: motivo obrigatório, trilha de auditoria, retirar do agregado. A representação final (ex.: CANCELADA/RECEBIMENTO_INDEVIDO) precisa ser fixada com PO antes do merge. O titular não ganha cancelamento comum de RECEBIDA.
5. Alinhar o cálculo de transparência ao novo recorte. Nesta versão, ocultar da navegação os indicadores públicos de módulos futuros; se permanecer um indicador de doações, só mostrar agregado sem nome, protocolo, comprovante ou detalhe de doador. O tech lead revisa também `/h2-console/**` e `/uploads/**` em `SecurityConfig`: são liberados atualmente e não devem compor apresentação/uso com dados reais. Documentos da paróquia/carta de aceite ficam fora de `public/` e de uploads públicos.

## Arquivos-alvo

Backend: criar `dto/RecebimentoRequest.java`, `dto/RetificacaoRecebimentoRequest.java`, `dto/AnulacaoRecebimentoRequest.java`, `entity/RegistroAuditoria.java`, `repository/RegistroAuditoriaRepository.java`, `service/RecebimentoDoacaoService.java`; adaptar `DoacaoRepository.java` (soma efetiva), `NecessidadeService.java` (recalcular), `DoacaoController.java` (endpoints admin), `TransparenciaService.java`/`TransparenciaController.java` se mantidos. Frontend: criar `src/paginas/doacoes/RecebimentoAdminPage.jsx` ou seção independente na gestão admin, com conferência/retificação; revisar `src/paginas/transparencia/TransparenciaPage.jsx`, `src/App.jsx`, `src/componentes/Layout.jsx`, `BottomNav.jsx`, `HomePage.jsx` para o recorte. `SecurityConfig.java` e `DataSeeder.java` são arquivos compartilhados: deixar ajuste final com tech lead, abrindo card/nota específica no PR.

## Critérios de aceite

- Prometida 10, recebida 6: doação RECEBIDA, necessidade +6, não +10. Segundo POST/PATCH de receber devolve erro e mantém +6; concorrência também não duplica.
- Corrigir 10→6 com justificativa: progresso cai 4; auditoria mostra 10, 6, autor, horário e motivo. Anular recebimento indevido: progresso retira a quantidade e auditoria preserva a operação.
- Usuário comum tentando receber, corrigir, anular ou consultar auditoria por chamada direta recebe 403. Titular só vê seus dados; auditoria completa é admin.
- Necessidade ATENDIDA por recebimento anterior não aceita nova intenção, mas uma intenção já CONFIRMADA pode ser recebida e levar o progresso acima de 100%.
- Item divergente não entra como recebimento do item original; interface orienta cancelar e criar outra intenção válida. Campos de campanha/ação/voluntariado não aparecem como indicadores do MVP.

**Dependências:** contrato de doação do dev 2 e validação de prazo do dev 3. A edição de `DoacaoRepository`, `DoacaoController` e `NecessidadeService` deve ser pequena e revisada pelo tech lead para evitar conflito com os PRs 1–3.
