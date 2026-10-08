package com.tallerautos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


class VentaServiceTest {

    
    private final VentaService servicio = new VentaService(null, null, null, null, null);

    private static final BigDecimal CIEN_MILLONES = new BigDecimal("100000000.00");

    @Test
    @DisplayName("RN-6: por debajo de 100M no hay descuento")
    void sinDescuentoPorDebajoDelUmbral() {
        BigDecimal descuento = servicio.calcularDescuento(new BigDecimal("85000000.00"));
        assertEquals(0, descuento.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("RN-6: exactamente 100M NO recibe descuento, porque dice 'supera'")
    void sinDescuentoEnElUmbralExacto() {
        BigDecimal descuento = servicio.calcularDescuento(CIEN_MILLONES);
        assertEquals(0, descuento.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("RN-6: un peso por encima de 100M ya recibe descuento")
    void conDescuentoUnPesoArribaDelUmbral() {
        BigDecimal descuento = servicio.calcularDescuento(new BigDecimal("100000000.01"));
        assertEquals(0, descuento.compareTo(new BigDecimal("5")));
    }

    @Test
    @DisplayName("RN-6: 120M con 5 % da 114.000.000")
    void totalConDescuento() {
        BigDecimal valor = new BigDecimal("120000000.00");
        BigDecimal total = servicio.calcularTotal(valor, servicio.calcularDescuento(valor));

        assertEquals(0, total.compareTo(new BigDecimal("114000000.00")));
    }

    @Test
    @DisplayName("RN-6: sin descuento el total es el valor")
    void totalSinDescuento() {
        BigDecimal valor = new BigDecimal("85000000.50");
        BigDecimal total = servicio.calcularTotal(valor, servicio.calcularDescuento(valor));

        assertEquals(0, total.compareTo(new BigDecimal("85000000.50")));
    }

    
    @Test
    @DisplayName("RN-6: compareTo y no equals. La escala no debe cambiar el resultado")
    void comparacionNoDependeDeLaEscala() {

        assertFalse(
                new BigDecimal("100000000").equals(new BigDecimal("100000000.00")),
                "Si equals devolviera true, este test no distinguiria los dos casos");

        assertEquals(
                servicio.calcularDescuento(new BigDecimal("100000000")),
                servicio.calcularDescuento(new BigDecimal("100000000.00")));
    }
}
