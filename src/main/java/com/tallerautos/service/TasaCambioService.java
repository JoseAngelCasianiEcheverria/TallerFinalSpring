package com.tallerautos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import com.tallerautos.client.TasaCambioClient;
import com.tallerautos.client.TasaCambioClient.TasaCambio;
import com.tallerautos.dto.response.TasaCambioResponse;


@Service
public class TasaCambioService {

    private static final Logger log = LoggerFactory.getLogger(TasaCambioService.class);
    private static final int ESCALA_TASA = 4;
    private static final int ESCALA_USD = 2;
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final TasaCambioClient client;
    private final BigDecimal valorPorDefecto;
    private final Duration cacheMinutos;

    private final Object candado = new Object();
    private TasaCambio cache;

    public TasaCambioService(TasaCambioClient client,
                             @Value("${tasa.valor-por-defecto:3200}") String valorPorDefecto,
                             @Value("${tasa.cache-minutes:30}") long cacheMinutos) {
        this.client = client;
        this.valorPorDefecto = new BigDecimal(valorPorDefecto);
        this.cacheMinutos = Duration.ofMinutes(cacheMinutos);
    }

    
    @Transactional(readOnly = true)
    public BigDecimal convertir(BigDecimal valorCop) {
        return convertirParaMostrar(valorCop).valorUsd();
    }

    private Conversion convertirParaMostrar(BigDecimal valorCop) {
        if (valorCop == null) {
            return new Conversion(null, null);
        }

        TasaCambio tasa = tasaConRespaldo();
        if (tasa == null || tasa.tasa() == null || tasa.tasa().signum() <= 0) {
            return new Conversion(null, null);
        }

        return new Conversion(
                valorCop.divide(tasa.tasa(), ESCALA_USD, RoundingMode.HALF_UP),
                tasa.tasa());
    }

    
    @Transactional(readOnly = true)
    public Conversion convertirConTasa(BigDecimal valorCop) {
        if (valorCop == null) {
            return new Conversion(null, null);
        }

        TasaCambio tasa = tasaReal();
        if (tasa == null || tasa.tasa() == null || tasa.tasa().signum() <= 0) {
            return new Conversion(null, null);
        }

        BigDecimal valorUsd = valorCop.divide(tasa.tasa(), ESCALA_USD, RoundingMode.HALF_UP);

        return new Conversion(valorUsd, tasa.tasa());
    }

    
    public record Conversion(BigDecimal valorUsd, BigDecimal tasa) {
    }

    
    @Transactional(readOnly = true)
    public TasaCambioResponse obtenerTasa() {
        TasaCambio tasa = tasaConRespaldo();

        return new TasaCambioResponse(
                "USD",
                "COP",
                tasa.tasa(),
                tasa.fechaActualizacion(),
                tasa.desdeApi() ? "api-externa" : "valor-por-defecto");
    }

    
    private TasaCambio tasaReal() {
        TasaCambio vigente = leerCache();
        if (vigente != null) {
            return vigente;
        }

        synchronized (candado) {
            vigente = leerCache();
            if (vigente != null) {
                return vigente;
            }

            try {
                TasaCambio obtenida = client.consultar();
                log.info("Tasa de cambio obtenida de la API externa: {} COP por USD", obtenida.tasa());
                guardarCache(obtenida);
                return cache;
            } catch (RestClientException | IllegalStateException e) {
                
                
                log.warn("La API externa de tasa de cambio fallo ({}). "
                        + "La venta se guardara sin el equivalente en dolares (RNF-008).",
                        e.getMessage());
                return null;
            }
        }
    }

    
    private TasaCambio tasaConRespaldo() {
        TasaCambio real = tasaReal();
        if (real != null) {
            return real;
        }

        return new TasaCambio(
                valorPorDefecto.setScale(ESCALA_TASA, RoundingMode.HALF_UP),
                null,
                LocalDateTime.now(),
                false);
    }

    private TasaCambio leerCache() {
        if (cache == null || cache.consultadoEn() == null) {
            return null;
        }
        if (Duration.between(cache.consultadoEn(), LocalDateTime.now()).compareTo(cacheMinutos) < 0) {
            return cache;
        }
        return null;
    }

    private void guardarCache(TasaCambio tasa) {
        this.cache = new TasaCambio(
                tasa.tasa().setScale(ESCALA_TASA, RoundingMode.HALF_UP),
                tasa.fechaActualizacion(),
                LocalDateTime.now(),
                tasa.desdeApi());
    }

    
    public void invalidarCache() {
        synchronized (candado) {
            this.cache = null;
        }
    }
}
