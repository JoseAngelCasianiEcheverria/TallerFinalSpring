package com.tallerautos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tallerautos.dto.request.MantenimientoRequest;
import com.tallerautos.dto.request.MantenimientoUpdateRequest;
import com.tallerautos.dto.response.MantenimientoResponse;
import com.tallerautos.service.MantenimientoService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/mantenimientos")
public class MantenimientoController {

    private final MantenimientoService servicio;

    public MantenimientoController(MantenimientoService servicio) {
        this.servicio = servicio;
    }

    
    @GetMapping
    public List<MantenimientoResponse> listar() {
        return servicio.listar();
    }

    
    @GetMapping("/vehiculo/{vehiculoId}")
    public List<MantenimientoResponse> historialPorVehiculo(@PathVariable Long vehiculoId) {
        return servicio.historialPorVehiculo(vehiculoId);
    }

    
    @PostMapping
    public ResponseEntity<MantenimientoResponse> registrar(
            @Valid @RequestBody MantenimientoRequest request) {

        MantenimientoResponse creado = servicio.registrar(request);
        return ResponseEntity
                .created(URI.create("/api/mantenimientos/" + creado.getId()))
                .body(creado);
    }

    
    @PatchMapping("/{id}/cerrar")
    public MantenimientoResponse cerrar(@PathVariable Long id) {
        return servicio.cerrar(id);
    }

    
    @PutMapping("/{id}")
    public MantenimientoResponse actualizar(@PathVariable Long id,
                                            @Valid @RequestBody MantenimientoUpdateRequest request) {
        return servicio.actualizar(id, request);
    }
}
