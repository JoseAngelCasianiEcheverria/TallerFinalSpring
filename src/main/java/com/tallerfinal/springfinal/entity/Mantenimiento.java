package com.tallerfinal.springfinal.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "mantenimiento")
public class Mantenimiento {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vehiculo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_mantenimiento_vehiculo"))
	private Vehiculo vehiculo;

	@Column(nullable = false, length = 255)
	private String descripcion;

	@Column(nullable = false, precision = 13, scale = 2)
	private BigDecimal costo;

	@Column(nullable = false)
	private Instant fechaIngreso;

	/** Vacía mientras el mantenimiento sigue abierto (RF-50). */
	private Instant fechaFinalizacion;

	protected Mantenimiento() {
	}

	public Mantenimiento(Vehiculo vehiculo, String descripcion, BigDecimal costo, Instant fechaIngreso) {
		this.vehiculo = vehiculo;
		this.descripcion = descripcion;
		this.costo = costo;
		this.fechaIngreso = fechaIngreso;
	}

	public Long getId() {
		return id;
	}

	public Vehiculo getVehiculo() {
		return vehiculo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public BigDecimal getCosto() {
		return costo;
	}

	public void setCosto(BigDecimal costo) {
		this.costo = costo;
	}

	public Instant getFechaIngreso() {
		return fechaIngreso;
	}

	public Instant getFechaFinalizacion() {
		return fechaFinalizacion;
	}

	public void setFechaFinalizacion(Instant fechaFinalizacion) {
		this.fechaFinalizacion = fechaFinalizacion;
	}

	public boolean estaAbierto() {
		return fechaFinalizacion == null;
	}

}
