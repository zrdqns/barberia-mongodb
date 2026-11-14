package com.barberia.citas.controller;

import com.barberia.citas.dto.MensajeDTO;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

// Convierte los errores que lanzan los controllers y los services en respuestas HTTP
// con el código que corresponde y un mensaje para el cliente.
@RestControllerAdvice
public class ManejadorErrores {

    // @Valid encontró campos inválidos -> 400 con el problema de cada campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MensajeDTO> camposInvalidos(MethodArgumentNotValidException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        // El primer problema también va como mensaje general, para mostrarlo tal cual
        String mensaje = errores.isEmpty() ? "Los datos enviados no son válidos." : errores.values().iterator().next();
        return ResponseEntity.badRequest().body(new MensajeDTO(mensaje, errores));
    }

    // Datos inválidos detectados en el service -> 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MensajeDTO> datosInvalidos(IllegalArgumentException e) {
        return respuesta(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // El cuerpo no se pudo convertir al DTO -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MensajeDTO> cuerpoIlegible(HttpMessageNotReadableException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no tiene el formato esperado.");
    }

    // Un parámetro que no tiene el tipo esperado -> 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MensajeDTO> parametroInvalido(MethodArgumentTypeMismatchException e) {
        return respuesta(HttpStatus.BAD_REQUEST, "El valor de " + e.getName() + " no es válido.");
    }

    // Usuario o contraseña incorrectos en el login -> 401
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MensajeDTO> credenciales(BadCredentialsException e) {
        return respuesta(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // El documento no existe -> 404
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<MensajeDTO> noEncontrado(NoSuchElementException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // La operación choca con una regla de negocio o con el estado actual de los datos -> 409
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<MensajeDTO> conflicto(IllegalStateException e) {
        return respuesta(HttpStatus.CONFLICT, e.getMessage());
    }

    // MongoDB rechazó el cambio por un índice único (por ejemplo, un documento repetido que llegó
    // al mismo tiempo desde otra petición) -> 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MensajeDTO> restriccion(DataIntegrityViolationException e) {
        return respuesta(HttpStatus.CONFLICT, "La base de datos rechazó el cambio: choca con un registro existente.");
    }

    // api-catalogo está apagada o no respondió -> 503
    // La comunicación es síncrona: sin su respuesta no se puede continuar.
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<MensajeDTO> catalogoNoDisponible(RestClientException e) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE,
                "El catálogo de la barbería (api-catalogo) no está disponible. Intente de nuevo más tarde.");
    }

    private ResponseEntity<MensajeDTO> respuesta(HttpStatus estado, String mensaje) {
        return ResponseEntity
                .status(estado)
                .body(new MensajeDTO(mensaje));
    }
}
