package com.tallerfinal.springfinal.exception;

/** El pedido viola una regla de negocio o de unicidad → 409 (RF-4). El mensaje explica la regla. */
public class ConflictoException extends RuntimeException {

	public ConflictoException(String mensaje) {
		super(mensaje);
	}

}
