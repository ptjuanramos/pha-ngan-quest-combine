package com.kpnquest.shared.exception;

import io.micronaut.http.HttpStatus;

public class UnauthorizedException extends ApiException {

    private UnauthorizedException(HttpStatus status, String code, String message) {
        super(status, code, message);
    }

    public static UnauthorizedException getUnauthorizedException() {
        return new UnauthorizedException(HttpStatus.FORBIDDEN, "UNAUTHORIZED", "");
    }

    public static UnauthorizedException getUserNotFoundException() {
        return new UnauthorizedException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "");
    }
}
