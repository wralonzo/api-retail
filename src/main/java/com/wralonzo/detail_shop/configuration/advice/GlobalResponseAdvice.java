package com.wralonzo.detail_shop.configuration.advice;

import com.wralonzo.detail_shop.configuration.response.ApiResponse;
import com.wralonzo.detail_shop.configuration.exception.ErrorResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.ByteArrayHttpMessageConverter; // Importante
import org.springframework.http.converter.ResourceHttpMessageConverter; // Importante
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType) {

        // 1. NO envolver si ya es ApiResponse
        if (returnType.getParameterType().equals(ApiResponse.class)) {
            return false;
        }

        // 2. NO envolver si el tipo de retorno es un arreglo de bytes (Archivos/Excel)
        if (returnType.getParameterType().equals(byte[].class)) {
            return false;
        }

        // 3. NO envolver si se están usando conversores de recursos o binarios
        return !converterType.equals(ByteArrayHttpMessageConverter.class) &&
                !converterType.equals(ResourceHttpMessageConverter.class);
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        // Evitar envolver errores o si el cuerpo es nulo/bytes
        if (body == null || body instanceof ErrorResponse || body instanceof byte[]) {
            return body;
        }

        // Si el Content-Type no es JSON (ej. es Excel), no envolver
        if (!selectedContentType.includes(MediaType.APPLICATION_JSON)) {
            return body;
        }

        int status = HttpStatus.OK.value();
        if (response instanceof ServletServerHttpResponse servletResponse) {
            status = servletResponse.getServletResponse().getStatus();
        }

        // 2. Si el status es 400 o mayor, o ya es un ErrorResponse, NO envolver en
        // ApiResponse
        if (status >= 400 || body instanceof ErrorResponse || body instanceof byte[]) {
            return body; // Devolver el error tal cual para que Rust lo procese en el bloque
                         // !status.is_success()
        }

        // 3. Solo envolver si es éxito
        if (body == null)
            return null;

        return new ApiResponse<>(body, status);
    }

}