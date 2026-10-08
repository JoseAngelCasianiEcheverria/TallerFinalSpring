package com.tallerfinal.springfinal.exception;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.tallerfinal.springfinal.PruebaIntegracion;

/**
 * El manejador de errores funciona en la aplicación completa, con el reloj
 * real en la hora de Colombia (RF-6, RF-60, RNF-6).
 */
class ErroresEnLaAplicacionTest extends PruebaIntegracion {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void unaRutaQueNoExisteRespondeConElFormatoComun() throws Exception {
		mockMvc.perform(get("/rutas/que/no/existen"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.estado").value(404))
			.andExpect(jsonPath("$.error").value("No encontrado"))
			.andExpect(jsonPath("$.mensaje").value("No existe la ruta /rutas/que/no/existen"))
			.andExpect(jsonPath("$.ruta").value("/rutas/que/no/existen"))
			.andExpect(jsonPath("$.fecha").value(endsWith("-05:00")));
	}

}
