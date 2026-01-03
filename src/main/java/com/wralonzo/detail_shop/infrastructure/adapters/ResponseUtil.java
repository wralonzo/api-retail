package com.wralonzo.detail_shop.infrastructure.adapters;

import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

public final class ResponseUtil {

    private ResponseUtil() {
        // Clase utilitaria, no instanciable
    }

    public static <T, ID> ResponseEntity<T> created(T resource, ID id) {
        // Construye la URI del nuevo recurso usando el ID
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(id)
                .toUri();

        // Devuelve la respuesta 201 con la URI en el encabezado Location y el cuerpo
        return ResponseEntity.created(location).body(resource);
    }

    public static <T> ResponseEntity<T> ok(T resource) {
        return ResponseEntity.ok(resource);
    }

    /**
     * 204 No Content: Úsalo para borrados (DELETE) exitosos
     * o actualizaciones donde no quieres devolver datos.
     */
    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }
}