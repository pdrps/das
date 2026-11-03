package br.com.univille.dto;

import br.com.univille.entity.StatusChamado;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusRequest(
    @NotNull(message = "Status é obrigatório")
    StatusChamado status
) {}