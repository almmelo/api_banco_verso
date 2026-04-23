package br.com.banco.verso.service;

import br.com.banco.verso.model.LoggedUser;

public interface CurrentUserService {

    LoggedUser getLoggedUser();
}