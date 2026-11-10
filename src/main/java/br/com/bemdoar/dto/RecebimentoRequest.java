package br.com.bemdoar.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecebimentoRequest(@NotNull @Positive Integer quantidadeRecebida,
                                 @NotNull Boolean itemConfere) {}
