package com.tallerfinal.springfinal.exception;

import java.math.BigInteger;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.ClassUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.tallerfinal.springfinal.dto.CampoInvalidoDto;
import com.tallerfinal.springfinal.dto.RespuestaErrorDto;

import jakarta.servlet.http.HttpServletRequest;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.exc.MismatchedInputException;

/**
 * Único punto de salida de los errores de la API: todos responden con
 * {@link RespuestaErrorDto} y el código HTTP de la spec (RF-1 a RF-6, RF-59 a
 * RF-61, RF-86). Ningún controlador arma una respuesta de error a mano.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores {

	private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

	private static final String DATOS_INVALIDOS = "Hay datos inválidos en el pedido";

	/** Mensaje de cada restricción de la base; los nombres se fijan en las entidades. */
	private static final Map<String, String> MENSAJES_POR_RESTRICCION = Map.of(
			"uq_cliente_correo", "Ya existe un cliente con ese correo",
			"uq_cliente_documento", "Ya existe un cliente con ese documento",
			"uq_vehiculo_placa", "Ya existe un vehículo con esa placa",
			"fk_venta_cliente", "El cliente tiene ventas registradas",
			"fk_venta_vehiculo", "El vehículo tiene ventas o mantenimientos registrados",
			"fk_mantenimiento_vehiculo", "El vehículo tiene ventas o mantenimientos registrados");

	private static final String CONFLICTO_GENERICO = "El pedido choca con datos ya registrados";

	private final Clock clock;

	public ManejadorGlobalErrores(Clock clock) {
		this.clock = clock;
	}

	// 400 — RF-1, RF-2, RF-59

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<RespuestaErrorDto> datosInvalidos(MethodArgumentNotValidException ex, HttpServletRequest pedido) {
		List<CampoInvalidoDto> campos = ex.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> new CampoInvalidoDto(error.getField(), error.getDefaultMessage()))
				.sorted(Comparator.comparing(CampoInvalidoDto::campo).thenComparing(CampoInvalidoDto::mensaje))
				.toList();
		return responder(HttpStatus.BAD_REQUEST, DATOS_INVALIDOS, pedido, campos);
	}

	@ExceptionHandler(DatoInvalidoException.class)
	ResponseEntity<RespuestaErrorDto> datoInvalido(DatoInvalidoException ex, HttpServletRequest pedido) {
		return responder(HttpStatus.BAD_REQUEST, DATOS_INVALIDOS, pedido,
				List.of(new CampoInvalidoDto(ex.getCampo(), ex.getMessage())));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<RespuestaErrorDto> cuerpoIlegible(HttpMessageNotReadableException ex, HttpServletRequest pedido) {
		MismatchedInputException tipoEquivocado = buscarCausa(ex, MismatchedInputException.class);
		if (tipoEquivocado != null) {
			String campo = rutaDelCampo(tipoEquivocado);
			if (campo.isEmpty()) {
				return responder(HttpStatus.BAD_REQUEST, "El cuerpo del pedido debe ser un objeto JSON", pedido, null);
			}
			return responder(HttpStatus.BAD_REQUEST, DATOS_INVALIDOS, pedido,
					List.of(new CampoInvalidoDto(campo, tipoEsperado(tipoEquivocado.getTargetType()))));
		}
		if (buscarCausa(ex, JacksonException.class) != null) {
			return responder(HttpStatus.BAD_REQUEST, "El cuerpo del pedido no es un JSON válido", pedido, null);
		}
		String mensaje = (ex.getMessage() != null && ex.getMessage().startsWith("Required request body is missing"))
				? "El pedido no trae cuerpo; se espera un JSON"
				: "No se pudo leer el cuerpo del pedido; se espera un JSON";
		return responder(HttpStatus.BAD_REQUEST, mensaje, pedido, null);
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	ResponseEntity<RespuestaErrorDto> tipoDeContenido(HttpMediaTypeNotSupportedException ex, HttpServletRequest pedido) {
		return responder(HttpStatus.BAD_REQUEST, "El cuerpo del pedido debe ser JSON (Content-Type: application/json)",
				pedido, null);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	ResponseEntity<RespuestaErrorDto> parametroConTipoEquivocado(MethodArgumentTypeMismatchException ex,
			HttpServletRequest pedido) {
		if (ex.getParameter().hasParameterAnnotation(PathVariable.class)) {
			String mensaje = esEntero(ex.getRequiredType())
					? "El identificador de la ruta debe ser un número entero"
					: "Un valor de la ruta tiene un tipo de dato inválido";
			return responder(HttpStatus.BAD_REQUEST, mensaje, pedido, null);
		}
		return responder(HttpStatus.BAD_REQUEST, DATOS_INVALIDOS, pedido,
				List.of(new CampoInvalidoDto(ex.getName(), tipoEsperado(ex.getRequiredType()))));
	}

	// 404 — RF-3, RF-60

	@ExceptionHandler(RecursoNoEncontradoException.class)
	ResponseEntity<RespuestaErrorDto> recursoNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest pedido) {
		return responder(HttpStatus.NOT_FOUND, ex.getMessage(), pedido, null);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	ResponseEntity<RespuestaErrorDto> rutaInexistente(NoResourceFoundException ex, HttpServletRequest pedido) {
		return responder(HttpStatus.NOT_FOUND, "No existe la ruta " + pedido.getRequestURI(), pedido, null);
	}

	// 405 — RF-61

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	ResponseEntity<RespuestaErrorDto> metodoNoAdmitido(HttpRequestMethodNotSupportedException ex,
			HttpServletRequest pedido) {
		Set<HttpMethod> admitidos = ex.getSupportedHttpMethods();
		String mensaje = "El método " + ex.getMethod() + " no está permitido en " + pedido.getRequestURI();
		HttpHeaders cabeceras = new HttpHeaders();
		if (admitidos != null && !admitidos.isEmpty()) {
			mensaje += "; métodos admitidos: "
					+ admitidos.stream().map(HttpMethod::name).sorted().collect(Collectors.joining(", "));
			cabeceras.setAllow(admitidos);
		}
		return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
				.headers(cabeceras)
				.body(cuerpo(HttpStatus.METHOD_NOT_ALLOWED, mensaje, pedido, null));
	}

	// 409 — RF-4

	@ExceptionHandler(ConflictoException.class)
	ResponseEntity<RespuestaErrorDto> conflicto(ConflictoException ex, HttpServletRequest pedido) {
		return responder(HttpStatus.CONFLICT, ex.getMessage(), pedido, null);
	}

	/**
	 * Una restricción de la base que la comprobación previa del servicio no
	 * alcanzó a ver (pedidos simultáneos): el mensaje sale del nombre de la
	 * restricción, nunca del detalle SQL.
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	ResponseEntity<RespuestaErrorDto> restriccionDeLaBase(DataIntegrityViolationException ex,
			HttpServletRequest pedido) {
		ConstraintViolationException violacion = buscarCausa(ex, ConstraintViolationException.class);
		String restriccion = violacion == null ? null : normalizarRestriccion(violacion.getConstraintName());
		String mensaje = restriccion == null ? CONFLICTO_GENERICO
				: MENSAJES_POR_RESTRICCION.getOrDefault(restriccion, CONFLICTO_GENERICO);
		return responder(HttpStatus.CONFLICT, mensaje, pedido, null);
	}

	// 503 — RF-86

	@ExceptionHandler(ServicioNoDisponibleException.class)
	ResponseEntity<RespuestaErrorDto> servicioNoDisponible(ServicioNoDisponibleException ex,
			HttpServletRequest pedido) {
		return responder(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), pedido, null);
	}

	// 500 — RF-5

	@ExceptionHandler(Exception.class)
	ResponseEntity<RespuestaErrorDto> errorInesperado(Exception ex, HttpServletRequest pedido) {
		log.error("Error inesperado en {} {}", pedido.getMethod(), pedido.getRequestURI(), ex);
		return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor", pedido, null);
	}

	// Apoyo

	private ResponseEntity<RespuestaErrorDto> responder(HttpStatus estado, String mensaje, HttpServletRequest pedido,
			List<CampoInvalidoDto> campos) {
		return ResponseEntity.status(estado).body(cuerpo(estado, mensaje, pedido, campos));
	}

	private RespuestaErrorDto cuerpo(HttpStatus estado, String mensaje, HttpServletRequest pedido,
			List<CampoInvalidoDto> campos) {
		return new RespuestaErrorDto(OffsetDateTime.now(clock).truncatedTo(ChronoUnit.SECONDS), estado.value(),
				nombreDelError(estado), mensaje, pedido.getRequestURI(), campos);
	}

	private static String nombreDelError(HttpStatus estado) {
		return switch (estado) {
			case BAD_REQUEST -> "Solicitud inválida";
			case NOT_FOUND -> "No encontrado";
			case METHOD_NOT_ALLOWED -> "Método no permitido";
			case CONFLICT -> "Conflicto";
			case SERVICE_UNAVAILABLE -> "Servicio no disponible";
			default -> "Error interno";
		};
	}

	/** "precio", "vehiculo.placa" o "items[0].nombre", según la ruta que reporta Jackson. */
	private static String rutaDelCampo(MismatchedInputException ex) {
		StringBuilder ruta = new StringBuilder();
		for (JacksonException.Reference referencia : ex.getPath()) {
			if (referencia.getPropertyName() != null) {
				if (!ruta.isEmpty()) {
					ruta.append('.');
				}
				ruta.append(referencia.getPropertyName());
			}
			else if (referencia.getIndex() >= 0) {
				ruta.append('[').append(referencia.getIndex()).append(']');
			}
		}
		return ruta.toString();
	}

	private static String tipoEsperado(Class<?> tipo) {
		if (tipo == null) {
			return "tiene un tipo de dato inválido";
		}
		Class<?> clase = ClassUtils.resolvePrimitiveIfNecessary(tipo);
		if (esEntero(clase)) {
			return "debe ser un número entero";
		}
		if (Number.class.isAssignableFrom(clase)) {
			return "debe ser un número";
		}
		if (clase == String.class) {
			return "debe ser un texto";
		}
		if (clase == Boolean.class) {
			return "debe ser verdadero o falso";
		}
		return "tiene un tipo de dato inválido";
	}

	private static boolean esEntero(Class<?> tipo) {
		if (tipo == null) {
			return false;
		}
		Class<?> clase = ClassUtils.resolvePrimitiveIfNecessary(tipo);
		return clase == Integer.class || clase == Long.class || clase == Short.class || clase == Byte.class
				|| clase == BigInteger.class;
	}

	/** Hibernate puede reportar el nombre en mayúsculas o con el esquema delante ("public.uq_…"). */
	private static String normalizarRestriccion(String nombre) {
		if (nombre == null) {
			return null;
		}
		String sinEsquema = nombre.substring(nombre.lastIndexOf('.') + 1);
		return sinEsquema.toLowerCase(Locale.ROOT);
	}

	private static <T extends Throwable> T buscarCausa(Throwable error, Class<T> tipo) {
		for (Throwable actual = error; actual != null; actual = actual.getCause()) {
			if (tipo.isInstance(actual)) {
				return tipo.cast(actual);
			}
			if (actual.getCause() == actual) {
				break;
			}
		}
		return null;
	}

}
