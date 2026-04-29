package com.kpnquest.listplayercompletions;

import io.micronaut.core.annotation.Introspected;

import java.util.List;

@Introspected
public record PlayerMissionStatusResponse(
    Long playerId,
    String username,
    List<MissionStatusResponse> missions
) {}