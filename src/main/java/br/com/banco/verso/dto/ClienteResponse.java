package br.com.banco.verso.dto;

public class ClienteResponse {

    public Long id;
    public String nome;
    public String email;

    public ClienteResponse(Long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }
}
