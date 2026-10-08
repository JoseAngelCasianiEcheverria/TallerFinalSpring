package com.tallerfinal.springfinal.config;

import java.util.stream.Collectors;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * Convierte {@link ConfiguracionBaseDeDatosException} en el bloque "APPLICATION
 * FAILED TO START" de Spring Boot, en español (RNF-8). Registrado en
 * {@code META-INF/spring.factories}.
 */
public class ConfiguracionBaseDeDatosFailureAnalyzer extends AbstractFailureAnalyzer<ConfiguracionBaseDeDatosException> {

	private static final String ACCION = """
			Define las tres variables de entorno en la misma terminal antes de arrancar la API. En PowerShell:

			    $env:DB_URL = "jdbc:postgresql://localhost:5432/autodrive"
			    $env:DB_USUARIO = "postgres"
			    $env:DB_CONTRASENA = "tu contraseña"
			    ./mvnw spring-boot:run

			Con Neon, la URL lleva el host de tu proyecto y no lleva usuario ni contraseña:
			jdbc:postgresql://<host-de-neon>/autodrive?sslmode=require (el usuario y la contraseña van en
			DB_USUARIO y DB_CONTRASENA). La cadena postgresql://usuario:contraseña@... no sirve tal cual.""";

	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, ConfiguracionBaseDeDatosException causa) {
		String descripcion = causa.getProblemas()
				.stream()
				.map(problema -> "    - " + problema)
				.collect(Collectors.joining("\n", "No se puede conectar a la base de datos PostgreSQL:\n\n", ""));
		return new FailureAnalysis(descripcion, ACCION, causa);
	}

}
