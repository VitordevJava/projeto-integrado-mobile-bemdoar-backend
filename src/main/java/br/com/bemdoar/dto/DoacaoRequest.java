package br.com.bemdoar.dto;

import br.com.bemdoar.enums.FormaEntrega;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DoacaoRequest(
        Long necessidadeId,
        // Campanha nao faz parte do MVP; mantido apenas para rejeicao explicita.
        Long campanhaId,
        @NotBlank @Size(max = 150) String item,
        @NotNull @Positive Integer quantidade,
        @Size(max = 500) String observacao,
        @NotNull FormaEntrega formaEntrega) {
}
