package com.tallerautos.dto.response;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class TasaCambioResponse {

    private String monedaOrigen;
    private String monedaDestino;

    
    private BigDecimal tasa;

    private String fechaActualizacion;

    
    private String origen;

    public TasaCambioResponse() {
    }

    public TasaCambioResponse(String monedaOrigen, String monedaDestino,
                              BigDecimal tasa, String fechaActualizacion, String origen) {
        this.monedaOrigen = monedaOrigen;
        this.monedaDestino = monedaDestino;
        this.tasa = tasa;
        this.fechaActualizacion = fechaActualizacion;
        this.origen = origen;
    }

    public String getMonedaOrigen() {
        return monedaOrigen;
    }

    public void setMonedaOrigen(String monedaOrigen) {
        this.monedaOrigen = monedaOrigen;
    }

    public String getMonedaDestino() {
        return monedaDestino;
    }

    public void setMonedaDestino(String monedaDestino) {
        this.monedaDestino = monedaDestino;
    }

    public BigDecimal getTasa() {
        return tasa;
    }

    public void setTasa(BigDecimal tasa) {
        this.tasa = tasa;
    }

    public String getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(String fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }
}
