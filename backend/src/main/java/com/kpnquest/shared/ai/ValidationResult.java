package com.kpnquest.shared.ai;

import io.micronaut.core.annotation.Introspected;

@Introspected
public record ValidationResult(boolean valid, String reason) {}