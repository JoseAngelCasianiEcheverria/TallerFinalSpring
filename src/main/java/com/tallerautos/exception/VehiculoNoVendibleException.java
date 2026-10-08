package com.tallerautos.exception;

import com.tallerautos.entity.EstadoVehiculo;


public class VehiculoNoVendibleException extends BusinessRuleException {

    public VehiculoNoVendibleException(String placa, EstadoVehiculo estado) {
        super("El vehiculo con placa " + placa + " no se puede vender porque esta en estado "
                + estado + ". Solo se venden vehiculos DISPONIBLE.");
    }
}
