package com.tallerfinal.springfinal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tallerfinal.springfinal.entity.EstadoVehiculo;
import com.tallerfinal.springfinal.entity.Vehiculo;

import jakarta.persistence.LockModeType;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

	/** RF-22. */
	List<Vehiculo> findAllByOrderByIdAsc();

	/** RF-24, con {@code DISPONIBLE}. */
	List<Vehiculo> findByEstadoOrderByIdAsc(EstadoVehiculo estado);

	/** RF-25: marca exacta sin distinguir mayúsculas; quien llama recorta los espacios. */
	List<Vehiculo> findByMarcaIgnoreCaseOrderByIdAsc(String marca);

	/**
	 * Lee el vehículo bloqueando su fila hasta que termine la transacción, para
	 * que dos operaciones que cambian su estado no se pisen (RF-38, RF-74,
	 * RF-82; Decisión 6). Tiene que ser la primera lectura del vehículo en la
	 * transacción: si ya estuviera cargado, Hibernate devolvería la copia en
	 * memoria.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select v from Vehiculo v where v.id = :id")
	Optional<Vehiculo> buscarConBloqueo(@Param("id") Long id);

}
