package br.com.bemdoar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RetificacaoRecebimentoRequest(@NotNull @Positive Integer quantidadeRecebida,
                                            @NotBlank @Size(max = 1000) String justificativa) {}
