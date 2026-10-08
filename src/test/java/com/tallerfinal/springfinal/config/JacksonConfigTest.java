package com.tallerfinal.springfinal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.context.annotation.Import;

import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Prueba el {@link JsonMapper} que arma Spring Boot con {@link JacksonConfig}:
 * es el mismo que lee el cuerpo de los pedidos (RF-1, RF-62).
 */
@JsonTest
@Import(JacksonConfig.class)
class JacksonConfigTest {

	record Pedido(String nombre, Integer anio, BigDecimal precio, Long clienteId) {
	}

	@Autowired
	private JsonMapper jsonMapper;

	@ParameterizedTest
	@ValueSource(strings = {
			"{\"anio\": 2024.5}",
			"{\"anio\": \"2024\"}",
			"{\"precio\": \"1000\"}",
			"{\"nombre\": 123}",
			"{\"nombre\": true}",
			"{\"clienteId\": 1.0}" })
	void rechazaTiposDeDatoEquivocados(String json) {
		assertThatThrownBy(() -> jsonMapper.readValue(json, Pedido.class))
				.isInstanceOf(MismatchedInputException.class);
	}

	@Test
	void aceptaLosTiposCorrectos() {
		Pedido pedido = jsonMapper.readValue(
				"{\"nombre\": \"Ana\", \"anio\": 2024, \"precio\": 120000000.50, \"clienteId\": 1}", Pedido.class);

		assertThat(pedido).isEqualTo(new Pedido("Ana", 2024, new BigDecimal("120000000.50"), 1L));
	}

	@Test
	void aceptaUnPrecioSinDecimales() {
		Pedido pedido = jsonMapper.readValue("{\"precio\": 120000000}", Pedido.class);

		assertThat(pedido.precio()).isEqualByComparingTo("120000000");
	}

	@Test
	void ignoraLosCamposDesconocidos() {
		Pedido pedido = jsonMapper.readValue("{\"nombre\": \"Ana\", \"estado\": \"VENDIDO\", \"otro\": 1}", Pedido.class);

		assertThat(pedido.nombre()).isEqualTo("Ana");
	}

}
