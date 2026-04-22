package com.kpnquest.validatephoto;

import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.MappedProperty;

@MappedEntity("missions")
public record MissionKeywords(
    @Id Integer id,
    @MappedProperty("validation_keywords") String validationKeywords
) {}