package com.tallerfinal.springfinal.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

/**
 * Las fechas se registran y se muestran en la hora de Colombia, UTC−5 (RNF-6).
 */
class ClockConfigTest {

	@Test
	void elRelojUsaLaHoraDeColombia() {
		Clock clock = new ClockConfig().clock();

		assertThat(clock.getZone()).isEqualTo(ZoneId.of("America/Bogota"));
	}

	@Test
	void laHoraDeColombiaEsUtcMenos5TodoElAnio() {
		ZoneId zona = new ClockConfig().clock().getZone();

		assertThat(zona.getRules().getOffset(Instant.parse("2026-01-15T12:00:00Z"))).isEqualTo(ZoneOffset.ofHours(-5));
		assertThat(zona.getRules().getOffset(Instant.parse("2026-07-15T12:00:00Z"))).isEqualTo(ZoneOffset.ofHours(-5));
	}

}
