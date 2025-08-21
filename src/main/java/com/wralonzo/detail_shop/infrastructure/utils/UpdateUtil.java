package com.wralonzo.detail_shop.infrastructure.utils;

import java.util.Optional;
import java.util.function.Consumer;

public final class UpdateUtil {

    // Constructor privado para evitar la instanciación
    private UpdateUtil() {
    }

    /**
     * Aplica una acción solo si el valor no es nulo.
     * @param value El valor que puede ser nulo.
     * @param consumer La acción a ejecutar si el valor no es nulo.
     */
    public static <T> void updateIfPresent(T value, Consumer<T> consumer) {
        Optional.ofNullable(value).ifPresent(consumer);
    }
}
