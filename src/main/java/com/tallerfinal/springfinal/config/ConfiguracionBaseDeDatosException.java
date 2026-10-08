package com.tallerfinal.springfinal.config;

import java.util.List;

public class ConfiguracionBaseDeDatosException extends RuntimeException {

	private final List<String> problemas;

	public ConfiguracionBaseDeDatosException(List<String> problemas) {
		super("Configuración de la base de datos incompleta: " + String.join("; ", problemas));
		this.problemas = List.copyOf(problemas);
	}

	public List<String> getProblemas() {
		return problemas;
	}

}
