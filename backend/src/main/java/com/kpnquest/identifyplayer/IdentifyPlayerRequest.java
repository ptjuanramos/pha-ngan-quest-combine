package com.kpnquest.identifyplayer;

import io.micronaut.core.annotation.Introspected;
import jakarta.validation.constraints.NotBlank;

@Introspected
public record IdentifyPlayerRequest(@NotBlank String username) {}
