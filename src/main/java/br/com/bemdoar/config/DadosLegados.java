package br.com.bemdoar.config;

import br.com.bemdoar.entity.Doacao;
import br.com.bemdoar.enums.SituacaoDoacao;
import br.com.bemdoar.repository.DoacaoRepository;
import br.com.bemdoar.repository.NecessidadeRepository;
import br.com.bemdoar.service.NecessidadeService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Preenche campos novos sem apagar registros existentes. Rodar varias vezes e seguro. */
@Component
@Order(2)
public class DadosLegados implements CommandLineRunner {
    private final DoacaoRepository doacoes;
    private final NecessidadeRepository necessidades;
    private final NecessidadeService necessidadeService;

    public DadosLegados(DoacaoRepository doacoes, NecessidadeRepository necessidades,
                        NecessidadeService necessidadeService) {
        this.doacoes = doacoes;
        this.necessidades = necessidades;
        this.necessidadeService = necessidadeService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        for (Doacao d : doacoes.findAll()) {
            boolean mudou = false;
            if (d.getProtocolo() == null) {
                d.setProtocolo("BD-LEGADO-" + d.getId());
                mudou = true;
            }
            if (d.getSituacao() == SituacaoDoacao.RECEBIDA && d.getQuantidadeRecebida() == null) {
                d.setQuantidadeRecebida(d.getQuantidade());
                mudou = true;
            }
            if (d.getSituacao() == SituacaoDoacao.PENDENTE && d.getPrazoAnalise() == null) {
                d.setPrazoAnalise(d.getDataRegistro().plusDays(3));
                mudou = true;
            }
            if (d.getSituacao() == SituacaoDoacao.CONFIRMADA && d.getPrazoEntrega() == null
                    && d.getDataConfirmacao() != null) {
                d.setPrazoEntrega(d.getDataConfirmacao().plusDays(15));
                mudou = true;
            }
            if (mudou) doacoes.save(d);
        }
        doacoes.flush();
        necessidades.findAll().forEach(n -> necessidadeService.recalcularProgresso(n.getId()));
    }
}
