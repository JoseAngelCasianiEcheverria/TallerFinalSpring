package com.tallerautos.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tallerautos.dto.request.VehiculoRequest;
import com.tallerautos.dto.response.VehiculoResponse;
import com.tallerautos.entity.EstadoVehiculo;
import com.tallerautos.entity.Vehiculo;
import com.tallerautos.exception.ConHistorialException;
import com.tallerautos.exception.NotFoundException;
import com.tallerautos.exception.PlacaDuplicadaException;
import com.tallerautos.mapper.VehiculoMapper;
import com.tallerautos.repository.VehiculoRepository;


@Service
@Transactional(readOnly = true)
public class VehiculoService {

    private final VehiculoRepository repositorio;
    private final VehiculoMapper mapper;
    private final TasaCambioService tasaCambioService;

    public VehiculoService(VehiculoRepository repositorio,
                           VehiculoMapper mapper,
                           TasaCambioService tasaCambioService) {
        this.repositorio = repositorio;
        this.mapper = mapper;
        this.tasaCambioService = tasaCambioService;
    }

    
    public List<VehiculoResponse> listar() {
        return repositorio.findAll().stream()
                .map(mapper::aResponse)
                .toList();
    }

    
    public VehiculoResponse buscarPorId(Long id) {
        Vehiculo vehiculo = obtener(id);
        BigDecimal precioUsd = tasaCambioService.convertir(vehiculo.getPrecio());

        return mapper.aResponse(vehiculo, precioUsd, null);
    }

    
    @Transactional
    public VehiculoResponse registrar(VehiculoRequest request) {
        String placa = request.getPlaca().trim().toUpperCase();

        if (repositorio.existsByPlaca(placa)) {
            throw new PlacaDuplicadaException(placa);
        }

        Vehiculo vehiculo = new Vehiculo(
                placa,
                request.getMarca().trim(),
                request.getModelo().trim(),
                request.getAnio(),
                (request.getColor() == null) ? null : request.getColor().trim(),
                request.getPrecio());

        
        return mapper.aResponse(repositorio.save(vehiculo));
    }

    
    @Transactional
    public VehiculoResponse actualizar(Long id, VehiculoRequest request) {
        Vehiculo vehiculo = obtener(id);
        String placa = request.getPlaca().trim().toUpperCase();

        if (repositorio.existsByPlacaAndIdNot(placa, id)) {
            throw new PlacaDuplicadaException(placa);
        }

        vehiculo.setPlaca(placa);
        vehiculo.setMarca(request.getMarca().trim());
        vehiculo.setModelo(request.getModelo().trim());
        vehiculo.setAnio(request.getAnio());
        vehiculo.setColor(request.getColor() == null ? null : request.getColor().trim());
        vehiculo.setPrecio(request.getPrecio());

        return mapper.aResponse(repositorio.save(vehiculo));
    }

    
    @Transactional
    public void eliminar(Long id) {
        Vehiculo vehiculo = obtener(id);

        if (repositorio.existeVentaDe(id)) {
            throw new ConHistorialException(
                    "el vehiculo con placa " + vehiculo.getPlaca(), "al menos una venta");
        }
        if (repositorio.existeMantenimientoDe(id)) {
            throw new ConHistorialException(
                    "el vehiculo con placa " + vehiculo.getPlaca(), "al menos un mantenimiento");
        }

        repositorio.delete(vehiculo);
    }

    
    public List<VehiculoResponse> buscarPorMarca(String marca) {
        return repositorio.buscarPorMarca(marca.trim()).stream()
                .map(mapper::aResponse)
                .toList();
    }

    
    public List<VehiculoResponse> listarDisponibles() {
        return repositorio.findByEstadoOrderByPrecioAsc(EstadoVehiculo.DISPONIBLE).stream()
                .map(mapper::aResponse)
                .toList();
    }

    Vehiculo obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un vehiculo con el identificador " + id + "."));
    }

    Vehiculo obtenerParaActualizar(Long id) {
        return repositorio.findByIdParaActualizar(id)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un vehiculo con el identificador " + id + "."));
    }
}
