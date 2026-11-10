package br.com.bemdoar.repository;

import br.com.bemdoar.entity.Necessidade;
import br.com.bemdoar.enums.SituacaoNecessidade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

public interface NecessidadeRepository extends JpaRepository<Necessidade, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT n FROM Necessidade n WHERE n.id = :id")
    Optional<Necessidade> bloquearPorId(@Param("id") Long id);

    /**
     * RF07 - busca por titulo OU descricao + filtros combinados de
     * categoria, prioridade e situacao. Um filtro nulo simplesmente
     * nao entra na condicao.
     */
    @Query("""
            SELECT n FROM Necessidade n
            WHERE (:termo IS NULL OR LOWER(n.titulo) LIKE LOWER(CONCAT('%', :termo, '%'))
                                 OR LOWER(n.descricao) LIKE LOWER(CONCAT('%', :termo, '%')))
              AND (:categoriaId IS NULL OR n.categoria.id = :categoriaId)
              AND (:prioridade IS NULL OR n.prioridade = :prioridade)
              AND (:situacao IS NULL OR n.situacao = :situacao)
            """)
    Page<Necessidade> buscar(@Param("termo") String termo,
                             @Param("categoriaId") Long categoriaId,
                             @Param("prioridade") br.com.bemdoar.enums.Prioridade prioridade,
                             @Param("situacao") SituacaoNecessidade situacao,
                             Pageable pageable);

    long countBySituacao(SituacaoNecessidade situacao);

    /** RF05 - impede desativar/apagar categoria que ja esta em uso. */
    boolean existsByCategoriaId(Long categoriaId);
}
