package com.tallerfinal.springfinal.exception;

/** Un servicio externo que la API necesita no está disponible → 503 (RF-86). */
public class ServicioNoDisponibleException extends RuntimeException {

	public ServicioNoDisponibleException(String mensaje) {
		super(mensaje);
	}

}
