package com.tallerautos.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VehiculoResponse {

    private Long id;
    private String placa;
    private String marca;
    private String modelo;
    private Integer anio;
    private String color;
    private BigDecimal precio;

    
    private BigDecimal precioUsd;

    private BigDecimal tasaCambio;
    private EstadoVehiculoResponse estado;
    private LocalDateTime fechaIngreso;

    
    public enum EstadoVehiculoResponse {
        DISPONIBLE,
        EN_MANTENIMIENTO,
        VENDIDO
    }

    public VehiculoResponse() {
    }

    public VehiculoResponse(Long id, String placa, String marca, String modelo,
                            Integer anio, String color, BigDecimal precio,
                            BigDecimal precioUsd, BigDecimal tasaCambio,
                            EstadoVehiculoResponse estado, LocalDateTime fechaIngreso) {
        this.id = id;
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anio = anio;
        this.color = color;
        this.precio = precio;
        this.precioUsd = precioUsd;
        this.tasaCambio = tasaCambio;
        this.estado = estado;
        this.fechaIngreso = fechaIngreso;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getPrecioUsd() {
        return precioUsd;
    }

    public void setPrecioUsd(BigDecimal precioUsd) {
        this.precioUsd = precioUsd;
    }

    public BigDecimal getTasaCambio() {
        return tasaCambio;
    }

    public void setTasaCambio(BigDecimal tasaCambio) {
        this.tasaCambio = tasaCambio;
    }

    public EstadoVehiculoResponse getEstado() {
        return estado;
    }

    public void setEstado(EstadoVehiculoResponse estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
}
