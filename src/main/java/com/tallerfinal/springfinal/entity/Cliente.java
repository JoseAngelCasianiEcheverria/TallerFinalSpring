package com.tallerfinal.springfinal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Los nombres de las restricciones únicas los usa el manejador de errores para
 * elegir el mensaje cuando dos pedidos simultáneos las violan (RF-68).
 */
@Entity
@Table(name = "cliente", uniqueConstraints = {
		@UniqueConstraint(name = "uq_cliente_documento", columnNames = "documento"),
		@UniqueConstraint(name = "uq_cliente_correo", columnNames = "correo") })
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String nombre;

	@Column(nullable = false, length = 15)
	private String documento;

	@Column(nullable = false, length = 254)
	private String correo;

	@Column(nullable = false, length = 16)
	private String telefono;

	protected Cliente() {
	}

	public Cliente(String nombre, String documento, String correo, String telefono) {
		this.nombre = nombre;
		this.documento = documento;
		this.correo = correo;
		this.telefono = telefono;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

}
