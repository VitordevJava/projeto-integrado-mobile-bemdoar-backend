package br.com.bemdoar.dto;

import br.com.bemdoar.enums.Prioridade;
import jakarta.validation.constraints.*;

/** RF06 / RF08 - dados que o administrador digita. */
public record NecessidadeRequest(

        @NotBlank(message = "Informe o titulo.")
        @Size(max = 150, message = "Titulo muito longo.")
        String titulo,

        @NotBlank(message = "Informe a descricao.")
        @Size(max = 2000, message = "Descricao muito longa.")
        String descricao,

        @NotBlank(message = "Informe a justificativa.")
        @Size(max = 1000, message = "Justificativa muito longa.")
        String justificativa,

        @NotNull(message = "Selecione uma categoria ativa.")
        Long categoriaId,

        @NotNull(message = "Informe a quantidade necessaria.")
        @Positive(message = "A quantidade deve ser maior que zero.")
        Integer quantidadeNecessaria,

        @NotNull(message = "Selecione a prioridade.")
        Prioridade prioridade
) {
}
