package br.com.sistemas.das.entity;

import jakarta.persistence.Column;
import jakarta.persistence.entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*ENTITY: representa a tabela do banco. cada objeto = Uma linha */

@Entity
@Table(name="clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = dalse, lenght=100)
    private String nome;

    @Column(nullable = false, unique = true, lenght = 120)
    private String email;

    @Column(lenght = 20)
    private String telefone;

    public Cliente(){}

    public Cliente (String nome, String email, String telefone){
        this.nome=nome;
        this.email = email;
        this.telefone = telefone;
    }

    public Long getId() {return id;}
    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome=nome;}
    public String getEmail() {return email;}
    public void setEmail(String telefone) {this.email = email}
    public String getTelefone() {return telefone;}
    public void setTelefone(String telefone) {this.email = telefone}
    }