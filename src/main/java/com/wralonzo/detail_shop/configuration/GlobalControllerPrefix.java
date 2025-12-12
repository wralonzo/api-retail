package com.wralonzo.detail_shop.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.bind.annotation.RestController;

/**
 * Clase de configuración para aplicar el prefijo '/api' a todos los controladores REST.
 * Esta es la implementación de la Opción B.
 */
@Configuration
public class GlobalControllerPrefix implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // Aplica el prefijo "/api" a cualquier clase anotada con @RestController
        configurer.addPathPrefix("/api", c -> c.isAnnotationPresent(RestController.class));
    }
}