# DEV 1 — Necessidades verificáveis e meta de 100%

**Prioridade:** P0. **Referência:** D04, D10, RN35, RN37, RF06/RF08/RF09/RF10 revisados, páginas 2–5 e cenários da página 6 do aditivo. **Resultado:** o administrador justifica a necessidade e cada alteração fica atribuída; ATENDIDA bloqueia novas intenções, mas continua distinta de ENCERRADA.

## Situação atual

`Necessidade` tem título, descrição, categoria, meta, quantidade recebida, prioridade, situação e datas. `NecessidadeService` salva/edita sem identificar administrador e `buscarDisponivelOuFalhar` rejeita só ENCERRADA. O formulário não pede justificativa. A tela de detalhes oferece “Quero doar” em ATENDIDA. `DataSeeder` cria necessidades sem autoria nem justificativa. O progresso é recalculado, mas a consulta ainda soma `Doacao.quantidade` (valor prometido); o dev 4 trocará essa soma para o efetivamente recebido.

## Implementar

1. Adicionar justificativa obrigatória no cadastro e registro de `criadoPor`, `criadoEm`, `atualizadoPor`, `atualizadoEm`. Guardar **histórico mínimo das edições relevantes**, com autor, data, campo, valor anterior/novo e justificativa; o aditivo não pede um segundo aprovador. Identidade vem do usuário autenticado, nunca do JSON. Preservar `dataCriacao` existente ou migrar com cuidado para `criadoEm`.
2. Atualizar `NecessidadeRequest`/`NecessidadeResponse`, controller, service e formulário admin. Na edição, exigir justificativa para a mudança e registrar autoria/data. Não expor `quantidadeRecebida` como campo editável.
3. `buscarDisponivelOuFalhar` deve aceitar apenas ABERTA ou PARCIALMENTE_ATENDIDA. Bloquear nova intenção em ATENDIDA e ENCERRADA, inclusive chamada direta da API. Necessidades ATENDIDA ainda podem receber doações que já estavam CONFIRMADAS antes do alcance da meta, inclusive acima de 100%.
4. Esconder/desabilitar “Quero doar” em necessidade ATENDIDA ou ENCERRADA e informar o motivo na tela. A regra obrigatória continua no servidor.
5. Atualizar seed e dados existentes sem inventar autor histórico. O aditivo fala em paróquia no Gama/DF, mas os READMEs remotos falam em clube do idoso Nina Cruz; a identidade oficial depende de confirmação do PO. Não substituir o seed fictício por nenhum dos dois nomes antes da decisão. A validação documental é procedimento de implantação, não cadastro de múltiplas ONGs.

## Arquivos-alvo

Backend: `entity/Necessidade.java`, `dto/NecessidadeRequest.java`, `dto/NecessidadeResponse.java`, `service/NecessidadeService.java`, `controller/NecessidadeController.java`, `config/DataSeeder.java`; criar entidade/repository para histórico de edição se necessário. Frontend: `src/paginas/necessidades/NecessidadeFormPage.jsx`, `NecessidadeDetalhePage.jsx`, `NecessidadeListaPage.jsx` e `NecessidadesPublicasPage.jsx` (somente estado/CTA). Não editar `DoacaoService` nem a tela de doações.

## Critérios de aceite

- Admin cria necessidade com justificativa; consulta admin informa autor e data. Usuário comum tentando criar/editar recebe 403.
- Admin edita título/meta com justificativa; histórico registra antes/depois, autor e data; o público não vê dados internos de autoria/auditoria se não forem necessários.
- Tentar editar `quantidadeRecebida` pelo JSON não muda progresso. Meta não pode cair abaixo do já recebido.
- Necessidade com meta 10 e 10 efetivamente recebidos fica ATENDIDA. POST `/api/doacoes` novo para ela falha; o botão desaparece. Recebimento de intenção confirmada anteriormente ainda conclui e pode levar a 11/10.
- ENCERRADA continua marcada ENCERRADA após recálculo; não vira ATENDIDA automaticamente.

**Dependência:** alinhar com dev 2 o contrato `buscarDisponivelOuFalhar`; alinhar com dev 4 o método de soma efetiva. Abrir PR desta frente antes do recebimento do dev 4.
