package com.tallerautos.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class VehiculoRequest {

    @NotBlank(message = "la placa es obligatoria")
    @Size(max = 10, message = "la placa no puede superar 10 caracteres")
    private String placa;

    @NotBlank(message = "la marca es obligatoria")
    @Size(max = 50, message = "la marca no puede superar 50 caracteres")
    private String marca;

    @NotBlank(message = "el modelo es obligatorio")
    @Size(max = 80, message = "el modelo no puede superar 80 caracteres")
    private String modelo;

    private Integer anio;

    @Size(max = 40, message = "el color no puede superar 40 caracteres")
    private String color;

    
    @NotNull(message = "el precio es obligatorio")
    @PositiveOrZero(message = "el precio no puede ser negativo")
    @Digits(integer = 13, fraction = 2, message = "el precio admite hasta 13 digitos y 2 decimales")
    private BigDecimal precio;

    public VehiculoRequest() {
    }

    public VehiculoRequest(String placa, String marca, String modelo,
                           Integer anio, String color, BigDecimal precio) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.precio = precio;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
}
