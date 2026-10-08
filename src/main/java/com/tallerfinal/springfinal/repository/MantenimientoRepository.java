package com.tallerfinal.springfinal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tallerfinal.springfinal.entity.Mantenimiento;

public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {

	/** RF-50: del más reciente al más antiguo; ante la misma fecha, el de id mayor primero. */
	@EntityGraph(attributePaths = "vehiculo")
	List<Mantenimiento> findAllByOrderByFechaIngresoDescIdDesc();

	/** RF-51. */
	@EntityGraph(attributePaths = "vehiculo")
	List<Mantenimiento> findByVehiculoIdOrderByFechaIngresoDescIdDesc(Long vehiculoId);

}
