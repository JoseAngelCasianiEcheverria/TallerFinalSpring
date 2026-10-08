package com.tallerautos.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tallerautos.dto.response.TasaCambioResponse;
import com.tallerautos.service.TasaCambioService;


@RestController
@RequestMapping("/api/tasa-cambio")
public class TasaCambioController {

    private final TasaCambioService servicio;

    public TasaCambioController(TasaCambioService servicio) {
        this.servicio = servicio;
    }

    
    @GetMapping
    public TasaCambioResponse obtener() {
        return servicio.obtenerTasa();
    }
}
