package br.com.bemdoar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnulacaoRecebimentoRequest(@NotBlank @Size(max = 1000) String justificativa) {}
