package com.tallerfinal.springfinal;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.jupiter.api.Test;

/**
 * La conexión a la base se lee de variables de entorno: ningún archivo
 * versionado guarda la URL, el usuario ni la contraseña (RNF-1).
 */
class ConfiguracionTest {

	@Test
	void laBaseSeLeeDeVariablesDeEntorno() throws IOException {
		Properties propiedades = new Properties();
		try (InputStream entrada = getClass().getResourceAsStream("/application.properties")) {
			propiedades.load(entrada);
		}

		assertThat(propiedades.getProperty("spring.datasource.url")).isEqualTo("${DB_URL}");
		assertThat(propiedades.getProperty("spring.datasource.username")).isEqualTo("${DB_USUARIO}");
		assertThat(propiedades.getProperty("spring.datasource.password")).isEqualTo("${DB_CONTRASENA}");
	}

}
