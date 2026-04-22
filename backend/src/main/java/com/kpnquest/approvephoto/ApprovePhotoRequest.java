package com.kpnquest.approvephoto;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotNull;

@Introspected
public record ApprovePhotoRequest(@NotNull Boolean approved) {}