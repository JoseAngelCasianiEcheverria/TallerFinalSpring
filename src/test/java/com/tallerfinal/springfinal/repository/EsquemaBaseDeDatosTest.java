package com.tallerfinal.springfinal.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.tallerfinal.springfinal.PruebaIntegracion;

/**
 * El PostgreSQL embebido arranca vacío en cada corrida: las tablas que hay
 * aquí las creó la aplicación al iniciar (RNF-7), con los tipos, longitudes y
 * restricciones del plan (RNF-2).
 */
class EsquemaBaseDeDatosTest extends PruebaIntegracion {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void laAplicacionCreaLasCuatroTablas() {
		List<String> tablas = jdbcTemplate.queryForList(
				"select table_name from information_schema.tables where table_schema = 'public'", String.class);

		assertThat(tablas).contains("cliente", "vehiculo", "venta", "mantenimiento");
	}

	@ParameterizedTest(name = "{0}.{1} es {2}")
	@CsvSource(delimiter = '|', value = {
			"cliente       | nombre             | character varying(100)   | NO",
			"cliente       | documento          | character varying(15)    | NO",
			"cliente       | correo             | character varying(254)   | NO",
			"cliente       | telefono           | character varying(16)    | NO",
			"vehiculo      | placa              | character varying(7)     | NO",
			"vehiculo      | marca              | character varying(50)    | NO",
			"vehiculo      | linea              | character varying(50)    | NO",
			"vehiculo      | anio               | integer                  | NO",
			"vehiculo      | precio             | numeric(13,2)            | NO",
			"vehiculo      | estado             | character varying(20)    | NO",
			"venta         | cliente_id         | bigint                   | NO",
			"venta         | vehiculo_id        | bigint                   | NO",
			"venta         | fecha              | timestamp(6) with time zone | NO",
			"venta         | precio             | numeric(13,2)            | NO",
			"venta         | descuento          | numeric(13,2)            | NO",
			"venta         | total              | numeric(13,2)            | NO",
			"venta         | tasa_cambio        | numeric(20,12)           | YES",
			"venta         | total_usd          | numeric(13,2)            | YES",
			"venta         | motivo_usd         | character varying(255)   | YES",
			"mantenimiento | vehiculo_id        | bigint                   | NO",
			"mantenimiento | descripcion        | character varying(255)   | NO",
			"mantenimiento | costo              | numeric(13,2)            | NO",
			"mantenimiento | fecha_ingreso      | timestamp(6) with time zone | NO",
			"mantenimiento | fecha_finalizacion | timestamp(6) with time zone | YES" })
	void cadaColumnaTieneElTipoYLaNulidadDelPlan(String tabla, String columna, String tipo, String admiteNulo) {
		Map<String, Object> fila = jdbcTemplate.queryForMap("""
				select format_type(a.atttypid, a.atttypmod) as tipo,
				       case when a.attnotnull then 'NO' else 'YES' end as admite_nulo
				from pg_attribute a
				where a.attrelid = ?::regclass and a.attname = ? and not a.attisdropped
				""", tabla, columna);

		assertThat(fila.get("tipo")).isEqualTo(tipo);
		assertThat(fila.get("admite_nulo")).isEqualTo(admiteNulo);
	}

	@Test
	void losIdentificadoresSonBigintGeneradosPorLaBase() {
		List<String> sinIdentidad = jdbcTemplate.queryForList("""
				select table_name from information_schema.columns
				where table_schema = 'public' and column_name = 'id'
				  and table_name in ('cliente', 'vehiculo', 'venta', 'mantenimiento')
				  and not (data_type = 'bigint' and (is_identity = 'YES' or column_default like 'nextval%'))
				""", String.class);
		Integer tablasConId = jdbcTemplate.queryForObject("""
				select count(*) from information_schema.columns
				where table_schema = 'public' and column_name = 'id'
				  and table_name in ('cliente', 'vehiculo', 'venta', 'mantenimiento')
				""", Integer.class);

		assertThat(tablasConId).isEqualTo(4);
		assertThat(sinIdentidad).isEmpty();
	}

	@ParameterizedTest(name = "{0} sobre {1}({2})")
	@CsvSource(delimiter = '|', value = {
			"uq_cliente_correo    | cliente  | correo",
			"uq_cliente_documento | cliente  | documento",
			"uq_vehiculo_placa    | vehiculo | placa" })
	void lasRestriccionesUnicasTienenElNombreDelPlan(String restriccion, String tabla, String columna) {
		List<String> columnas = jdbcTemplate.queryForList("""
				select kcu.column_name
				from information_schema.table_constraints tc
				join information_schema.key_column_usage kcu
				  on kcu.constraint_name = tc.constraint_name and kcu.table_schema = tc.table_schema
				where tc.table_schema = 'public' and tc.constraint_type = 'UNIQUE'
				  and tc.constraint_name = ? and tc.table_name = ?
				""", String.class, restriccion, tabla);

		assertThat(columnas).containsExactly(columna);
	}

	@ParameterizedTest(name = "{0}: {1}.{2} → {3}, sin cascada")
	@CsvSource(delimiter = '|', value = {
			"fk_venta_cliente          | venta         | cliente_id  | cliente",
			"fk_venta_vehiculo         | venta         | vehiculo_id | vehiculo",
			"fk_mantenimiento_vehiculo | mantenimiento | vehiculo_id | vehiculo" })
	void lasLlavesForaneasTienenElNombreDelPlanYNoBorranEnCascada(String restriccion, String tabla, String columna,
			String tablaReferida) {
		Map<String, Object> fk = jdbcTemplate.queryForMap("""
				select kcu.table_name, kcu.column_name, ccu.table_name as tabla_referida, rc.delete_rule
				from information_schema.referential_constraints rc
				join information_schema.key_column_usage kcu
				  on kcu.constraint_name = rc.constraint_name and kcu.constraint_schema = rc.constraint_schema
				join information_schema.constraint_column_usage ccu
				  on ccu.constraint_name = rc.constraint_name and ccu.constraint_schema = rc.constraint_schema
				where rc.constraint_schema = 'public' and rc.constraint_name = ?
				""", restriccion);

		assertThat(fk.get("table_name")).isEqualTo(tabla);
		assertThat(fk.get("column_name")).isEqualTo(columna);
		assertThat(fk.get("tabla_referida")).isEqualTo(tablaReferida);
		assertThat(fk.get("delete_rule")).isIn("NO ACTION", "RESTRICT");
	}

	@Test
	void elEstadoDelVehiculoSoloAdmiteLosTresValores() {
		String definicion = jdbcTemplate.queryForObject("""
				select string_agg(pg_get_constraintdef(c.oid), ' ')
				from pg_constraint c
				where c.conrelid = 'vehiculo'::regclass and c.contype = 'c'
				""", String.class);

		assertThat(definicion).contains("DISPONIBLE", "VENDIDO", "EN_MANTENIMIENTO");
	}

}
