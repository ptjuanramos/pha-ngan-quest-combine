package com.kpnquest.approvephoto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record ApprovePhotoResponse(Long photoId, String validationStatus) {}