package com.chatop.api.auth.exception;

public class EmailAlreadyUsedException extends RuntimeException {

    public EmailAlreadyUsedException() {
        super("Cette adresse e-mail est déjà utilisée");
    }
}
