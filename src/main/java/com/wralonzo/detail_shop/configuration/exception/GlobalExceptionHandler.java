package com.wralonzo.detail_shop.configuration.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.coyote.BadRequestException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Recurso no encontrado. La URL o el método de la petición no es válido.",
                LocalDateTime.now(),
                ex.getClass().getSimpleName());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // Maneja excepciones de tipo IllegalArgumentException (ej. validaciones de
    // negocio)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(), // Usa el mensaje de la excepción para dar detalles
                LocalDateTime.now(),
                ex.getClass().getSimpleName());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Este es el manejador por defecto para cualquier otra excepción
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor",
                LocalDateTime.now(),
                ex.getClass().getSimpleName() +
                        ex.getMessage());
        ex.printStackTrace(); // Recomendado para depurar
        System.out.println();
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Este es el manejador 409
    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<ErrorResponse> handleResourceConflictException(ResourceConflictException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                ex.getClass().getSimpleName());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((org.springframework.validation.FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Error");
        response.put("message", "La petición no es válida debido a errores de validación en los campos.");
        response.put("details", errors);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Maneja errores cuando el cuerpo de la petición está vacío o mal formado.
     * Retorna un código 400 Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleReadableException(HttpMessageNotReadableException ex) {
        System.err.println("===== ERROR 400 - PAYLOAD INVÁLIDO =====");
        System.err.println("Causa: " + ex.getMostSpecificCause().getMessage());

        // Si quieres ver el log completo en 'docker logs'
        ex.printStackTrace();
        Map<String, Object> response = new HashMap<>();
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("exception", "BadRequest");

        // ESTA LÍNEA ES LA CLAVE:
        // Te dirá: "Cannot deserialize value of type Long from String 'A4340D'"
        String message = ex.getMostSpecificCause().getMessage();
        response.put("message", message);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(), // Aquí vendrá "La contraseña debe contener al menos una letra mayúscula"
                LocalDateTime.now(),
                ex.getClass().getSimpleName());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // También es recomendable agregar ResourceUnauthorizedException si la estás
    // usando
    @ExceptionHandler(ResourceUnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleResourceUnauthorizedException(ResourceUnauthorizedException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                ex.getMessage(),
                LocalDateTime.now(),
                ex.getClass().getSimpleName());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    /**
     * ✅ NUEVO: Maneja el error 401 No Autorizado cuando las credenciales
     * (usuario/contraseña) son incorrectas.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(), // Devuelve 401
                "Credenciales de acceso incorrectas. Verifica tu usuario y contraseña.",
                LocalDateTime.now(),
                ex.getClass().getSimpleName());
        // ¡Cambiamos el estado a 401 UNAUTHORIZED!
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex) {
        Map<String, Object> errorDetails = Map.of(
                "timestamp", System.currentTimeMillis(),
                "status", HttpStatus.FORBIDDEN.value(),
                "error", "Forbidden",
                "message", "Acceso denegado. No tiene permisos para este recurso.");
        return new ResponseEntity<>(errorDetails, HttpStatus.FORBIDDEN);
    }

    /**
     * Maneja el error cuando falta un parámetro requerido en la URL (?param=valor).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String message = String.format("Falta el parámetro obligatorio: %s (%s)",
                ex.getParameterName(), ex.getParameterType());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                message,
                LocalDateTime.now(),
                ex.getClass().getSimpleName());

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ErrorResponse> handleDisabledException(DisabledException ex, HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(), // Devuelve 401
                "Esta cuenta ha sido desactivada. Por favor, contacta al administrado",
                LocalDateTime.now(),
                ex.getClass().getSimpleName());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * Maneja errores de tipo de dato en los parámetros (ej. enviar texto en un ID
     * numérico).
     */// Manejador consolidado para INTEGRIDAD DE DATOS (Intercepción dinámica)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String rootMsg = ex.getMostSpecificCause().getMessage().toLowerCase();
        String friendlyMessage = "Error de integridad: El registro viola una restricción de la base de datos.";

        if (rootMsg.contains("duplicate key") || rootMsg.contains("unique constraint")) {
            friendlyMessage = "Ya existe un registro con estos datos únicos.";
            if (rootMsg.contains("username"))
                friendlyMessage = "El nombre de usuario ya está en uso.";
            if (rootMsg.contains("email") || rootMsg.contains("uk_client_email"))
                friendlyMessage = "El correo electrónico ya está registrado.";
        } else if (rootMsg.contains("violates foreign key constraint")) {
            friendlyMessage = "No se puede realizar la operación porque el registro está relacionado con otra información.";
        } else if (rootMsg.contains("violates not-null constraint")) {
            friendlyMessage = "Faltan campos obligatorios para completar la operación.";
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                friendlyMessage,
                LocalDateTime.now(),
                "DatabaseIntegrityError");

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Captura errores generales de acceso a datos (Conexión perdida, Timeout, Error
     * de sintaxis SQL)
     */
    @ExceptionHandler(org.springframework.dao.DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleGeneralDataError(org.springframework.dao.DataAccessException ex) {
        // Log para el desarrollador (Docker/Consola)
        System.err.println("Database Error: " + ex.getMostSpecificCause().getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "El servicio de base de datos no está disponible temporalmente. Por favor, intente más tarde.",
                LocalDateTime.now(),
                "PersistenceException");

        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }
}