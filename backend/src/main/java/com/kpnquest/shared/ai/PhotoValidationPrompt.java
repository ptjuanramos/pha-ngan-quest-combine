package com.kpnquest.shared.ai;

import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Singleton
public class PhotoValidationPrompt {

    private final String template;

    public PhotoValidationPrompt() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("prompts/photo-validation.txt")) {
            if (is == null) throw new IllegalStateException("prompts/photo-validation.txt not found on classpath");
            template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String build(String keywords) {
        return template.replace("{keywords}", keywords);
    }
}