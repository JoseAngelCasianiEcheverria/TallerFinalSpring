package com.tallerautos.client;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;


@Component
public class TasaCambioClient {

    private static final Logger log = LoggerFactory.getLogger(TasaCambioClient.class);

    private final RestClient restClient;
    private final String url;
    private final Duration timeout;

    public TasaCambioClient(RestClient.Builder builder,
                            @Value("${tasa.api.url}") String url,
                            @Value("${tasa.api.timeout-ms:3000}") long timeoutMs) {
        this.restClient = builder.build();
        this.url = url;
        this.timeout = Duration.ofMillis(timeoutMs);
    }

    
    public TasaCambio consultar() {
        RespuestaApi respuesta = restClient.get()
                .uri(url)
                .retrieve()
                .body(RespuestaApi.class);

        if (respuesta == null || !"success".equals(respuesta.result())) {
            throw new IllegalStateException(
                    "La API de tasa de cambio no devolvio un resultado exitoso.");
        }

        BigDecimal cop = respuesta.rates() == null ? null : respuesta.rates().get("COP");
        if (cop == null || cop.signum() <= 0) {
            throw new IllegalStateException(
                    "La API de tasa de cambio no devolvio la tasa COP.");
        }

        if (respuesta.timeLastUpdateUtc() == null) {
            log.warn("La API no devolvio time_last_update_utc. "
                    + "Revisa el mapeo de RespuestaApi: si falta el @JsonProperty, "
                    + "el campo llega null en silencio.");
        }

        log.debug("Tasa obtenida de la API externa: {} COP por {} (publicada el {})",
                cop, respuesta.baseCode(), respuesta.timeLastUpdateUtc());

        return new TasaCambio(
                cop,
                respuesta.timeLastUpdateUtc(),
                LocalDateTime.now(),
                true);
    }

    
    public Duration timeout() {
        return timeout;
    }

    
    public record TasaCambio(BigDecimal tasa, String fechaActualizacion,
                             LocalDateTime consultadoEn, boolean desdeApi) {
    }

    
    @SuppressWarnings("unused")
    private record RespuestaApi(
            String result,
            @JsonProperty("base_code") String baseCode,
            @JsonProperty("time_last_update_utc") String timeLastUpdateUtc,
            Map<String, BigDecimal> rates) {
    }
}
