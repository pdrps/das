package br.com.univille.dto;
import br.com.univille.entity.Perfil;
import br.com.univille.entity.Tecnico;

public record TecnicoResponse ( Long id, String nome, String email, Perfil perfil) {
    public static TecnicoResponse de (Tecnico t){
        return new TecnicoResponse(t.getId(), t.getNome(), t.getEmail(), t.getPerfil());
    }
}