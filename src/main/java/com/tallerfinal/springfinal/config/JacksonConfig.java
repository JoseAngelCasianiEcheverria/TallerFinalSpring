package com.tallerfinal.springfinal.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.cfg.CoercionAction;
import tools.jackson.databind.cfg.CoercionInputShape;
import tools.jackson.databind.type.LogicalType;

@Configuration(proxyBeanMethods = false)
public class JacksonConfig {

	/**
	 * Un tipo de dato equivocado en el pedido es un error (RF-1). Sin esto,
	 * Spring Boot acepta {@code "anio": 2024.5} como 2024, {@code "precio": "1000"}
	 * como número o {@code "nombre": 123} como texto. Los campos desconocidos se
	 * siguen ignorando (RF-62).
	 */
	@Bean
	JsonMapperBuilderCustomizer tiposEstrictos() {
		return builder -> builder
				.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
				.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
				.withCoercionConfig(LogicalType.Textual, textos -> textos
						.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
						.setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
						.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
	}

}
