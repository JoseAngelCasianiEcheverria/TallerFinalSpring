package com.tallerfinal.springfinal.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tallerfinal.springfinal.config.ClockConfig;
import com.tallerfinal.springfinal.config.JacksonConfig;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Formato común de errores (RF-1 a RF-6, RF-59 a RF-62). Un controlador que
 * solo existe en esta prueba provoca cada error; las pruebas parciales no
 * cargan las {@code @Configuration} propias, por eso se importan.
 */
@WebMvcTest(controllers = ManejadorGlobalErroresTest.ControladorDePrueba.class)
@Import({ ManejadorGlobalErroresTest.ControladorDePrueba.class, ClockConfig.class, JacksonConfig.class })
class ManejadorGlobalErroresTest {

	/** 2026-10-08 15:31:02,123456 en Colombia: la respuesta trunca al segundo. */
	private static final String FECHA = "2026-10-08T15:31:02-05:00";

	@TestBean
	private Clock clock;

	static Clock clock() {
		return Clock.fixed(Instant.parse("2026-10-08T20:31:02.123456Z"), ZoneId.of("America/Bogota"));
	}

	@Autowired
	private MockMvc mockMvc;

	record PedidoPrueba(@NotBlank(message = "es obligatorio") String nombre,
			@NotNull(message = "es obligatorio") @Positive(message = "debe ser mayor que cero") Integer cantidad) {
	}

	@RestController
	@RequestMapping("/prueba")
	static class ControladorDePrueba {

		@PostMapping("/pedidos")
		PedidoPrueba crear(@Valid @RequestBody PedidoPrueba pedido) {
			return pedido;
		}

		@GetMapping("/recursos/{id}")
		String recurso(@PathVariable Long id) {
			throw new RecursoNoEncontradoException("el cliente", id);
		}

		@GetMapping("/consulta")
		String consulta(@RequestParam Long numero) {
			return "ok";
		}

		@GetMapping("/conflicto")
		String conflicto() {
			throw new ConflictoException("El vehículo ya está VENDIDO");
		}

		@GetMapping("/integridad/{restriccion}")
		String integridad(@PathVariable String restriccion) {
			throw new DataIntegrityViolationException("could not execute statement", new ConstraintViolationException(
					"duplicate key value violates constraint", new SQLException("detalle SQL interno"), restriccion));
		}

		@GetMapping("/dato-invalido")
		String datoInvalido() {
			throw new DatoInvalidoException("anio", "debe estar entre 1900 y 2027");
		}

		@GetMapping("/no-disponible")
		String noDisponible() {
			throw new ServicioNoDisponibleException("No hay una tasa de cambio disponible");
		}

		@GetMapping("/inesperado")
		String inesperado() {
			throw new IllegalStateException("NullPointer en SELECT * FROM cliente");
		}

	}

	private static ResultMatcher[] formatoComun(int estado, String error, String ruta) {
		return new ResultMatcher[] {
				status().is(estado),
				content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON),
				jsonPath("$.fecha").value(FECHA),
				jsonPath("$.estado").value(estado),
				jsonPath("$.error").value(error),
				jsonPath("$.mensaje").isNotEmpty(),
				jsonPath("$.ruta").value(ruta) };
	}

	private static ResultMatcher sinCampos() {
		return jsonPath("$.campos").doesNotExist();
	}

	// RF-1

	@Test
	void datosInvalidosDevuelven400ConLaListaDeCampos() throws Exception {
		mockMvc.perform(post("/prueba/pedidos").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\": \"   \", \"cantidad\": 0}"))
			.andExpectAll(formatoComun(400, "Solicitud inválida", "/prueba/pedidos"))
			.andExpect(jsonPath("$.mensaje").value("Hay datos inválidos en el pedido"))
			.andExpect(jsonPath("$.campos.length()").value(2))
			.andExpect(jsonPath("$.campos[0].campo").value("cantidad"))
			.andExpect(jsonPath("$.campos[0].mensaje").value("debe ser mayor que cero"))
			.andExpect(jsonPath("$.campos[1].campo").value("nombre"))
			.andExpect(jsonPath("$.campos[1].mensaje").value("es obligatorio"));
	}

	@Test
	void unCampoAusenteApareceEnLaLista() throws Exception {
		mockMvc.perform(post("/prueba/pedidos").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\": \"Ana\"}"))
			.andExpectAll(formatoComun(400, "Solicitud inválida", "/prueba/pedidos"))
			.andExpect(jsonPath("$.campos.length()").value(1))
			.andExpect(jsonPath("$.campos[0].campo").value("cantidad"))
			.andExpect(jsonPath("$.campos[0].mensaje").value("es obligatorio"));
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"{\"nombre\": \"Ana\", \"cantidad\": \"cinco\"} | cantidad | debe ser un número entero",
			"{\"nombre\": \"Ana\", \"cantidad\": 2.5}       | cantidad | debe ser un número entero",
			"{\"nombre\": 123, \"cantidad\": 1}             | nombre   | debe ser un texto" })
	void unTipoDeDatoEquivocadoNombraElCampo(String json, String campo, String mensaje) throws Exception {
		mockMvc.perform(post("/prueba/pedidos").contentType(MediaType.APPLICATION_JSON).content(json))
			.andExpectAll(formatoComun(400, "Solicitud inválida", "/prueba/pedidos"))
			.andExpect(jsonPath("$.mensaje").value("Hay datos inválidos en el pedido"))
			.andExpect(jsonPath("$.campos.length()").value(1))
			.andExpect(jsonPath("$.campos[0].campo").value(campo))
			.andExpect(jsonPath("$.campos[0].mensaje").value(mensaje));
	}

	@Test
	void unParametroDeConsultaConTipoEquivocadoNombraElCampo() throws Exception {
		mockMvc.perform(get("/prueba/consulta").param("numero", "abc"))
			.andExpectAll(formatoComun(400, "Solicitud inválida", "/prueba/consulta"))
			.andExpect(jsonPath("$.campos[0].campo").value("numero"))
			.andExpect(jsonPath("$.campos[0].mensaje").value("debe ser un número entero"));
	}

	@Test
	void unDatoInvalidoDelServicioNombraElCampo() throws Exception {
		mockMvc.perform(get("/prueba/dato-invalido"))
			.andExpectAll(formatoComun(400, "Solicitud inválida", "/prueba/dato-invalido"))
			.andExpect(jsonPath("$.campos[0].campo").value("anio"))
			.andExpect(jsonPath("$.campos[0].mensaje").value("debe estar entre 1900 y 2027"));
	}

	// RF-2

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"{\"nombre\": \"Ana\",",
			"{\"nombre\": \"Ana\", \"cantidad\": 1} texto de sobra",
			"esto no es JSON" })
	void unJsonInvalidoDevuelve400(String cuerpo) throws Exception {
		mockMvc.perform(post("/prueba/pedidos").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
			.andExpectAll(formatoComun(400, "Solicitud inválida", "/prueba/pedidos"))
			.andExpect(jsonPath("$.mensaje").value("El cuerpo del pedido no es un JSON válido"))
			.andExpect(sinCampos());
	}

	@Test
	void unPedidoSinCuerpoDevuelve400() throws Exception {
		mockMvc.perform(post("/prueba/pedidos").contentType(MediaType.APPLICATION_JSON))
			.andExpectAll(formatoComun(400, "Solicitud inválida", "/prueba/pedidos"))
			.andExpect(jsonPath("$.mensaje").value("El pedido no trae cuerpo; se espera un JSON"))
			.andExpect(sinCampos());
	}

	@Test
	void unCuerpoQueNoEsUnObjetoDevuelve400() throws Exception {
		mockMvc.perform(post("/prueba/pedidos").contentType(MediaType.APPLICATION_JSON).content("[1, 2]"))
			.andExpectAll(formatoComun(400, "Solicitud inválida", "/prueba/pedidos"))
			.andExpect(jsonPath("$.mensaje").value("El cuerpo del pedido debe ser un objeto JSON"))
			.andExpect(sinCampos());
	}

	@Test
	void unCuerpoQueNoEsJsonPorSuTipoDevuelve400() throws Exception {
		mockMvc.perform(post("/prueba/pedidos").contentType(MediaType.TEXT_PLAIN).content("nombre=Ana"))
			.andExpectAll(formatoComun(400, "Solicitud inválida", "/prueba/pedidos"))
			.andExpect(jsonPath("$.mensaje").value("El cuerpo del pedido debe ser JSON (Content-Type: application/json)"))
			.andExpect(sinCampos());
	}

	// RF-3 y RF-59

	@Test
	void unRecursoInexistenteDevuelve404NombrandoRecursoEId() throws Exception {
		mockMvc.perform(get("/prueba/recursos/7"))
			.andExpectAll(formatoComun(404, "No encontrado", "/prueba/recursos/7"))
			.andExpect(jsonPath("$.mensaje").value("No existe el cliente con id 7"))
			.andExpect(sinCampos());
	}

	@Test
	void unIdDeRutaQueNoEsEnteroDevuelve400() throws Exception {
		mockMvc.perform(get("/prueba/recursos/abc"))
			.andExpectAll(formatoComun(400, "Solicitud inválida", "/prueba/recursos/abc"))
			.andExpect(jsonPath("$.mensaje").value("El identificador de la ruta debe ser un número entero"))
			.andExpect(sinCampos());
	}

	// RF-4

	@Test
	void unConflictoDevuelve409ConLaRegla() throws Exception {
		mockMvc.perform(get("/prueba/conflicto"))
			.andExpectAll(formatoComun(409, "Conflicto", "/prueba/conflicto"))
			.andExpect(jsonPath("$.mensaje").value("El vehículo ya está VENDIDO"))
			.andExpect(sinCampos());
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"uq_cliente_correo         | Ya existe un cliente con ese correo",
			"uq_cliente_documento      | Ya existe un cliente con ese documento",
			"uq_vehiculo_placa         | Ya existe un vehículo con esa placa",
			"fk_venta_cliente          | El cliente tiene ventas registradas",
			"fk_venta_vehiculo         | El vehículo tiene ventas o mantenimientos registrados",
			"fk_mantenimiento_vehiculo | El vehículo tiene ventas o mantenimientos registrados",
			"UQ_CLIENTE_CORREO         | Ya existe un cliente con ese correo",
			"uq_otra_restriccion       | El pedido choca con datos ya registrados" })
	void unaRestriccionDeLaBaseDevuelve409ConSuMensaje(String restriccion, String mensaje) throws Exception {
		mockMvc.perform(get("/prueba/integridad/" + restriccion))
			.andExpectAll(formatoComun(409, "Conflicto", "/prueba/integridad/" + restriccion))
			.andExpect(jsonPath("$.mensaje").value(mensaje))
			.andExpect(content().string(not(containsString("detalle SQL interno"))))
			.andExpect(sinCampos());
	}

	// RF-5 y servicio no disponible

	@Test
	void unErrorInesperadoDevuelve500SinDetallesInternos() throws Exception {
		mockMvc.perform(get("/prueba/inesperado"))
			.andExpectAll(formatoComun(500, "Error interno", "/prueba/inesperado"))
			.andExpect(jsonPath("$.mensaje").value("Ocurrió un error inesperado en el servidor"))
			.andExpect(content().string(not(containsString("SELECT"))))
			.andExpect(content().string(not(containsString("IllegalStateException"))))
			.andExpect(sinCampos());
	}

	@Test
	void unServicioNoDisponibleDevuelve503() throws Exception {
		mockMvc.perform(get("/prueba/no-disponible"))
			.andExpectAll(formatoComun(503, "Servicio no disponible", "/prueba/no-disponible"))
			.andExpect(jsonPath("$.mensaje").value("No hay una tasa de cambio disponible"))
			.andExpect(sinCampos());
	}

	// RF-60 y RF-61

	@Test
	void unaRutaQueNoExisteDevuelve404() throws Exception {
		mockMvc.perform(get("/no-existe"))
			.andExpectAll(formatoComun(404, "No encontrado", "/no-existe"))
			.andExpect(jsonPath("$.mensaje").value("No existe la ruta /no-existe"))
			.andExpect(sinCampos());
	}

	@Test
	void unMetodoNoAdmitidoDevuelve405ConLosAdmitidos() throws Exception {
		mockMvc.perform(delete("/prueba/conflicto"))
			.andExpectAll(formatoComun(405, "Método no permitido", "/prueba/conflicto"))
			.andExpect(jsonPath("$.mensaje").value(containsString("El método DELETE no está permitido en /prueba/conflicto")))
			.andExpect(header().string("Allow", containsString("GET")))
			.andExpect(sinCampos());
	}

	// RF-62

	@Test
	void losCamposDesconocidosOCalculadosSeIgnoran() throws Exception {
		mockMvc.perform(post("/prueba/pedidos").contentType(MediaType.APPLICATION_JSON)
				.content("{\"nombre\": \"Ana\", \"cantidad\": 2, \"total\": 5, \"estado\": \"VENDIDO\", \"otro\": true}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.nombre").value("Ana"))
			.andExpect(jsonPath("$.cantidad").value(2));
	}

}
