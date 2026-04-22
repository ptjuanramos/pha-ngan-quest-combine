package com.kpnquest.uploadphoto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record UploadPhotoResponse(Long photoId, int missionId, String blobUrl, String validationStatus) {}