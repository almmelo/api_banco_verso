package br.com.banco.verso.resource.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "Cpf é obrigatório")
        String cpf,

        @NotBlank(message = "Senha é obrigatório")
        String senha

) {
}

