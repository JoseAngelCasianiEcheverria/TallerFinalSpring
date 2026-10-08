package com.tallerautos.dto.request;

import jakarta.validation.constraints.NotNull;


public class VentaRequest {

    @NotNull(message = "el identificador del cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "el identificador del vehiculo es obligatorio")
    private Long vehiculoId;

    public VentaRequest() {
    }

    public VentaRequest(Long clienteId, Long vehiculoId) {
        this.clienteId = clienteId;
        this.vehiculoId = vehiculoId;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }
}
