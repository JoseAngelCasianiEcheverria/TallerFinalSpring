package com.tallerfinal.springfinal.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato común de todos los errores de la API (RF-6). {@code campos} solo
 * aparece en los errores de validación.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespuestaErrorDto(OffsetDateTime fecha, int estado, String error, String mensaje, String ruta,
		List<CampoInvalidoDto> campos) {
}
