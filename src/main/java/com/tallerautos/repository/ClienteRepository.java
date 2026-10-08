package com.tallerautos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import com.tallerautos.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    
    boolean existsByEmail(String email);

    
    List<Cliente> findAllByOrderByIdAsc();

    
    boolean existsByEmailAndIdNot(String email, Long id);

    
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END FROM Venta v WHERE v.cliente.id = :clienteId")
    boolean existeVentaDe(@Param("clienteId") Long clienteId);
}
