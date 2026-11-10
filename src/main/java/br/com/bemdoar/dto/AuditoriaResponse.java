package br.com.bemdoar.dto;

import br.com.bemdoar.entity.RegistroAuditoria;
import java.time.LocalDateTime;

public record AuditoriaResponse(Long id, String acao, String responsavelNome,
                                LocalDateTime dataHora, String justificativa,
                                String valorAnterior, String valorNovo) {
    public static AuditoriaResponse de(RegistroAuditoria r) {
        return new AuditoriaResponse(r.getId(), r.getAcao(),
                r.getResponsavel() == null ? "MIGRACAO" : r.getResponsavel().getNome(),
                r.getDataHora(), r.getJustificativa(), r.getValorAnterior(), r.getValorNovo());
    }
}
