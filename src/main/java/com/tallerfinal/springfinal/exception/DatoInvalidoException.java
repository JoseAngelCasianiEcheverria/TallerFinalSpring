package com.tallerfinal.springfinal.exception;

/**
 * Un dato del pedido es inválido por una regla que solo el servicio puede
 * revisar (por ejemplo el año del vehículo contra la fecha actual) → 400 con
 * el campo (RF-1).
 */
public class DatoInvalidoException extends RuntimeException {

	private final String campo;

	public DatoInvalidoException(String campo, String mensaje) {
		super(mensaje);
		this.campo = campo;
	}

	public String getCampo() {
		return campo;
	}

}
