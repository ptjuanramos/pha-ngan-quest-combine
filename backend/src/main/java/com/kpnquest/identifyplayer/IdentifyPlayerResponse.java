package com.kpnquest.identifyplayer;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record IdentifyPlayerResponse(Long playerId, String token) {}
