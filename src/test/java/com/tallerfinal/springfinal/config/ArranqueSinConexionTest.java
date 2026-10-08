package com.tallerfinal.springfinal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Objects;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.tallerfinal.springfinal.SpringfinalApplication;

/**
 * Arranca la aplicación de verdad (sin servidor web) con la conexión mal
 * configurada: el chequeo tiene que cortar el arranque antes de tocar la base
 * y Spring Boot tiene que mostrar el mensaje en español (RNF-8). Los
 * argumentos de línea de comandos pisan cualquier variable de entorno de la
 * PC, así que la prueba no depende de cómo esté configurada.
 */
@ExtendWith(OutputCaptureExtension.class)
class ArranqueSinConexionTest {

	private static Throwable arrancarCon(String... argumentos) {
		return catchThrowable(() -> new SpringApplicationBuilder(SpringfinalApplication.class)
				.web(WebApplicationType.NONE)
				.run(argumentos)
				.close());
	}

	private static boolean fueCortadoPorElChequeo(Throwable error) {
		return Stream.iterate(error, Objects::nonNull, Throwable::getCause)
				.anyMatch(ConfiguracionBaseDeDatosException.class::isInstance);
	}

	@Test
	void sinDbUrlNoArrancaYLoDiceEnEspanol(CapturedOutput salida) {
		Throwable error = arrancarCon(
				"--spring.datasource.url=${AUTODRIVE_VARIABLE_QUE_NO_EXISTE}",
				"--spring.datasource.username=postgres",
				"--spring.datasource.password=clave");

		assertThat(error).isNotNull();
		assertThat(fueCortadoPorElChequeo(error)).isTrue();
		assertThat(salida.getAll()).contains("APPLICATION FAILED TO START").contains("DB_URL no está definida");
	}

	@Test
	void conLaCadenaDeNeonSinJdbcNoMuestraLaContrasena(CapturedOutput salida) {
		Throwable error = arrancarCon(
				"--spring.datasource.url=postgresql://ana:secreta@ep-ejemplo.us-east-2.aws.neon.tech/autodrive",
				"--spring.datasource.username=ana",
				"--spring.datasource.password=otraclave");

		assertThat(fueCortadoPorElChequeo(error)).isTrue();
		assertThat(salida.getAll()).contains("APPLICATION FAILED TO START")
				.contains("jdbc:postgresql://")
				.doesNotContain("secreta")
				.doesNotContain("otraclave");
	}

}
