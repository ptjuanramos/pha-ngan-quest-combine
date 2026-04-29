package com.kpnquest.listplayercompletions;

import io.micronaut.core.annotation.Introspected;

import java.time.LocalDateTime;

@Introspected
public record MissionStatusResponse(
    int missionId,
    String title,
    boolean completed,
    LocalDateTime completedAt,
    String photoUrl,
    String validationStatus
) {}