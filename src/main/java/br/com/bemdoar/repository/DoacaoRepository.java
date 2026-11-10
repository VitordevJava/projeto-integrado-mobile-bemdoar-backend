package br.com.bemdoar.repository;

import br.com.bemdoar.entity.Doacao;
import br.com.bemdoar.enums.SituacaoDoacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    boolean existsByProtocolo(String protocolo);

    Optional<Doacao> findByProtocolo(String protocolo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doacao d WHERE d.id = :id")
    Optional<Doacao> bloquearPorId(@Param("id") Long id);

    List<Doacao> findBySituacaoAndPrazoAnaliseLessThanEqual(SituacaoDoacao situacao, LocalDateTime agora);
    List<Doacao> findBySituacaoAndPrazoEntregaLessThanEqual(SituacaoDoacao situacao, LocalDateTime agora);

    List<Doacao> findByUsuarioIdOrderByDataRegistroDesc(Long usuarioId);

    List<Doacao> findBySituacaoOrderByDataRegistroDesc(SituacaoDoacao situacao);

    List<Doacao> findByNecessidadeIdOrderByDataRegistroDesc(Long necessidadeId);

    List<Doacao> findByCampanhaIdOrderByDataRegistroDesc(Long campanhaId);

    boolean existsByNecessidadeId(Long necessidadeId);

    boolean existsByCampanhaId(Long campanhaId);

    long countBySituacao(SituacaoDoacao situacao);

    /**
     * RN10 - soma das quantidades das doacoes RECEBIDA de uma necessidade.
     * COALESCE devolve 0 quando ainda nao existe nenhuma.
     */
    @Query("""
            SELECT COALESCE(SUM(d.quantidadeRecebida), 0) FROM Doacao d
            WHERE d.necessidade.id = :necessidadeId AND d.situacao = 'RECEBIDA'
            """)
    Integer somarRecebidasDaNecessidade(@Param("necessidadeId") Long necessidadeId);

    /** RN25 - soma da campanha. */
    @Query("""
            SELECT COALESCE(SUM(d.quantidade), 0) FROM Doacao d
            WHERE d.campanha.id = :campanhaId AND d.situacao = 'RECEBIDA'
            """)
    Integer somarRecebidasDaCampanha(@Param("campanhaId") Long campanhaId);
}
