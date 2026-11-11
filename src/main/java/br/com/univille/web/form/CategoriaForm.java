package br.com.univille.web.form;

import br.com.univille.dto.CategoriaRequest;
import br.com.univille.dto.CategoriaResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoriaForm {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @Size(max = 100, message = "Descrição deve ter no máximo 100 caracteres")
    private String descricao;

    public static CategoriaForm de(CategoriaResponse r) {
        CategoriaForm form = new CategoriaForm();
        form.setNome(r.nome());
        form.setDescricao(r.descricao());
        return form;
    }

    public CategoriaRequest toRequest() {
        return new CategoriaRequest(nome, descricao);
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}