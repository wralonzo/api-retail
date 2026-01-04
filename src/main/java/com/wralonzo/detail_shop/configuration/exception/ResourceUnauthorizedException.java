package com.wralonzo.detail_shop.configuration.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class ResourceUnauthorizedException extends RuntimeException {

    public ResourceUnauthorizedException(String message) {
        super(message);
    }
}