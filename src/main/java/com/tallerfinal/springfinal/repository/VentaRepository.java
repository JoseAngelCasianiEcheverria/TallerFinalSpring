package com.tallerfinal.springfinal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tallerfinal.springfinal.entity.Venta;

public interface VentaRepository extends JpaRepository<Venta, Long> {

	/** RF-39: de la más reciente a la más antigua; ante la misma fecha, la de id mayor primero. */
	@EntityGraph(attributePaths = { "cliente", "vehiculo" })
	List<Venta> findAllByOrderByFechaDescIdDesc();

	/** RF-40. */
	@EntityGraph(attributePaths = { "cliente", "vehiculo" })
	Optional<Venta> findConClienteYVehiculoById(Long id);

}
