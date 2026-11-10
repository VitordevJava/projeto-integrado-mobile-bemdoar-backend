package br.com.bemdoar.entity;

import br.com.bemdoar.enums.FormaEntrega;
import br.com.bemdoar.enums.SituacaoDoacao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Intenção física de doação. Registros antigos de campanha são mantidos somente para consulta. */
@Entity
@Table(name = "doacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Nullable somente para preservar doacoes historicas de campanha.
    // Novos cadastros exigem necessidade no service.
    @ManyToOne
    @JoinColumn(name = "necessidade_id")
    private Necessidade necessidade;

    @ManyToOne
    @JoinColumn(name = "campanha_id")
    private Campanha campanha;

    @Column(nullable = false, length = 150)
    private String item;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(unique = true, length = 50)
    private String protocolo;

    /** Nula enquanto nao houver entrega fisica. */
    @Column(name = "quantidade_recebida")
    private Integer quantidadeRecebida;

    @Column(name = "prazo_analise")
    private LocalDateTime prazoAnalise;

    @Column(name = "prazo_entrega")
    private LocalDateTime prazoEntrega;

    @ManyToOne
    @JoinColumn(name = "responsavel_recebimento_id")
    private Usuario responsavelRecebimento;


    @Column(length = 500)
    private String observacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_entrega", nullable = false, length = 30)
    private FormaEntrega formaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SituacaoDoacao situacao;

    @Column(name = "data_registro", nullable = false)
    private LocalDateTime dataRegistro;

    @Column(name = "data_confirmacao")
    private LocalDateTime dataConfirmacao;

    @Column(name = "data_recebimento")
    private LocalDateTime dataRecebimento;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;
}
