package com.tallerautos.exception;


public class EmailDuplicadoException extends BusinessRuleException {

    public EmailDuplicadoException(String email) {
        super("Ya existe un cliente con el correo " + email
                + ". Cada cliente se registra una sola vez.");
    }
}
