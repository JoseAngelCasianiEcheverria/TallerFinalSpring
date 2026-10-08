package com.tallerfinal.springfinal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.tallerfinal.springfinal.PruebaIntegracion;

import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.json.JsonMapper;

/**
 * La aplicación completa (no solo las pruebas que importan la configuración a
 * mano) usa el reloj de Colombia y el JSON con tipos estrictos.
 */
class ConfiguracionDeLaAplicacionTest extends PruebaIntegracion {

	record Pedido(Integer anio) {
	}

	@Autowired
	private Clock clock;

	@Autowired
	private JsonMapper jsonMapper;

	@Test
	void usaElRelojDeColombia() {
		assertThat(clock.getZone()).isEqualTo(ZoneId.of("America/Bogota"));
	}

	@Test
	void usaElJsonConTiposEstrictos() {
		assertThatThrownBy(() -> jsonMapper.readValue("{\"anio\": 2024.5}", Pedido.class))
				.isInstanceOf(MismatchedInputException.class);
	}

}
