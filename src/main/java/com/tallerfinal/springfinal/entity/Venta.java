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

/**
 * Una venta guarda el precio, el descuento, el total y la conversión a USD del
 * momento en que se registró: no cambian si después cambia el vehículo
 * (RF-34, RF-77). Por eso no tiene setters.
 */
@Entity
@Table(name = "venta")
public class Venta {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "cliente_id", nullable = false, foreignKey = @ForeignKey(name = "fk_venta_cliente"))
	private Cliente cliente;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "vehiculo_id", nullable = false, foreignKey = @ForeignKey(name = "fk_venta_vehiculo"))
	private Vehiculo vehiculo;

	@Column(nullable = false)
	private Instant fecha;

	@Column(nullable = false, precision = 13, scale = 2)
	private BigDecimal precio;

	@Column(nullable = false, precision = 13, scale = 2)
	private BigDecimal descuento;

	@Column(nullable = false, precision = 13, scale = 2)
	private BigDecimal total;

	/** Tasa COP → USD vigente al registrar; vacía si no había tasa (RF-77). */
	@Column(precision = 20, scale = 12)
	private BigDecimal tasaCambio;

	@Column(precision = 13, scale = 2)
	private BigDecimal totalUsd;

	/** Por qué no hay valor en USD, si no lo hay (RF-55). */
	@Column(length = 255)
	private String motivoUsd;

	protected Venta() {
	}

	public Venta(Cliente cliente, Vehiculo vehiculo, Instant fecha, BigDecimal precio, BigDecimal descuento,
			BigDecimal total, BigDecimal tasaCambio, BigDecimal totalUsd, String motivoUsd) {
		this.cliente = cliente;
		this.vehiculo = vehiculo;
		this.fecha = fecha;
		this.precio = precio;
		this.descuento = descuento;
		this.total = total;
		this.tasaCambio = tasaCambio;
		this.totalUsd = totalUsd;
		this.motivoUsd = motivoUsd;
	}

	public Long getId() {
		return id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public Vehiculo getVehiculo() {
		return vehiculo;
	}

	public Instant getFecha() {
		return fecha;
	}

	public BigDecimal getPrecio() {
		return precio;
	}

	public BigDecimal getDescuento() {
		return descuento;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public BigDecimal getTasaCambio() {
		return tasaCambio;
	}

	public BigDecimal getTotalUsd() {
		return totalUsd;
	}

	public String getMotivoUsd() {
		return motivoUsd;
	}

}
