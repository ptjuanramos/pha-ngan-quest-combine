package com.kpnquest.validatephoto;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Introspected
public record ValidatePhotoRequest(@NotNull Long playerId, @NotBlank String photo) {}