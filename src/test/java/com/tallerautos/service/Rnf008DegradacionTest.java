package com.tallerautos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.tallerautos.client.TasaCambioClient;
import com.tallerautos.entity.Cliente;
import com.tallerautos.entity.Vehiculo;
import com.tallerautos.entity.Venta;


class Rnf008DegradacionTest {

    private static final BigDecimal VALOR = new BigDecimal("120000000.00");
    private static final BigDecimal TOTAL_CON_DESCUENTO = new BigDecimal("114000000.00");

    
    private static final BigDecimal RESPALDO = new BigDecimal("3200");

    private TasaCambioService servicioCon(RuntimeException fallo) {
        
        
        
        TasaCambioClient client = new TasaCambioClient(
                RestClient.builder(), "http://localhost:9999/no-existe", 100L) {
            @Override
            public TasaCambio consultar() {
                if (fallo != null) throw fallo;
                return new TasaCambio(new BigDecimal("3236.8410"),
                        "Thu, 08 Oct 2026 00:02:31 +0000",
                        LocalDateTime.now(), true);
            }
        };
        return new TasaCambioService(client, RESPALDO.toPlainString(), 30);
    }

    @Test
    @DisplayName("RNF-008: con la API caida, la venta NO guarda precio en dolares")
    void laVentaNoGuardaUnValorCalculadoConTasaFalsa() {
        TasaCambioService servicio = servicioCon(new RestClientException("no responde"));

        TasaCambioService.Conversion conversion = servicio.convertirConTasa(TOTAL_CON_DESCUENTO);

        assertNull(conversion.valorUsd(),
                "No se debe calcular precio_usd con la tasa de respaldo: "
                        + "quedaria congelado para siempre en un registro inmutable");

        assertNull(conversion.tasa(),
                "Tampoco debe quedar congelada una tasa que no vino de la API");
    }

    @Test
    @DisplayName("RNF-008: el total en pesos, que es lo que se cobra, siempre se guarda")
    void elTotalEnPesosNoDependeDeLaApiExterna() {
        
        VentaService ventaService = new VentaService(null, null, null, null, null);

        BigDecimal descuento = ventaService.calcularDescuento(VALOR);
        BigDecimal total = ventaService.calcularTotal(VALOR, descuento);

        assertEquals(0, total.compareTo(TOTAL_CON_DESCUENTO));
    }

    @Test
    @DisplayName("RNF-008: una LECTURA si degrada, con la tasa de respaldo")
    void unaLecturaSiUsaElRespaldo() {
        TasaCambioService servicio = servicioCon(new RestClientException("no responde"));

        BigDecimal precioUsd = servicio.convertir(TOTAL_CON_DESCUENTO);

        assertTrue(precioUsd != null && precioUsd.signum() > 0,
                "Al mostrar un precio el vendedor quiere un numero, no un guion");

        
        assertEquals(0, precioUsd.compareTo(new BigDecimal("35625.00")));
    }

    @Test
    @DisplayName("Con la API viva, la venta si guarda precio en dolares y la tasa")
    void conLaApiVivaSeGuardanLosDosCampos() {
        TasaCambioService servicio = servicioCon(null);

        TasaCambioService.Conversion conversion = servicio.convertirConTasa(TOTAL_CON_DESCUENTO);

        assertTrue(conversion.valorUsd() != null && conversion.valorUsd().signum() > 0);
        assertEquals(0, conversion.tasa().compareTo(new BigDecimal("3236.8410")));
    }

    @Test
    @DisplayName("Un fallo no se cachea: la peticion siguiente reintenta")
    void unFalloNoSeCachea() {
        TasaCambioService servicio = servicioCon(new RestClientException("no responde"));

        
        assertNull(servicio.convertirConTasa(TOTAL_CON_DESCUENTO).valorUsd());

        
        
        assertNull(servicio.convertirConTasa(TOTAL_CON_DESCUENTO).valorUsd(),
                "El fallo no debe quedar cacheado como si fuera una tasa valida");
    }

    @Test
    @DisplayName("La venta se guarda igual: total y descuento no dependen de la tasa")
    void laVentaSeGuardaIgualConTotalYDescuento() {
        Venta venta = new Venta(
                new Cliente("Camila", "camila@correo.com", null, null),
                new Vehiculo("UJE201", "Toyota", "Hilux", 2024, "Blanco", VALOR),
                LocalDateTime.now(),
                VALOR,
                new BigDecimal("5"),
                TOTAL_CON_DESCUENTO,
                null,                       
                null);                      

        assertNull(venta.getPrecioUsd());
        assertNull(venta.getTasaCambio());
        assertEquals(0, venta.getTotal().compareTo(TOTAL_CON_DESCUENTO));
        assertEquals(0, venta.getValorVehiculo().compareTo(VALOR));
    }
}
