# DEV 2 — Intenção privada e protocolo do doador

**Prioridade:** P0. **Referência:** D01/D02/D11/D12, RN28/RN29/RN36, RF11/RF13 revisados, páginas 1–5 e cenários da página 6 do aditivo. **Resultado:** toda intenção válida tem um protocolo único, vinculado ao titular e à necessidade; o titular acompanha os dados sem expor terceiros.

## Situação atual

`DoacaoService.criar` já usa o usuário do token, cria PENDENTE e não aumenta o progresso. `Doacao` tem `id`, `item`, `quantidade`, usuário, necessidade e campanha; ainda não tem protocolo separado nem quantidade recebida. `DoacaoResponse` mostra `usuarioNome` inclusive em consultas do titular. `MinhasDoacoesPage` exibe quantidade e estado, mas não protocolo, prazo ou quantidade efetiva. `DoacaoFormPage` ainda aceita destino campanha, fora do novo MVP.

## Implementar

1. **PR de contrato primeiro:** adicionar protocolo único gerado pelo backend (sem informação pessoal embutida), vínculo obrigatório à necessidade e quantidade prometida separada da efetivamente recebida. Manter campo `quantidade` antigo só durante migração compatível, se necessário. Não aceitar usuário, protocolo, status, quantidade recebida ou datas do cliente no POST. Criar índice/constraint único de protocolo no banco. Registros antigos exigem estratégia explícita de backfill; preservar histórico.
2. Cadastro de intenção só para necessidade disponível, com item e quantidade prometida positivos. Criar PENDENTE e prazo de análise conforme contrato com dev 3. A lógica de aprovação continua manual. Sem caminhos novos para campanha e sem aprovação automática.
3. Depois do POST, mostrar protocolo ao titular e instruir que o apresente na entrega. Em “Minhas Doações” e detalhe, exibir protocolo, prometida, efetiva (se houver), situação, motivo de cancelamento e data-limite. A tela do doador não oferece publicação ou compartilhamento dentro do app.
4. Revisar consultas: GET `/api/doacoes/{id}` e busca por protocolo só respondem ao titular ou admin; a listagem admin é exclusiva do admin. A checagem de titularidade já existe em `DoacaoService.buscar`, mas deve cobrir as novas rotas. Nunca devolver nome/protocolo de outro doador em área pública.
5. Ajustar front e backend para não permitir intenção de campanha no fluxo do MVP. Não apagar doações antigas com campanha; definir comportamento histórico com tech lead.

## Arquivos-alvo

Backend: `entity/Doacao.java`, `dto/DoacaoRequest.java`, `dto/DoacaoResponse.java`, `repository/DoacaoRepository.java`, `service/DoacaoService.java` **somente criação/consulta**, `controller/DoacaoController.java` **somente criação/consulta**; novos DTOs de detalhe quando necessário. Frontend: `src/paginas/doacoes/DoacaoFormPage.jsx`, `MinhasDoacoesPage.jsx` e eventual `DoacaoDetalhePage.jsx`; rota de detalhe em `src/App.jsx` combinada com dev 4. Dev 3 e 4 devem depender dos campos deste PR, sem editar esses arquivos antes da integração.

## Critérios de aceite

- POST `/api/doacoes` autenticado devolve PENDENTE e protocolo; progresso não muda. Dois cadastros geram protocolos diferentes. Cliente tentando mandar `usuarioId` ou `protocolo` não consegue escolher esses valores.
- Protocolo aparece no histórico e no detalhe do titular. GET direto de doação de outra conta devolve 403; sem token, 401. Não há endpoint público de consulta de doação por protocolo.
- O histórico mostra prometida e efetiva como valores distintos após recebimento parcial. Enquanto pendente, efetiva é “ainda não recebida”, não zero contado no progresso.
- Intenção de campanha nova é rejeitada; registros antigos continuam consultáveis de forma privada.

**Dependências:** combinar nomes/tipos de campos com devs 3 e 4 antes do primeiro PR; dev 1 entrega bloqueio de ATENDIDA. Não implementar a rotina de prazo nem a correção administrativa nesta frente.
