package br.com.sistemas.das.dto;

import br.com.sistemas.chamados.entty.Cliente;

public record ClienteResponse(Long id, String nome, String email, String telefone){
    public static ClienteResponse de(Cliente c){
        return nex ClienteResponse(c.getId(), c.getNome(), c.getEmail(), c.getTelefone())
    }
}