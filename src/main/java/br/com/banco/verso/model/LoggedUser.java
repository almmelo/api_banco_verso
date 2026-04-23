package br.com.banco.verso.model;

public record LoggedUser(Long id, String username, String name, String role) {

    public boolean isGerente() {
        return "GERENTE".equals(role);
    }

    public boolean isCliente() {
        return "CLIENTE".equals(role);
    }
}