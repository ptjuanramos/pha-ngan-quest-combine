package com.kpnquest.validatephoto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record ValidatePhotoResponse(boolean valid, String reason) {

    public static ValidatePhotoResponse okResponse() {
        return new ValidatePhotoResponse(true, null);
    }
}