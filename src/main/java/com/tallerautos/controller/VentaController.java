package com.tallerautos.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tallerautos.dto.request.VentaRequest;
import com.tallerautos.dto.response.SimulacionVentaResponse;
import com.tallerautos.dto.response.VentaResponse;
import com.tallerautos.service.VentaService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService servicio;

    public VentaController(VentaService servicio) {
        this.servicio = servicio;
    }

    
    @GetMapping
    public List<VentaResponse> listar() {
        return servicio.listar();
    }

    
    @GetMapping("/simular/{vehiculoId}")
    public SimulacionVentaResponse simular(@PathVariable Long vehiculoId) {
        return servicio.simular(vehiculoId);
    }

    
    @GetMapping("/cliente/{clienteId}")
    public List<VentaResponse> listarPorCliente(@PathVariable Long clienteId) {
        return servicio.listarPorCliente(clienteId);
    }

    
    @GetMapping("/vehiculo/{vehiculoId}")
    public List<VentaResponse> listarPorVehiculo(@PathVariable Long vehiculoId) {
        return servicio.listarPorVehiculo(vehiculoId);
    }

    
    @PostMapping
    public ResponseEntity<VentaResponse> registrar(@Valid @RequestBody VentaRequest request) {
        VentaResponse creada = servicio.registrar(request);
        return ResponseEntity
                .created(URI.create("/api/ventas/" + creada.getId()))
                .body(creada);
    }
}
