package com.tallerautos.mapper;

import org.springframework.stereotype.Component;

import com.tallerautos.dto.response.VehiculoResponse;
import com.tallerautos.dto.response.VehiculoResponse.EstadoVehiculoResponse;
import com.tallerautos.entity.Vehiculo;


@Component
public class VehiculoMapper {

    
    public VehiculoResponse aResponse(Vehiculo vehiculo) {
        return aResponse(vehiculo, null, null);
    }

    
    public VehiculoResponse aResponse(Vehiculo vehiculo,
                                      java.math.BigDecimal precioUsd,
                                      java.math.BigDecimal tasa) {

        if (vehiculo == null) {
            return null;
        }

        return new VehiculoResponse(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getAnio(),
                vehiculo.getColor(),
                vehiculo.getPrecio(),
                precioUsd,
                tasa,
                aEstado(vehiculo.getEstado()),
                vehiculo.getFechaIngreso());
    }

    public EstadoVehiculoResponse aEstado(com.tallerautos.entity.EstadoVehiculo estado) {
        if (estado == null) {
            return null;
        }
        return EstadoVehiculoResponse.valueOf(estado.name());
    }
}
