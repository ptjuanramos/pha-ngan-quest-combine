package com.kpnquest.uploadphoto;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;

@Introspected
public record UploadPhotoRequest(@NotBlank String base64Content) {}
