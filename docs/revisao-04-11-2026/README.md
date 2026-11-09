# BemDoar — demandas após revisão do PO (04/11/2026)

Preparado em 09/11/2026. Base: PDF do PO `BemDoar_Revisao_Consolidada_04-11-2026.pdf` (7 páginas), comparado com `origin/main` do backend (`e08480c`) e do frontend (`f882877`). Os dois commits remotos mais recentes mudam somente o `README.md`; o código analisado é o mesmo do checkout local (`48c478d` e `4d06973`). O PDF diz que é um **aditivo a incorporar/validar nos documentos oficiais**, não prova de implementação. As decisões D01–D12 e o recorte da primeira versão orientam as demandas; os campos da seção 8 são sugestões de modelagem.

## Mudança de escopo

O MVP agora tem uma única instituição, categorias, necessidades, cadastro/login, intenção de doação física, aprovação manual, protocolo, recebimento e histórico privado. **Suspendam a divisão antiga** (Doações/Transparência, Campanhas/Comunicados, Voluntariado, Ações/Notificações). Campanhas (RF14–RF16), voluntariado, ações sociais, comunicados, transparência completa, relatórios, QR Code, e-mail/push e aprovação automática não recebem desenvolvimento nesta entrega. Não apaguem tabelas nem histórico por isso; tirem essas opções da navegação e do fluxo do MVP.

O PDF `BemDoar-Manual-da-Equipe.pdf` foi escrito antes deste recorte e **não serve mais para distribuir tarefas**. As instruções de instalação/Git continuam úteis; as frentes antigas e a lista de funcionalidades devem ser substituídas pelos quatro cards abaixo.

| Novo card | Responsabilidade | Prioridade | Base para começar |
|---|---|---|---|
| [DEV 1 — Necessidades](01-necessidades.md) | Justificativa, autoria, edição, meta ATENDIDA | P0 | imediato |
| [DEV 2 — Doador e protocolo](02-intencao-protocolo.md) | Intenção, protocolo, sigilo, histórico do titular | P0 | imediato; combinar contrato do DTO com devs 3 e 4 |
| [DEV 3 — Análise e prazos](03-analise-prazos.md) | Aprovação manual, 3/15 dias, cancelamento automático | P0 | iniciar serviço/rotina e testes em paralelo; integrar após DTO do dev 2 |
| [DEV 4 — Recebimento e auditoria](04-recebimento-auditoria.md) | Entrega parcial, correção/anulação, contagem única | P0 | iniciar auditoria e testes em paralelo; integrar após devs 2 e 3 |

## O que já existe, e o que falta

| Item | Estado visto em `main` | Evidência no código | Aditivo |
|---|---|---|---|
| Cadastro/login e admin | presente | `AuthController`, `UsuarioService`, `SecurityConfig` | preservar |
| Necessidade e categoria | CRUD presente; justificativa e autoria de edição ausentes | `Necessidade`, `NecessidadeService`, `NecessidadeRequest` | D04, RN37, p. 3–5 |
| Progresso | soma `Doacao.quantidade` de RECEBIDA; aceita intenção em ATENDIDA | `DoacaoRepository.somarRecebidasDaNecessidade`, `NecessidadeService.buscarDisponivelOuFalhar` | D07, D10, RN32, RN35 |
| Intenção | PENDENTE e vínculo ao usuário presentes; protocolo próprio ausente | `DoacaoService.criar`, `Doacao`, `DoacaoResponse` | D01, RN28 |
| Aprovação | manual presente; prazo e vencimento ausentes | `DoacaoService.confirmar` | D05–D06, RN30–RN31 |
| Recebimento | 1 transição CONFIRMADA→RECEBIDA; usa quantidade prometida, sem parcial, correção ou auditoria | `DoacaoService.receber` | D07–D09, RN32–RN34 |
| Privacidade | detalhe valida dono/admin; listagem admin protegida | `DoacaoService.buscar`, `DoacaoController` | D02, D12: rever todos os caminhos |
| Front | form de intenção, Minhas Doações e gestão admin presentes; sem protocolo, prazo ou quantidade recebida | `src/paginas/doacoes/*` | RF11–RF13 revisados |
| Módulos fora do MVP | entidades e rotas herdadas; transparência pública mistura módulos futuros | `App.jsx`, `TransparenciaService`, `SecurityConfig` | p. 1, 7 |
| Instituição parceira | seed traz nome/endereço fictícios de São Paulo | `DataSeeder` | D03, RN38: nome oficial pendente, paróquia no Gama/DF |

## Contrato de integração e ordem de PRs

1. **Dev 2 abre primeiro o PR de modelo/contrato**: `Doacao` com protocolo único, quantidade prometida e recebida separadas, datas, campos de motivo e DTOs. Para não quebrar o sistema, migre gradualmente os consumidores. Convenção recomendada: `protocolo` gerado pelo servidor (UUID textual ou formato curto com unicidade no banco), `quantidadePrometida`, `quantidadeRecebida` (nula antes de RECEBIDA), `prazoAnalise`, `prazoEntrega`. Não aceite protocolo ou usuário vindos do cliente. Decidir tratamento dos registros H2 antigos antes de marcar novas colunas como obrigatórias; jamais orientar os colegas a apagar banco com histórico sem consentimento.
2. **Dev 1 integra a necessidade** com justificativa/autoria e regra ATENDIDA. Dev 2 chama `NecessidadeService.buscarDisponivelOuFalhar` na criação. Essa API deve rejeitar ATENDIDA e ENCERRADA; receber intenção anterior continua permitido.
3. **Dev 3 integra análise/prazos** sobre o contrato do dev 2. Deve concentrar regras em serviço próprio (ex.: `AnaliseDoacaoService` e `VencimentoDoacaoService`) e adicionar métodos finos ao controller existente. Evitar alterar o cadastro do dev 2.
4. **Dev 4 integra recebimento e auditoria** sobre os PRs anteriores. Criar serviço próprio (ex.: `RecebimentoDoacaoService`) e registro de auditoria; atualizar cálculo em `DoacaoRepository`/`NecessidadeService`. O tech lead revisa as alterações compartilhadas em `DoacaoController`, `DoacaoService`, `DoacaoRepository`, `App.jsx` e `SecurityConfig`.

Cada dev trabalha em **branch própria nos dois repositórios** quando tocar back e front. Sugerimos `feature/revisao-dev1-necessidades` etc. Não façam PR das quatro frentes diretamente sobre o mesmo `DoacaoService` sem esperar a ordem acima. Um PR de contrato pequeno reduz o tempo de espera dos devs 3 e 4.

## Decisões a fechar com PO/tech lead antes do merge final

- **Contagem de prazo**: “até o fim de 3/15 dias corridos” e “começam no registro/aprovação” não fixam inequivocamente se vence no mesmo horário de `+3/+15 dias` ou às 23:59 do último dia. Fixar um único critério, fuso `America/Sao_Paulo`, e testar os instantes antes/depois.
- **Dados atuais**: como migrar doações existentes (`quantidade` hoje significa prometida e também a recebida) e como identificar autor de necessidades antigas. Valores históricos desconhecidos não devem ser inventados.
- **Nome oficial e carta de aceite**: obter da parceira e registrar em documentação de implantação restrita. Não colocar o nome fictício do seed ou a carta em tela pública.
- **Identidade da parceira — conflito entre fontes atuais**: o aditivo descreve uma **paróquia no Gama/DF, nome oficial pendente**; os `README.md` publicados nos dois repositórios afirmam **“clube do idoso Nina Cruz (Gama-DF)”**. O `DataSeeder` ainda traz instituição fictícia de São Paulo. PO e tech lead precisam confirmar qual instituição é a parceira oficial e corrigir documentos, READMEs e dados de demonstração de forma consistente antes da apresentação ou uso oficial. Não assumir que uma das fontes está certa apenas pelo nome.
- **Anulação excepcional**: o aditivo sugere CANCELADA com motivo RECEBIMENTO_INDEVIDO, mas deixa essa representação para formalização. Fixar status/histórico antes de implementar. Uma opção é CANCELADA excepcionalmente e registro de auditoria imutável.
- **Item divergente**: a conferência é humana. O software precisa impedir que o admin receba um item declarado como diferente do prometido no mesmo registro; não promete reconhecer fisicamente o produto.

## Verificação de entrega

Em cada PR, anexar evidência dos cenários do próprio card, `./mvnw test` no backend, `npm run build` no frontend e ao menos uma chamada negada de usuário sem permissão. O PO propõe os cenários da página 6. Os testes existentes (`FrenteUmIntegracaoTest`) cobrem parte do ciclo antigo e deverão ser atualizados com os novos campos e regras.

Estado da checagem local em 09/11: `npm run build` passou e `./mvnw -Dmaven.repo.local=/private/tmp/bemdoar-m2 test` passou com **4 testes, 0 falhas**. O comando sem `-Dmaven.repo.local` não chegou aos testes por falta de permissão de escrita no cache Maven padrão (`~/.m2`) desta sessão. Não atribuir esse erro a uma feature.

## Mensagem pronta para enviar à equipe

> Pessoal, o PO revisou o escopo depois da apresentação de 04/11. O MVP do BemDoar agora foca em uma instituição parceira, necessidades e doações físicas. Campanhas, voluntariado e ações sociais ficaram para uma versão futura; por favor, não continuem as frentes antigas do manual. Separei quatro cards novos: 1) necessidades e autoria; 2) protocolo e histórico privado do doador; 3) aprovação manual e prazos de 3/15 dias; 4) recebimento parcial e correções auditadas. Cada card traz arquivos e critérios de pronto. Antes de começar, atualizem `main` dos dois repositórios e abram uma branch para o seu card. O dev 2 entrega primeiro o contrato de dados de doação; os devs 3 e 4 podem preparar as classes e cenários em paralelo, integrando após esse PR. Me avisem qual card cada um assumiu. Temos uma divergência entre o PDF novo (paróquia) e os READMEs (clube do idoso); o PO vai confirmar a instituição correta.
