package com.kpnquest.shared.domain;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.time.LocalDateTime;

@MappedEntity("photos")
public record Photo(
    @Id @GeneratedValue Long id,
    @MappedProperty("player_id") Long playerId,
    @MappedProperty("mission_id") Integer missionId,
    @MappedProperty("blob_path") String blobPath,
    @MappedProperty("sas_token") String sasToken,
    @MappedProperty("sas_expires_at") LocalDateTime sasExpiresAt,
    @MappedProperty("validation_status") String validationStatus,
    @MappedProperty("created_at") LocalDateTime createdAt,
    @MappedProperty("updated_at") LocalDateTime updatedAt
) {}