package com.tallerautos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerautos.entity.EstadoVehiculo;
import com.tallerautos.entity.Vehiculo;

import jakarta.persistence.LockModeType;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {

    
    boolean existsByPlaca(String placa);

    
    Optional<Vehiculo> findByPlaca(String placa);

    
    boolean existsByPlacaAndIdNot(String placa, Long id);

    
    List<Vehiculo> findByEstadoOrderByPrecioAsc(EstadoVehiculo estado);

    
    @Query("SELECT v FROM Vehiculo v WHERE lower(v.marca) = lower(:marca) ORDER BY v.precio ASC")
    List<Vehiculo> buscarPorMarca(@Param("marca") String marca);

    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Vehiculo v WHERE v.id = :id")
    Optional<Vehiculo> findByIdParaActualizar(@Param("id") Long id);

    
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Venta v WHERE v.vehiculo.id = :vehiculoId")
    boolean existeVentaDe(@Param("vehiculoId") Long vehiculoId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Mantenimiento m WHERE m.vehiculo.id = :vehiculoId")
    boolean existeMantenimientoDe(@Param("vehiculoId") Long vehiculoId);
}
