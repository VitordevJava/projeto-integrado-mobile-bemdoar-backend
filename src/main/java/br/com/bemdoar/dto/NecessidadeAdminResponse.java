package br.com.bemdoar.dto;

import br.com.bemdoar.entity.Necessidade;
import java.time.LocalDateTime;

public record NecessidadeAdminResponse(NecessidadeResponse necessidade, String justificativa,
                                       String criadoPor, String atualizadoPor, LocalDateTime atualizadoEm) {
    public static NecessidadeAdminResponse de(Necessidade n) {
        return new NecessidadeAdminResponse(NecessidadeResponse.de(n), n.getJustificativa(),
                n.getCriadoPor() == null ? "Registro anterior a revisao" : n.getCriadoPor().getNome(),
                n.getAtualizadoPor() == null ? null : n.getAtualizadoPor().getNome(),
                n.getAtualizadoEm());
    }
}
