package com.tallerautos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tallerautos.entity.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    
    @Query("SELECT v FROM Venta v "
            + "JOIN FETCH v.cliente "
            + "JOIN FETCH v.vehiculo "
            + "ORDER BY v.fechaVenta DESC")
    List<Venta> findAllConRelaciones();

    
    @Query("SELECT v FROM Venta v "
            + "JOIN FETCH v.cliente "
            + "JOIN FETCH v.vehiculo "
            + "WHERE v.cliente.id = :clienteId "
            + "ORDER BY v.fechaVenta DESC")
    List<Venta> findByClienteIdConRelaciones(@Param("clienteId") Long clienteId);

    
    @Query("SELECT v FROM Venta v "
            + "JOIN FETCH v.cliente "
            + "JOIN FETCH v.vehiculo "
            + "WHERE v.vehiculo.id = :vehiculoId")
    List<Venta> findByVehiculoIdConRelaciones(@Param("vehiculoId") Long vehiculoId);
}
