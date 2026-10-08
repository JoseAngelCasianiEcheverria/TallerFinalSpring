package com.tallerautos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerautos.entity.Mantenimiento;

@Repository
public interface MantenimientoRepository extends JpaRepository<Mantenimiento, Long> {

    
    @Query("SELECT m FROM Mantenimiento m "
            + "JOIN FETCH m.vehiculo "
            + "WHERE m.vehiculo.id = :vehiculoId "
            + "ORDER BY m.fechaInicio DESC")
    List<Mantenimiento> findByVehiculoIdOrderByFechaInicioDesc(@Param("vehiculoId") Long vehiculoId);

    @Query("SELECT m FROM Mantenimiento m "
            + "JOIN FETCH m.vehiculo "
            + "ORDER BY m.fechaInicio DESC")
    List<Mantenimiento> findAllConVehiculo();

    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END "
            + "FROM Mantenimiento m "
            + "WHERE m.vehiculo.id = :vehiculoId AND m.estado = com.tallerautos.entity.EstadoMantenimiento.EN_PROCESO")
    boolean existeAbiertoDe(@Param("vehiculoId") Long vehiculoId);
}
