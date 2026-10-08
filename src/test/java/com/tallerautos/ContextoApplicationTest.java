package com.tallerautos;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tallerautos;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "datos.iniciales.activar=false",
        "tasa.api.url=http://localhost:9999/no-existe",
        "tasa.valor-por-defecto=3200"
})
class ContextoApplicationTest {

    @Autowired
    private org.springframework.context.ApplicationContext contexto;

    @Test
    @DisplayName("V1: el contexto de Spring arranca y todos los beans existen")
    void elContextoArranca() {

        assertDoesNotThrow(() -> {
            contexto.getBean(com.tallerautos.repository.ClienteRepository.class);
            contexto.getBean(com.tallerautos.repository.VehiculoRepository.class);
            contexto.getBean(com.tallerautos.repository.VentaRepository.class);
            contexto.getBean(com.tallerautos.repository.MantenimientoRepository.class);

            contexto.getBean(com.tallerautos.service.ClienteService.class);
            contexto.getBean(com.tallerautos.service.VehiculoService.class);
            contexto.getBean(com.tallerautos.service.VentaService.class);
            contexto.getBean(com.tallerautos.service.MantenimientoService.class);
            contexto.getBean(com.tallerautos.service.TasaCambioService.class);

            contexto.getBean(com.tallerautos.client.TasaCambioClient.class);
            contexto.getBean(com.tallerautos.mapper.ClienteMapper.class);
            contexto.getBean(com.tallerautos.mapper.VehiculoMapper.class);
            contexto.getBean(com.tallerautos.mapper.VentaMapper.class);
            contexto.getBean(com.tallerautos.mapper.MantenimientoMapper.class);

            contexto.getBean(com.tallerautos.exception.GlobalExceptionHandler.class);

            contexto.getBean(com.tallerautos.controller.ClienteController.class);
            contexto.getBean(com.tallerautos.controller.VehiculoController.class);
            contexto.getBean(com.tallerautos.controller.VentaController.class);
            contexto.getBean(com.tallerautos.controller.MantenimientoController.class);
            contexto.getBean(com.tallerautos.controller.TasaCambioController.class);
        });
    }
}
