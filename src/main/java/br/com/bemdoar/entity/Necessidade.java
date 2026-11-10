package br.com.bemdoar.entity;

import br.com.bemdoar.enums.Prioridade;
import br.com.bemdoar.enums.SituacaoNecessidade;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * RF06 a RF10 - demanda da instituicao por um item.
 * RN06: quantidadeNecessaria > 0 e nunca menor que quantidadeRecebida.
 * RN07/RN10: quantidadeRecebida e DERIVADA das doacoes RECEBIDA.
 *            Nunca e digitada em tela.
 */
@Entity
@Table(name = "necessidade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Necessidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descricao;

    @Column(length = 1000)
    private String justificativa;

    @ManyToOne
    @JoinColumn(name = "criado_por_id")
    private Usuario criadoPor;

    @ManyToOne
    @JoinColumn(name = "atualizado_por_id")
    private Usuario atualizadoPor;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaNecessidade categoria;

    @Column(name = "quantidade_necessaria", nullable = false)
    private Integer quantidadeNecessaria;

    @Column(name = "quantidade_recebida", nullable = false)
    private Integer quantidadeRecebida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Prioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private SituacaoNecessidade situacao;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_encerramento")
    private LocalDateTime dataEncerramento;
}
