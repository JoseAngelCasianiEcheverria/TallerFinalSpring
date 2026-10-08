package com.tallerfinal.springfinal.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.diagnostics.FailureAnalysis;

class ConfiguracionBaseDeDatosFailureAnalyzerTest {

	@Test
	void explicaEnEspanolQueFaltaYComoCorregirlo() {
		ConfiguracionBaseDeDatosException causa = new ConfiguracionBaseDeDatosException(
				List.of("DB_URL no está definida", "DB_CONTRASENA no está definida"));

		FailureAnalysis analisis = new ConfiguracionBaseDeDatosFailureAnalyzer()
				.analyze(new BeanCreationException("arranque", causa));

		assertThat(analisis).isNotNull();
		assertThat(analisis.getDescription()).contains("base de datos")
				.contains("DB_URL no está definida")
				.contains("DB_CONTRASENA no está definida");
		assertThat(analisis.getAction()).contains("$env:DB_URL")
				.contains("jdbc:postgresql://")
				.contains("DB_USUARIO")
				.contains("DB_CONTRASENA")
				.contains("Neon");
		assertThat(analisis.getCause()).isSameAs(causa);
	}

}
