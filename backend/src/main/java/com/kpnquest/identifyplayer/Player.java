package com.kpnquest.identifyplayer;

import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

import java.time.LocalDateTime;

@MappedEntity("players")
public record Player(
    @Id @GeneratedValue Long id,
    @MappedProperty("username") String username,
    @MappedProperty("is_admin") boolean isAdmin,
    @MappedProperty("created_at") LocalDateTime createdAt,
    @MappedProperty("updated_at") LocalDateTime updatedAt
) {}
