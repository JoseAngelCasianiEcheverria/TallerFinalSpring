package com.tallerautos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tallerautos.dto.request.VehiculoRequest;
import com.tallerautos.dto.response.VehiculoResponse;
import com.tallerautos.service.VehiculoService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoService servicio;

    public VehiculoController(VehiculoService servicio) {
        this.servicio = servicio;
    }

    
    @GetMapping
    public List<VehiculoResponse> listar() {
        return servicio.listar();
    }

    
    @GetMapping("/disponibles")
    public List<VehiculoResponse> listarDisponibles() {
        return servicio.listarDisponibles();
    }

    
    @GetMapping("/marca/{marca}")
    public List<VehiculoResponse> buscarPorMarca(@PathVariable String marca) {
        return servicio.buscarPorMarca(marca);
    }

    
    @GetMapping("/{id}")
    public VehiculoResponse buscarPorId(@PathVariable Long id) {
        return servicio.buscarPorId(id);
    }

    
    @PostMapping
    public ResponseEntity<VehiculoResponse> registrar(@Valid @RequestBody VehiculoRequest request) {
        VehiculoResponse creado = servicio.registrar(request);
        return ResponseEntity
                .created(URI.create("/api/vehiculos/" + creado.getId()))
                .body(creado);
    }

    
    @PutMapping("/{id}")
    public VehiculoResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody VehiculoRequest request) {
        return servicio.actualizar(id, request);
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
