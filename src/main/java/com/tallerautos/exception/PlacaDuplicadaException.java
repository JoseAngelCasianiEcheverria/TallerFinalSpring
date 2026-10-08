package com.tallerautos.exception;


public class PlacaDuplicadaException extends BusinessRuleException {

    public PlacaDuplicadaException(String placa) {
        super("Ya existe un vehiculo con la placa " + placa
                + ". La placa debe ser unica.");
    }
}
