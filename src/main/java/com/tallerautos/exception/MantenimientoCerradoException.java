package com.tallerautos.exception;


public class MantenimientoCerradoException extends BusinessRuleException {

    public MantenimientoCerradoException(Long id) {
        super("El mantenimiento " + id + " ya estaba finalizado. "
                + "No se puede modificar ni volver a cerrar.");
    }
}
