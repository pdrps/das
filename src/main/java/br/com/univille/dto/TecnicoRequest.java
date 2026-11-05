package br.com.univille.dto;
import br.com.univille.entity.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record TecnicoRequest(

    @NotBlank (message = "Nome é obrigatório")
    @Size (max = 100, message = "Nome deve ter no máximo 100 caracteres")
    String nome,

    @NotBlank (message = "Email é obrigatório")
    @Email(message = "E-mail inválido")
    @Size (max = 120, message = "Email deve ter no máximo 120 caracteres")
    String email,

    @NotNull (message = "Perfil é obrigatório")
    Perfil perfil
) {}