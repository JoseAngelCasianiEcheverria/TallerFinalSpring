package com.tallerautos.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;


public class MantenimientoUpdateRequest {

    @Size(max = 2000, message = "la descripcion no puede superar 2000 caracteres")
    private String descripcion;

    @NotNull(message = "el costo es obligatorio")
    @PositiveOrZero(message = "el costo no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "el costo admite hasta 10 digitos y 2 decimales")
    private BigDecimal costo;

    public MantenimientoUpdateRequest() {
    }

    public MantenimientoUpdateRequest(String descripcion, BigDecimal costo) {
        this.descripcion = descripcion;
        this.costo = costo;
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
