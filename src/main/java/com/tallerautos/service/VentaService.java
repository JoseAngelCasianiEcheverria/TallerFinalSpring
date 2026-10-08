package com.tallerautos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tallerautos.dto.request.VentaRequest;
import com.tallerautos.dto.response.SimulacionVentaResponse;
import com.tallerautos.dto.response.VentaResponse;
import com.tallerautos.entity.Cliente;
import com.tallerautos.entity.EstadoVehiculo;
import com.tallerautos.entity.Vehiculo;
import com.tallerautos.entity.Venta;
import com.tallerautos.exception.NotFoundException;
import com.tallerautos.exception.VehiculoNoVendibleException;
import com.tallerautos.mapper.VentaMapper;
import com.tallerautos.repository.ClienteRepository;
import com.tallerautos.repository.VehiculoRepository;
import com.tallerautos.repository.VentaRepository;
import com.tallerautos.service.TasaCambioService.Conversion;


@Service
@Transactional(readOnly = true)
public class VentaService {

    
    static final BigDecimal UMBRAL_DESCUENTO = new BigDecimal("100000000");

    
    static final BigDecimal PORCENTAJE_DESCUENTO = new BigDecimal("5");

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final int ESCALA = 2;

    private final VentaRepository repositorio;
    private final ClienteRepository clienteRepositorio;
    private final VehiculoRepository vehiculoRepositorio;
    private final VentaMapper mapper;
    private final TasaCambioService tasaCambioService;

    public VentaService(VentaRepository repositorio,
                        ClienteRepository clienteRepositorio,
                        VehiculoRepository vehiculoRepositorio,
                        VentaMapper mapper,
                        TasaCambioService tasaCambioService) {
        this.repositorio = repositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.mapper = mapper;
        this.tasaCambioService = tasaCambioService;
    }

    
    @Transactional
    public VentaResponse registrar(VentaRequest request) {

        
        
        Cliente cliente = clienteRepositorio.findById(request.getClienteId())
                .orElseThrow(() -> new NotFoundException(
                        "No existe un cliente con el identificador " + request.getClienteId() + "."));

        
        
        Vehiculo vehiculo = vehiculoRepositorio.findByIdParaActualizar(request.getVehiculoId())
                .orElseThrow(() -> new NotFoundException(
                        "No existe un vehiculo con el identificador " + request.getVehiculoId() + "."));

        
        if (vehiculo.getEstado() != EstadoVehiculo.DISPONIBLE) {
            throw new VehiculoNoVendibleException(vehiculo.getPlaca(), vehiculo.getEstado());
        }

        
        
        BigDecimal valor = vehiculo.getPrecio();
        BigDecimal descuento = calcularDescuento(valor);
        BigDecimal total = calcularTotal(valor, descuento);

        
        
        Conversion conversion = tasaCambioService.convertirConTasa(total);

        Venta venta = new Venta(
                cliente,
                vehiculo,
                LocalDateTime.now(),
                valor,
                descuento,
                total,
                conversion.tasa(),
                conversion.valorUsd());

        Venta guardada = repositorio.save(venta);

        
        
        vehiculo.setEstado(EstadoVehiculo.VENDIDO);
        vehiculoRepositorio.save(vehiculo);

        return mapper.aResponse(guardada);
    }

    
    @Transactional(readOnly = true)
    public SimulacionVentaResponse simular(Long vehiculoId) {
        Vehiculo vehiculo = vehiculoRepositorio.findById(vehiculoId)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un vehiculo con el identificador " + vehiculoId + "."));

        BigDecimal valor = vehiculo.getPrecio();
        BigDecimal descuento = calcularDescuento(valor);
        BigDecimal total = calcularTotal(valor, descuento);

        Conversion conversion = tasaCambioService.convertirConTasa(total);

        return new SimulacionVentaResponse(
                vehiculo.getId(),
                vehiculo.getPlaca(),
                vehiculo.getMarca(),
                vehiculo.getModelo(),
                vehiculo.getEstado().name(),
                valor,
                descuento,
                total,
                conversion.tasa(),
                conversion.valorUsd(),
                explicarDescuento(valor, descuento));
    }

    
    private String explicarDescuento(BigDecimal valor, BigDecimal descuento) {
        if (descuento.signum() > 0) {
            return "El valor supera los $100.000.000, así que se aplica un "
                    + descuento.toPlainString() + " % de descuento.";
        }
        if (valor.compareTo(UMBRAL_DESCUENTO) == 0) {
            return "El valor es exactamente $100.000.000. Como la regla dice "
                    + "\"supera\" y no \"igual o supera\", este precio no lleva descuento.";
        }
        return "El valor no supera los $100.000.000, así que aquí no hay descuento.";
    }

    
    public List<VentaResponse> listar() {
        return repositorio.findAllConRelaciones().stream()
                .map(mapper::aResponse)
                .toList();
    }

    
    public List<VentaResponse> listarPorCliente(Long clienteId) {
        if (!clienteRepositorio.existsById(clienteId)) {
            throw new NotFoundException("No existe un cliente con el identificador " + clienteId + ".");
        }
        return repositorio.findByClienteIdConRelaciones(clienteId).stream()
                .map(mapper::aResponse)
                .toList();
    }

    
    public List<VentaResponse> listarPorVehiculo(Long vehiculoId) {
        if (!vehiculoRepositorio.existsById(vehiculoId)) {
            throw new NotFoundException("No existe un vehiculo con el identificador " + vehiculoId + ".");
        }
        return repositorio.findByVehiculoIdConRelaciones(vehiculoId).stream()
                .map(mapper::aResponse)
                .toList();
    }

    
    BigDecimal calcularDescuento(BigDecimal valor) {
        return valor.compareTo(UMBRAL_DESCUENTO) > 0
                ? PORCENTAJE_DESCUENTO
                : BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);
    }

    
    BigDecimal calcularTotal(BigDecimal valor, BigDecimal descuento) {
        BigDecimal factor = BigDecimal.ONE.subtract(descuento.divide(CIEN, 10, RoundingMode.HALF_UP));
        return valor.multiply(factor).setScale(ESCALA, RoundingMode.HALF_UP);
    }
}
