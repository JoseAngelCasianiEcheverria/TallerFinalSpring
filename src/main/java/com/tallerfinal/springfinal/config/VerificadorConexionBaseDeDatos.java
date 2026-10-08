package com.tallerfinal.springfinal.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Revisa al arrancar, antes de crear la conexión, que la URL, el usuario y la
 * contraseña de la base estén definidos y que la URL sea JDBC de PostgreSQL
 * (RNF-8). Corre después de que las pruebas ponen la URL del PostgreSQL
 * embebido. Nunca copia el valor recibido en el mensaje: la cadena de Neon
 * trae la contraseña.
 */
@Component
public class VerificadorConexionBaseDeDatos implements BeanFactoryPostProcessor, EnvironmentAware {

	private static final String PREFIJO_JDBC = "jdbc:postgresql://";

	private Environment entorno;

	@Override
	public void setEnvironment(Environment entorno) {
		this.entorno = entorno;
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
		verificar(entorno);
	}

	static void verificar(Environment entorno) {
		List<String> problemas = new ArrayList<>();

		String url = leer(entorno, "spring.datasource.url");
		if (url == null) {
			problemas.add("DB_URL no está definida");
		}
		else if (!url.startsWith(PREFIJO_JDBC)) {
			problemas.add("DB_URL no es una URL JDBC de PostgreSQL: debe empezar con " + PREFIJO_JDBC
					+ " (por ejemplo jdbc:postgresql://localhost:5432/autodrive)");
		}
		if (leer(entorno, "spring.datasource.username") == null) {
			problemas.add("DB_USUARIO no está definida");
		}
		if (leer(entorno, "spring.datasource.password") == null) {
			problemas.add("DB_CONTRASENA no está definida");
		}

		if (!problemas.isEmpty()) {
			throw new ConfiguracionBaseDeDatosException(problemas);
		}
	}

	/** El valor ya resuelto, o {@code null} si falta, está vacío o su variable no existe. */
	private static String leer(Environment entorno, String propiedad) {
		try {
			String valor = entorno.getProperty(propiedad);
			return (valor == null || valor.isBlank()) ? null : valor.strip();
		}
		catch (IllegalArgumentException variableSinDefinir) {
			return null;
		}
	}

}
