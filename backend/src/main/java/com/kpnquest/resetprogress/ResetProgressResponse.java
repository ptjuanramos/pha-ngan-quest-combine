package com.kpnquest.resetprogress;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record ResetProgressResponse(long deletedCompletions, long deletedPhotos) {}