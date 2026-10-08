package com.tallerautos.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tallerautos.dto.request.MantenimientoRequest;
import com.tallerautos.dto.request.MantenimientoUpdateRequest;
import com.tallerautos.dto.response.MantenimientoResponse;
import com.tallerautos.entity.EstadoMantenimiento;
import com.tallerautos.entity.EstadoVehiculo;
import com.tallerautos.entity.Mantenimiento;
import com.tallerautos.entity.Vehiculo;
import com.tallerautos.exception.BusinessRuleException;
import com.tallerautos.exception.MantenimientoCerradoException;
import com.tallerautos.exception.NotFoundException;
import com.tallerautos.mapper.MantenimientoMapper;
import com.tallerautos.repository.MantenimientoRepository;
import com.tallerautos.repository.VehiculoRepository;


@Service
@Transactional(readOnly = true)
public class MantenimientoService {

    private final MantenimientoRepository repositorio;
    private final VehiculoRepository vehiculoRepositorio;
    private final MantenimientoMapper mapper;

    public MantenimientoService(MantenimientoRepository repositorio,
                                VehiculoRepository vehiculoRepositorio,
                                MantenimientoMapper mapper) {
        this.repositorio = repositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.mapper = mapper;
    }

    
    @Transactional
    public MantenimientoResponse registrar(MantenimientoRequest request) {
        Vehiculo vehiculo = obtenerVehiculo(request.getVehiculoId());

        
        
        if (vehiculo.getEstado() == EstadoVehiculo.VENDIDO) {
            throw new BusinessRuleException(
                    "El vehiculo con placa " + vehiculo.getPlaca()
                            + " ya fue vendido y no admite mantenimientos.");
        }

        Mantenimiento mantenimiento = new Mantenimiento(
                vehiculo,
                request.getTipo().trim(),
                (request.getDescripcion() == null) ? null : request.getDescripcion().trim(),
                request.getCosto(),
                LocalDateTime.now(),
                null,
                EstadoMantenimiento.EN_PROCESO);

        Mantenimiento guardado = repositorio.save(mantenimiento);

        
        vehiculo.setEstado(EstadoVehiculo.EN_MANTENIMIENTO);
        vehiculoRepositorio.save(vehiculo);

        return mapper.aResponse(guardado);
    }

    
    public List<MantenimientoResponse> historialPorVehiculo(Long vehiculoId) {
        obtenerVehiculo(vehiculoId);

        return repositorio.findByVehiculoIdOrderByFechaInicioDesc(vehiculoId).stream()
                .map(mapper::aResponse)
                .toList();
    }

    
    public List<MantenimientoResponse> listar() {
        return repositorio.findAllConVehiculo().stream()
                .map(mapper::aResponse)
                .toList();
    }

    
    @Transactional
    public MantenimientoResponse cerrar(Long id) {
        Mantenimiento mantenimiento = obtener(id);

        if (mantenimiento.getEstado() == EstadoMantenimiento.FINALIZADO) {
            throw new MantenimientoCerradoException(id);
        }

        mantenimiento.setEstado(EstadoMantenimiento.FINALIZADO);
        mantenimiento.setFechaFin(LocalDateTime.now());
        Mantenimiento guardado = repositorio.save(mantenimiento);

        Vehiculo vehiculo = mantenimiento.getVehiculo();
        if (vehiculo != null && vehiculo.getEstado() == EstadoVehiculo.EN_MANTENIMIENTO
                && !repositorio.existeAbiertoDe(vehiculo.getId())) {
            vehiculo.setEstado(EstadoVehiculo.DISPONIBLE);
            vehiculoRepositorio.save(vehiculo);
        }

        return mapper.aResponse(guardado);
    }

    
    @Transactional
    public MantenimientoResponse actualizar(Long id, MantenimientoUpdateRequest request) {
        Mantenimiento mantenimiento = obtener(id);

        if (mantenimiento.getEstado() == EstadoMantenimiento.FINALIZADO) {
            throw new MantenimientoCerradoException(id);
        }

        if (request.getDescripcion() != null) {
            mantenimiento.setDescripcion(request.getDescripcion().trim());
        }
        if (request.getCosto() != null) {
            mantenimiento.setCosto(request.getCosto());
        }

        return mapper.aResponse(repositorio.save(mantenimiento));
    }

    private Mantenimiento obtener(Long id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un mantenimiento con el identificador " + id + "."));
    }

    private Vehiculo obtenerVehiculo(Long id) {
        return vehiculoRepositorio.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un vehiculo con el identificador " + id + "."));
    }
}
