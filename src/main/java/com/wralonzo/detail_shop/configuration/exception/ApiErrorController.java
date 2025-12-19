package com.wralonzo.detail_shop.configuration.exception; // O donde guardas tus configuraciones

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/error") // Ruta predeterminada de manejo de errores de Spring
public class ApiErrorController implements ErrorController {

    @RequestMapping
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object errorMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        int statusCode = status != null ? (Integer) status : HttpStatus.INTERNAL_SERVER_ERROR.value();
        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String message = errorMessage != null ? errorMessage.toString() : "Error desconocido en el servidor.";

        HttpStatus httpStatus = HttpStatus.valueOf(statusCode);

        Map<String, Object> errorDetails = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", statusCode,
                "error", httpStatus.getReasonPhrase(),
                "message", message.isEmpty() ? httpStatus.getReasonPhrase() : message,
                "path", path != null ? path : "N/A"
        );

        return new ResponseEntity<>(errorDetails, httpStatus);
    }
}