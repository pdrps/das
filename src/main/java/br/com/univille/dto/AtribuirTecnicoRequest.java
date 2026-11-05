package br.com.univille.dto;

import jakarta.validation.constraints.NotNull;

public record AtribuirTecnicoRequest(
    @NotNull(message = "Técnico é obrigatório")
    Long tecnicoId
) {}