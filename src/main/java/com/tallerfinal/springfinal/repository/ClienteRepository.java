package com.tallerfinal.springfinal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tallerfinal.springfinal.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

	/** RF-12. */
	List<Cliente> findAllByOrderByIdAsc();

}
