package com.kpnquest.listplayercompletions;

import io.micronaut.core.annotation.Introspected;

import java.time.LocalDateTime;

@Introspected
public record PlayerCompletionResponse(Integer missionId, LocalDateTime completedAt) {}