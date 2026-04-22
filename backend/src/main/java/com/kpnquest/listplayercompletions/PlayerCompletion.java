package com.kpnquest.listplayercompletions;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.time.LocalDateTime;

@MappedEntity("mission_completions")
public record PlayerCompletion(
    @Id Long id,
    @MappedProperty("player_id") Long playerId,
    @MappedProperty("mission_id") Integer missionId,
    @MappedProperty("completed_at") LocalDateTime completedAt
) {}