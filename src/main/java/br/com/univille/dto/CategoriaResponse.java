package br.com.univille.dto;

import br.com.univille.entity.Categoria;

public record CategoriaResponse (Long id, String nome, String descricao){
    public static CategoriaResponse de(Categoria c){
        return new CategoriaResponse(c.getId(), c.getNome(), c.getDescricao());
    }
    
}
