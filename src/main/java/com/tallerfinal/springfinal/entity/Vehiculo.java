package com.tallerfinal.springfinal.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "vehiculo", uniqueConstraints = @UniqueConstraint(name = "uq_vehiculo_placa", columnNames = "placa"))
public class Vehiculo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 7)
	private String placa;

	@Column(nullable = false, length = 50)
	private String marca;

	/** Nombre comercial (Corolla, Mazda 3), no el año (RF-17). */
	@Column(nullable = false, length = 50)
	private String linea;

	@Column(nullable = false)
	private Integer anio;

	/** Pesos colombianos con 2 decimales, hasta $10.000.000.000,00 (RNF-2, RF-73). */
	@Column(nullable = false, precision = 13, scale = 2)
	private BigDecimal precio;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EstadoVehiculo estado;

	protected Vehiculo() {
	}

	public Vehiculo(String placa, String marca, String linea, Integer anio, BigDecimal precio, EstadoVehiculo estado) {
		this.placa = placa;
		this.marca = marca;
		this.linea = linea;
		this.anio = anio;
		this.precio = precio;
		this.estado = estado;
	}

	public Long getId() {
		return id;
	}

	public String getPlaca() {
		return placa;
	}

	public void setPlaca(String placa) {
		this.placa = placa;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getLinea() {
		return linea;
	}

	public void setLinea(String linea) {
		this.linea = linea;
	}

	public Integer getAnio() {
		return anio;
	}

	public void setAnio(Integer anio) {
		this.anio = anio;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public void setPrecio(BigDecimal precio) {
		this.precio = precio;
	}

	public EstadoVehiculo getEstado() {
		return estado;
	}

	public void setEstado(EstadoVehiculo estado) {
		this.estado = estado;
	}

}
