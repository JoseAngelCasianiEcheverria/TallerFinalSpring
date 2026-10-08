package com.tallerfinal.springfinal.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import com.tallerfinal.springfinal.PruebaIntegracion;
import com.tallerfinal.springfinal.entity.Cliente;
import com.tallerfinal.springfinal.entity.EstadoVehiculo;
import com.tallerfinal.springfinal.entity.Mantenimiento;
import com.tallerfinal.springfinal.entity.Vehiculo;
import com.tallerfinal.springfinal.entity.Venta;

/**
 * Las consultas de los repositorios contra PostgreSQL: orden de las listas
 * (incluido el empate de fechas), búsqueda por marca, carga del cliente y el
 * vehículo en la misma consulta y bloqueo de la fila del vehículo.
 */
class RepositoriosTest extends PruebaIntegracion {

	private static final Instant LUNES = Instant.parse("2026-10-05T14:00:00Z");

	private static final Instant MARTES = Instant.parse("2026-10-06T14:00:00Z");

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private VehiculoRepository vehiculoRepository;

	@Autowired
	private VentaRepository ventaRepository;

	@Autowired
	private MantenimientoRepository mantenimientoRepository;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private DataSource dataSource;

	private Cliente cliente(String documento) {
		return clienteRepository.save(new Cliente("Cliente " + documento, documento, documento + "@correo.com", "3001234567"));
	}

	private Vehiculo vehiculo(String placa, String marca, EstadoVehiculo estado) {
		return vehiculoRepository.save(
				new Vehiculo(placa, marca, "Linea", 2024, new BigDecimal("50000000.00"), estado));
	}

	private Venta venta(Cliente cliente, Vehiculo vehiculo, Instant fecha) {
		BigDecimal precio = new BigDecimal("50000000.00");
		return ventaRepository.save(new Venta(cliente, vehiculo, fecha, precio, BigDecimal.ZERO.setScale(2), precio,
				null, null, "Sin tasa en la prueba"));
	}

	private Mantenimiento mantenimiento(Vehiculo vehiculo, Instant fechaIngreso) {
		return mantenimientoRepository.save(
				new Mantenimiento(vehiculo, "Revisión", new BigDecimal("100000.00"), fechaIngreso));
	}

	private static <T> List<Long> ids(List<T> entidades, Function<T, Long> id) {
		return entidades.stream().map(id).toList();
	}

	// Clientes y vehículos: por identificador (RF-12, RF-22, RF-24, RF-25)

	@Test
	void losClientesSeListanPorIdentificador() {
		Cliente primero = cliente("10001");
		Cliente segundo = cliente("10002");
		Cliente tercero = cliente("10003");

		assertThat(ids(clienteRepository.findAllByOrderByIdAsc(), Cliente::getId))
			.containsExactly(primero.getId(), segundo.getId(), tercero.getId());
	}

	@Test
	void losVehiculosSeListanPorIdentificador() {
		Vehiculo primero = vehiculo("AAA111", "Toyota", EstadoVehiculo.VENDIDO);
		Vehiculo segundo = vehiculo("BBB222", "Mazda", EstadoVehiculo.DISPONIBLE);

		assertThat(ids(vehiculoRepository.findAllByOrderByIdAsc(), Vehiculo::getId)).containsExactly(primero.getId(), segundo.getId());
	}

	@Test
	void losDisponiblesExcluyenVendidosYEnMantenimiento() {
		Vehiculo disponible1 = vehiculo("AAA111", "Toyota", EstadoVehiculo.DISPONIBLE);
		vehiculo("BBB222", "Toyota", EstadoVehiculo.VENDIDO);
		vehiculo("CCC333", "Toyota", EstadoVehiculo.EN_MANTENIMIENTO);
		Vehiculo disponible2 = vehiculo("DDD444", "Mazda", EstadoVehiculo.DISPONIBLE);

		assertThat(ids(vehiculoRepository.findByEstadoOrderByIdAsc(EstadoVehiculo.DISPONIBLE), Vehiculo::getId))
			.containsExactly(disponible1.getId(), disponible2.getId());
	}

	@Test
	void laMarcaSeBuscaExactaSinDistinguirMayusculas() {
		Vehiculo corolla = vehiculo("AAA111", "Toyota", EstadoVehiculo.DISPONIBLE);
		vehiculo("BBB222", "Toyota Motor", EstadoVehiculo.DISPONIBLE);
		Vehiculo hilux = vehiculo("CCC333", "TOYOTA", EstadoVehiculo.VENDIDO);
		vehiculo("DDD444", "Mazda", EstadoVehiculo.DISPONIBLE);

		assertThat(ids(vehiculoRepository.findByMarcaIgnoreCaseOrderByIdAsc("toyota"), Vehiculo::getId))
			.containsExactly(corolla.getId(), hilux.getId());
		assertThat(vehiculoRepository.findByMarcaIgnoreCaseOrderByIdAsc("Kia")).isEmpty();
	}

	// Ventas y mantenimientos: del más reciente al más antiguo, empate por id (RF-39, RF-50, RF-51)

	@Test
	void lasVentasVanDeLaMasRecienteALaMasAntiguaYElEmpateSeDecidePorId() {
		Cliente ana = cliente("10001");
		Venta delLunes = venta(ana, vehiculo("AAA111", "Toyota", EstadoVehiculo.VENDIDO), LUNES);
		Venta martesA = venta(ana, vehiculo("BBB222", "Toyota", EstadoVehiculo.VENDIDO), MARTES);
		Venta martesB = venta(ana, vehiculo("CCC333", "Mazda", EstadoVehiculo.VENDIDO), MARTES);

		assertThat(ids(ventaRepository.findAllByOrderByFechaDescIdDesc(), Venta::getId))
			.containsExactly(martesB.getId(), martesA.getId(), delLunes.getId());
	}

	@Test
	void laListaDeVentasTraeClienteYVehiculoSinOtraConsulta() {
		venta(cliente("10001"), vehiculo("AAA111", "Toyota", EstadoVehiculo.VENDIDO), LUNES);

		// Fuera de una transacción (open-in-view está apagado): sin @EntityGraph esto fallaría por carga perezosa.
		Venta venta = ventaRepository.findAllByOrderByFechaDescIdDesc().get(0);

		assertThat(venta.getCliente().getNombre()).isEqualTo("Cliente 10001");
		assertThat(venta.getVehiculo().getPlaca()).isEqualTo("AAA111");
	}

	@Test
	void unaVentaPorIdTraeClienteYVehiculo() {
		Venta guardada = venta(cliente("10001"), vehiculo("AAA111", "Toyota", EstadoVehiculo.VENDIDO), LUNES);

		Venta venta = ventaRepository.findConClienteYVehiculoById(guardada.getId()).orElseThrow();

		assertThat(venta.getCliente().getNombre()).isEqualTo("Cliente 10001");
		assertThat(venta.getVehiculo().getMarca()).isEqualTo("Toyota");
	}

	@Test
	void losMantenimientosVanDelMasRecienteAlMasAntiguoYTraenSuVehiculo() {
		Vehiculo corolla = vehiculo("AAA111", "Toyota", EstadoVehiculo.DISPONIBLE);
		Mantenimiento delLunes = mantenimiento(corolla, LUNES);
		Mantenimiento martesA = mantenimiento(corolla, MARTES);
		Mantenimiento martesB = mantenimiento(vehiculo("BBB222", "Mazda", EstadoVehiculo.DISPONIBLE), MARTES);

		List<Mantenimiento> todos = mantenimientoRepository.findAllByOrderByFechaIngresoDescIdDesc();

		assertThat(ids(todos, Mantenimiento::getId)).containsExactly(martesB.getId(), martesA.getId(), delLunes.getId());
		assertThat(todos.get(0).getVehiculo().getPlaca()).isEqualTo("BBB222");
	}

	@Test
	void elHistorialDeUnVehiculoSoloTraeLosSuyos() {
		Vehiculo corolla = vehiculo("AAA111", "Toyota", EstadoVehiculo.DISPONIBLE);
		Mantenimiento delLunes = mantenimiento(corolla, LUNES);
		Mantenimiento delMartes = mantenimiento(corolla, MARTES);
		mantenimiento(vehiculo("BBB222", "Mazda", EstadoVehiculo.DISPONIBLE), MARTES);

		List<Mantenimiento> historial = mantenimientoRepository.findByVehiculoIdOrderByFechaIngresoDescIdDesc(corolla.getId());

		assertThat(ids(historial, Mantenimiento::getId)).containsExactly(delMartes.getId(), delLunes.getId());
		assertThat(historial.get(0).getVehiculo().getPlaca()).isEqualTo("AAA111");
	}

	// Bloqueo de la fila del vehículo (Decisión 6; la concurrencia completa va en T15)

	@Test
	void buscarConBloqueoBloqueaLaFilaHastaTerminarLaTransaccion() {
		Vehiculo corolla = vehiculo("AAA111", "Toyota", EstadoVehiculo.DISPONIBLE);

		transactionTemplate.executeWithoutResult(estado -> {
			assertThat(vehiculoRepository.buscarConBloqueo(corolla.getId())).isPresent();

			// Otra conexión, fuera de esta transacción, no puede tomar la misma fila.
			assertThatThrownBy(() -> intentarBloquearDesdeOtraConexion(corolla.getId()))
				.isInstanceOf(SQLException.class)
				.extracting(error -> ((SQLException) error).getSQLState())
				.isEqualTo("55P03");
		});

		// Terminada la transacción, la fila queda libre.
		assertThat(intentarBloquearDesdeOtraConexionSinError(corolla.getId())).isTrue();
	}

	@Test
	void buscarConBloqueoDeUnIdInexistenteVieneVacio() {
		transactionTemplate.executeWithoutResult(
				estado -> assertThat(vehiculoRepository.buscarConBloqueo(999_999L)).isEmpty());
	}

	private void intentarBloquearDesdeOtraConexion(Long id) throws SQLException {
		try (Connection conexion = dataSource.getConnection();
				PreparedStatement consulta = conexion.prepareStatement("select id from vehiculo where id = ? for update nowait")) {
			conexion.setAutoCommit(false);
			consulta.setLong(1, id);
			try {
				consulta.executeQuery().close();
			}
			finally {
				conexion.rollback();
			}
		}
	}

	private boolean intentarBloquearDesdeOtraConexionSinError(Long id) {
		try {
			intentarBloquearDesdeOtraConexion(id);
			return true;
		}
		catch (SQLException ex) {
			return false;
		}
	}

}
