package br.com.bemdoar.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "registro_auditoria")
@Getter @Setter
public class RegistroAuditoria {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String tipoRegistro;

    @Column(nullable = false)
    private Long registroId;

    @Column(nullable = false, length = 35)
    private String acao;

    @ManyToOne
    @JoinColumn(name = "responsavel_id")
    private Usuario responsavel;

    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(nullable = false, length = 1000)
    private String justificativa;

    @Column(length = 5000)
    private String valorAnterior;

    @Column(length = 5000)
    private String valorNovo;
}
