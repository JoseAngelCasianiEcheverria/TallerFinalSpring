package com.tallerautos.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tallerautos.entity.Cliente;
import com.tallerautos.entity.EstadoMantenimiento;
import com.tallerautos.entity.EstadoVehiculo;
import com.tallerautos.entity.Mantenimiento;
import com.tallerautos.entity.Vehiculo;
import com.tallerautos.repository.ClienteRepository;
import com.tallerautos.repository.MantenimientoRepository;
import com.tallerautos.repository.VehiculoRepository;


@Component
@ConditionalOnProperty(name = "datos.iniciales.activar", havingValue = "true", matchIfMissing = true)
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final ClienteRepository clienteRepositorio;
    private final VehiculoRepository vehiculoRepositorio;
    private final MantenimientoRepository mantenimientoRepositorio;

    public DatosIniciales(ClienteRepository clienteRepositorio,
                          VehiculoRepository vehiculoRepositorio,
                          MantenimientoRepository mantenimientoRepositorio) {
        this.clienteRepositorio = clienteRepositorio;
        this.vehiculoRepositorio = vehiculoRepositorio;
        this.mantenimientoRepositorio = mantenimientoRepositorio;
    }

    @Override
    @Transactional
    public void run(String... args) {

        
        
        
        
        
        
        boolean hayClientes = clienteRepositorio.count() > 0;
        boolean hayVehiculos = vehiculoRepositorio.count() > 0;

        if (hayClientes && hayVehiculos) {
            log.info("Ya hay datos en la base. No se insertan los datos iniciales.");
            return;
        }

        if (hayClientes) {
            log.warn("Hay clientes pero no hay vehiculos: se reponen solo los vehiculos "
                    + "de ejemplo y los clientes que haya se dejan intactos.");
        } else if (hayVehiculos) {
            log.warn("Hay vehiculos pero no hay clientes: se reponen solo los clientes "
                    + "de ejemplo y los vehiculos que haya se dejan intactos.");
        }

        if (!hayClientes) {
            clienteRepositorio.saveAll(List.of(
                new Cliente("Karen Lucia Zapata Casta\u00f1o", "karen.zapata@correo.com",
                        "3001234567", "Calle 10 # 5-30, Cartagena"),
                new Cliente("Jose Casiani", "jose.casiani@correo.com",
                        "3112223344", "Avenida 68 # 120-45, Cartagena"),
                new Cliente("Yanileth Echeverria", "yanileth.echeverria@correo.com",
                        "3209876543", "Carrera 7 # 45-10, Cartagena"),
                new Cliente("Jose Blanco", "jose.blanco@correo.com",
                        "3154447788", "Barrio La Castellana, Cartagena")));
        }

        if (!hayVehiculos) {
            List<Vehiculo> vehiculos = vehiculoRepositorio.saveAll(List.of(
                
                new Vehiculo("CTG201", "Ferrari", "488 Spider", 2024, "Rosso Corsa",
                        new BigDecimal("120000000.00")),

                
                new Vehiculo("CTG202", "Ferrari", "F8 Tributo", 2023, "Giallo Modena",
                        new BigDecimal("100000000.00")),

                
                new Vehiculo("CTG203", "Ferrari", "Roma", 2023, "Nero Daytona",
                        new BigDecimal("85000000.00")),

                
                new Vehiculo("CTG205", "Ferrari", "SF90 Stradale", 2025, "Rosso Scuderia",
                        new BigDecimal("165000000.00")),

                
                new Vehiculo("CTG204", "Ferrari", "296 GTB", 2022, "Giallo Modena",
                        new BigDecimal("72000000.00"))));

        
            
            
            
            
            
            
            
            
            vehiculoRepositorio.findByPlaca("CTG204").ifPresent(vehiculo -> {
                vehiculo.setEstado(EstadoVehiculo.EN_MANTENIMIENTO);
                vehiculoRepositorio.save(vehiculo);

                mantenimientoRepositorio.save(new Mantenimiento(
                        vehiculo,
                        "Cambio de aceite y filtros",
                        "Mantenimiento correctivo-preventivo programado en la revision de los 20000 km.",
                        new BigDecimal("480000.00"),
                        LocalDateTime.now().minusDays(1),
                        null,
                        EstadoMantenimiento.EN_PROCESO));
            });

            log.info("Vehiculos de ejemplo repuestos: {}.", vehiculos.size());
            log.info("Para las pruebas: CTG201 (120M, con descuento), "
                    + "CTG202 (100M, sin descuento, caso limite), CTG203 (disponible), "
                    + "CTG204 (en servicio), CTG205 (para el filtro por marca).");
        }
    }
}
