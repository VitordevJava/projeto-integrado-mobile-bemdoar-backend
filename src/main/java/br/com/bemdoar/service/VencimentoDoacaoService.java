package br.com.bemdoar.service;

import br.com.bemdoar.entity.Doacao;
import br.com.bemdoar.enums.SituacaoDoacao;
import br.com.bemdoar.exception.RegraNegocioException;
import br.com.bemdoar.repository.DoacaoRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class VencimentoDoacaoService {
    private final DoacaoRepository repository;
    private final Clock clock;

    public VencimentoDoacaoService(DoacaoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Roda periodicamente. O prazo tambem e conferido em cada transicao. */
    @Scheduled(initialDelayString = "${bemdoar.vencimentos.intervalo-ms:60000}",
               fixedDelayString = "${bemdoar.vencimentos.intervalo-ms:60000}")
    @Transactional
    public void cancelarVencidas() {
        LocalDateTime agora = LocalDateTime.now(clock);
        for (Doacao candidata : repository.findBySituacaoAndPrazoAnaliseLessThanEqual(SituacaoDoacao.PENDENTE, agora)) {
            Doacao d = repository.bloquearPorId(candidata.getId()).orElseThrow();
            if (d.getSituacao() == SituacaoDoacao.PENDENTE && d.getPrazoAnalise() != null
                    && !agora.isBefore(d.getPrazoAnalise())) {
                d.setSituacao(SituacaoDoacao.CANCELADA);
                d.setMotivoCancelamento("NAO_ANALISADA_NO_PRAZO");
            }
        }
        for (Doacao candidata : repository.findBySituacaoAndPrazoEntregaLessThanEqual(SituacaoDoacao.CONFIRMADA, agora)) {
            Doacao d = repository.bloquearPorId(candidata.getId()).orElseThrow();
            if (d.getSituacao() == SituacaoDoacao.CONFIRMADA && d.getPrazoEntrega() != null
                    && !agora.isBefore(d.getPrazoEntrega())) {
                d.setSituacao(SituacaoDoacao.CANCELADA);
                d.setMotivoCancelamento("PRAZO_EXPIRADO");
            }
        }
    }

    public void exigirNoPrazo(Doacao d) {
        LocalDateTime limite = d.getSituacao() == SituacaoDoacao.PENDENTE
                ? d.getPrazoAnalise() : d.getPrazoEntrega();
        if (limite != null && !LocalDateTime.now(clock).isBefore(limite)) {
            throw new RegraNegocioException("O prazo desta doacao expirou.");
        }
    }
}
