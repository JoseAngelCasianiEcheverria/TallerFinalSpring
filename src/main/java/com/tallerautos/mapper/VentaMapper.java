package com.tallerautos.mapper;

import org.springframework.stereotype.Component;

import com.tallerautos.dto.response.VentaResponse;
import com.tallerautos.entity.Venta;


@Component
public class VentaMapper {

    public VentaResponse aResponse(Venta venta) {
        if (venta == null) {
            return null;
        }

        return new VentaResponse(
                venta.getId(),
                venta.getCliente() == null ? null : venta.getCliente().getId(),
                venta.getCliente() == null ? null : venta.getCliente().getNombre(),
                venta.getCliente() == null ? null : venta.getCliente().getEmail(),
                venta.getVehiculo() == null ? null : venta.getVehiculo().getId(),
                venta.getVehiculo() == null ? null : venta.getVehiculo().getPlaca(),
                venta.getVehiculo() == null ? null : venta.getVehiculo().getMarca(),
                venta.getVehiculo() == null ? null : venta.getVehiculo().getModelo(),
                venta.getFechaVenta(),
                venta.getValorVehiculo(),
                venta.getDescuentoAplicado(),
                venta.getTotal(),
                venta.getTasaCambio(),
                venta.getPrecioUsd());
    }
}
