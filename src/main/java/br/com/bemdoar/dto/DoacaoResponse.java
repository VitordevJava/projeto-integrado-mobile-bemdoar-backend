package br.com.bemdoar.dto;

import br.com.bemdoar.entity.Doacao;
import br.com.bemdoar.enums.FormaEntrega;
import br.com.bemdoar.enums.SituacaoDoacao;

import java.time.LocalDateTime;

public record DoacaoResponse(
        Long id,
        String protocolo,
        Long usuarioId,
        String usuarioNome,
        Long necessidadeId,
        String necessidadeTitulo,
        Long campanhaId,
        String campanhaTitulo,
        String item,
        Integer quantidade,
        Integer quantidadePrometida,
        Integer quantidadeRecebida,
        String observacao,
        FormaEntrega formaEntrega,
        SituacaoDoacao situacao,
        LocalDateTime dataRegistro,
        LocalDateTime dataConfirmacao,
        LocalDateTime prazoAnalise,
        LocalDateTime prazoEntrega,
        LocalDateTime dataRecebimento,
        String motivoCancelamento,
        String responsavelRecebimento) {

    public static DoacaoResponse de(Doacao doacao) {
        return new DoacaoResponse(
                doacao.getId(),
                doacao.getProtocolo(),
                doacao.getUsuario().getId(),
                doacao.getUsuario().getNome(),
                doacao.getNecessidade() == null ? null : doacao.getNecessidade().getId(),
                doacao.getNecessidade() == null ? null : doacao.getNecessidade().getTitulo(),
                doacao.getCampanha() == null ? null : doacao.getCampanha().getId(),
                doacao.getCampanha() == null ? null : doacao.getCampanha().getTitulo(),
                doacao.getItem(),
                doacao.getQuantidade(),
                doacao.getQuantidade(),
                doacao.getQuantidadeRecebida(),
                doacao.getObservacao(),
                doacao.getFormaEntrega(),
                doacao.getSituacao(),
                doacao.getDataRegistro(),
                doacao.getDataConfirmacao(),
                doacao.getPrazoAnalise(),
                doacao.getPrazoEntrega(),
                doacao.getDataRecebimento(),
                doacao.getMotivoCancelamento(),
                doacao.getResponsavelRecebimento() == null ? null : doacao.getResponsavelRecebimento().getNome());
    }
}
