package com.tallerfinal.springfinal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

/**
 * RNF-8. El {@link MockEnvironment} no ve las variables de entorno de la PC:
 * {@code ${DB_URL}} sin resolver simula que la variable no está definida.
 */
class VerificadorConexionBaseDeDatosTest {

	private static final String URL_VALIDA = "jdbc:postgresql://localhost:5432/autodrive";

	private static MockEnvironment entorno(String url, String usuario, String contrasena) {
		return new MockEnvironment()
				.withProperty("spring.datasource.url", url)
				.withProperty("spring.datasource.username", usuario)
				.withProperty("spring.datasource.password", contrasena);
	}

	private static ConfiguracionBaseDeDatosException fallaCon(MockEnvironment entorno) {
		return catchThrowableOfType(ConfiguracionBaseDeDatosException.class,
				() -> VerificadorConexionBaseDeDatos.verificar(entorno));
	}

	@Test
	void sinDbUrlNombraLaVariable() {
		ConfiguracionBaseDeDatosException error = fallaCon(entorno("${DB_URL}", "postgres", "clave"));

		assertThat(error).isNotNull();
		assertThat(error.getProblemas()).singleElement().asString().contains("DB_URL").contains("no está definida");
	}

	@Test
	void sinUsuarioNiContrasenaNombraLasDos() {
		ConfiguracionBaseDeDatosException error = fallaCon(entorno(URL_VALIDA, "${DB_USUARIO}", "${DB_CONTRASENA}"));

		assertThat(error).isNotNull();
		assertThat(error.getProblemas()).hasSize(2);
		assertThat(error.getMessage()).contains("DB_USUARIO").contains("DB_CONTRASENA").doesNotContain("DB_URL");
	}

	@Test
	void unaPropiedadAusenteOVaciaCuentaComoFaltante() {
		MockEnvironment sinUrl = new MockEnvironment()
				.withProperty("spring.datasource.username", "postgres")
				.withProperty("spring.datasource.password", "clave");

		assertThat(fallaCon(sinUrl).getMessage()).contains("DB_URL");
		assertThat(fallaCon(entorno(URL_VALIDA, "postgres", "   ")).getMessage()).contains("DB_CONTRASENA");
	}

	@Test
	void unaUrlQueNoEsJdbcDiceComoDebeSerSinMostrarElValor() {
		ConfiguracionBaseDeDatosException error = fallaCon(
				entorno("postgresql://ana:secreta@ep-ejemplo.us-east-2.aws.neon.tech/autodrive?sslmode=require",
						"ana", "secreta"));

		assertThat(error).isNotNull();
		assertThat(error.getProblemas()).singleElement().asString().contains("DB_URL").contains("jdbc:postgresql://");
		assertThat(error.getMessage()).doesNotContain("secreta").doesNotContain("ana:").doesNotContain("neon.tech");
	}

	@Test
	void conLasTresBienNoLanzaNada() {
		assertThatNoException().isThrownBy(() -> VerificadorConexionBaseDeDatos.verificar(
				entorno(URL_VALIDA, "postgres", "clave")));
	}

}
