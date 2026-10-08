package com.tallerfinal.springfinal.exception;

/** Un recurso pedido por su identificador no existe → 404 (RF-3). */
public class RecursoNoEncontradoException extends RuntimeException {

	/**
	 * @param recurso el recurso con su artículo, por ejemplo "el cliente" o "la venta"
	 * @param id el identificador pedido
	 */
	public RecursoNoEncontradoException(String recurso, Object id) {
		super("No existe " + recurso + " con id " + id);
	}

}
