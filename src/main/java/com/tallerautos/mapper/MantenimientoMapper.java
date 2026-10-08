package com.tallerautos.mapper;

import org.springframework.stereotype.Component;

import com.tallerautos.dto.response.MantenimientoResponse;
import com.tallerautos.entity.Mantenimiento;


@Component
public class MantenimientoMapper {

    public MantenimientoResponse aResponse(Mantenimiento mantenimiento) {
        if (mantenimiento == null) {
            return null;
        }

        return new MantenimientoResponse(
                mantenimiento.getId(),
                mantenimiento.getVehiculo() == null ? null : mantenimiento.getVehiculo().getId(),
                mantenimiento.getVehiculo() == null ? null : mantenimiento.getVehiculo().getPlaca(),
                mantenimiento.getTipo(),
                mantenimiento.getDescripcion(),
                mantenimiento.getCosto(),
                mantenimiento.getFechaInicio(),
                mantenimiento.getFechaFin(),
                mantenimiento.getEstado() == null ? null : mantenimiento.getEstado().name());
    }
}
