package com.tallerautos.exception;


public class ConHistorialException extends BusinessRuleException {

    public ConHistorialException(String que, String historial) {
        super("No se puede eliminar " + que + " porque tiene " + historial
                + " asociado. Ese historial es la trazabilidad del negocio.");
    }
}
