package com.tallerfinal.springfinal.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ClockConfig {

	/**
	 * Reloj de la aplicación en la hora de Colombia (RNF-6). Los servicios toman
	 * el "ahora" de aquí y no de {@code Instant.now()}, para que las pruebas
	 * puedan fijarlo.
	 */
	@Bean
	Clock clock() {
		return Clock.system(ZoneId.of("America/Bogota"));
	}

}
