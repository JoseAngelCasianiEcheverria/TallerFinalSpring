package com.tallerautos.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.tallerautos.dto.response.ApiError;


@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex,
                                                   ServletWebRequest request) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiError> handleBusinessRule(BusinessRuleException ex,
                                                       ServletWebRequest request) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex,
                                                        ServletWebRequest request) {
        log.warn("Violacion de integridad no detectada en el servicio: {}", ex.getMessage());
        return construir(HttpStatus.CONFLICT,
                "El registro viola una regla de unicidad de la base de datos.",
                request, null);
    }

    
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiError> handleRestClient(RestClientException ex,
                                                     ServletWebRequest request) {
        log.warn("Fallo la llamada a la API externa: {}", ex.getMessage());
        return construir(HttpStatus.BAD_GATEWAY,
                "El servicio externo de tasa de cambio no responde.", request, null);
    }

    
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex,
                                                              Object body,
                                                              HttpHeaders headers,
                                                              HttpStatusCode status,
                                                              WebRequest request) {

        List<String> detalles = null;
        String mensaje;

        if (ex instanceof MethodArgumentNotValidException validacion) {
            
            
            
            detalles = new ArrayList<>();
            for (FieldError error : validacion.getBindingResult().getFieldErrors()) {
                detalles.add(error.getField() + ": " + error.getDefaultMessage());
            }
            mensaje = "Datos invalidos. Revisa los campos marcados.";

        } else if (ex instanceof NoResourceFoundException sinRecurso) {
            String recurso = sinRecurso.getResourcePath();
            mensaje = (recurso == null || recurso.isBlank())
                    ? "No existe ese recurso."
                    : "No existe el recurso " + recurso + ".";

        } else if (status.is5xxServerError()) {
            
            log.error("Error inesperado en {} {}", metodo(request), ruta(request), ex);
            mensaje = "Ocurrio un error inesperado en el servidor. Intenta de nuevo.";

        } else {
            mensaje = ex.getMessage();
        }

        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.toString(),
                (mensaje == null || mensaje.isBlank()) ? "La peticion no se pudo procesar." : mensaje,
                ruta(request),
                detalles);

        return new ResponseEntity<>(error, headers, status);
    }

    private ResponseEntity<ApiError> construir(HttpStatus status, String mensaje,
                                               ServletWebRequest request,
                                               List<String> detalles) {

        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                ruta(request),
                detalles);

        return ResponseEntity.status(status).body(error);
    }

    
    private String ruta(WebRequest request) {
        if (request instanceof ServletWebRequest servlet) {
            return servlet.getRequest().getRequestURI();
        }
        return "";
    }

    private String metodo(WebRequest request) {
        if (request instanceof ServletWebRequest servlet) {
            return servlet.getRequest().getMethod();
        }
        return "?";
    }
}
