package com.tallerfinal.springfinal;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;

/**
 * Base de las pruebas de integración: levantan la aplicación completa contra un
 * PostgreSQL embebido, nunca contra Neon ni contra un PostgreSQL instalado
 * (aunque las variables de entorno de la base estén puestas). La instancia se
 * crea una sola vez por corrida y las tablas se vacían antes de cada prueba.
 */
@SpringBootTest
public abstract class PruebaIntegracion {

	private static final EmbeddedPostgres POSTGRES = iniciarPostgres();

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@DynamicPropertySource
	static void configurarBase(DynamicPropertyRegistry registro) {
		registro.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
		registro.add("spring.datasource.username", () -> "postgres");
		// El PostgreSQL embebido no pide contraseña, pero el chequeo de arranque (RNF-8) exige una.
		registro.add("spring.datasource.password", () -> "postgres");
	}

	@BeforeEach
	void vaciarTablasAntesDeCadaPrueba() {
		vaciarTablas();
	}

	protected void vaciarTablas() {
		List<String> tablas = jdbcTemplate.queryForList(
				"select quote_ident(tablename) from pg_tables where schemaname = 'public'", String.class);
		if (!tablas.isEmpty()) {
			jdbcTemplate.execute("truncate table " + String.join(", ", tablas) + " restart identity cascade");
		}
	}

	private static EmbeddedPostgres iniciarPostgres() {
		try {
			EmbeddedPostgres postgres = EmbeddedPostgres.start();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					postgres.close();
				}
				catch (IOException ex) {
					// La JVM ya se está cerrando: no hay nada más que hacer.
				}
			}));
			return postgres;
		}
		catch (IOException ex) {
			throw new UncheckedIOException("No se pudo iniciar el PostgreSQL embebido", ex);
		}
	}

}
