package com.tallerautos.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class MantenimientoRequest {

    @NotNull(message = "el identificador del vehiculo es obligatorio")
    private Long vehiculoId;

    @NotBlank(message = "el tipo de mantenimiento es obligatorio")
    @Size(max = 50, message = "el tipo no puede superar 50 caracteres")
    private String tipo;

    @Size(max = 2000, message = "la descripcion no puede superar 2000 caracteres")
    private String descripcion;

    
    @NotNull(message = "el costo es obligatorio")
    @PositiveOrZero(message = "el costo no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "el costo admite hasta 10 digitos y 2 decimales")
    private BigDecimal costo;

    public MantenimientoRequest() {
    }

    public MantenimientoRequest(Long vehiculoId, String tipo, String descripcion,
                                BigDecimal costo) {
        this.vehiculoId = vehiculoId;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.costo = costo;
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
    }
}
