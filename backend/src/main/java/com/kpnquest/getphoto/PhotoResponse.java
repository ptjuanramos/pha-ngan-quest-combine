package com.kpnquest.getphoto;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record PhotoResponse(Long photoId, String blobUrl, String validationStatus) {}