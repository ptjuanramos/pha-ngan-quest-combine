package com.kpnquest.shared.ai;

import com.azure.ai.vision.imageanalysis.ImageAnalysisClient;
import com.azure.ai.vision.imageanalysis.ImageAnalysisClientBuilder;
import com.azure.ai.vision.imageanalysis.models.DetectedTag;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisOptions;
import com.azure.ai.vision.imageanalysis.models.ImageAnalysisResult;
import com.azure.ai.vision.imageanalysis.models.VisualFeatures;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class AiPhotoValidationService {

    private final ImageAnalysisClient client;
    private final double confidenceThreshold;

    public AiPhotoValidationService(
        @Value("${azure.vision.endpoint}") String endpoint,
        @Value("${azure.vision.api-key}") String apiKey,
        @Value("${azure.vision.confidence-threshold:0.6}") double confidenceThreshold
    ) {
        this.client = new ImageAnalysisClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
        this.confidenceThreshold = confidenceThreshold;
    }

    /**
     * Validate a photo against the expected keywords for a mission.
     *
     * @param base64Content  raw base64 (with or without data URL prefix)
     * @param keywordsCsv    comma-separated expected tags, e.g. "beach,ocean,person"
     */
    public ValidationResult validate(String base64Content, String keywordsCsv) {
        if (keywordsCsv == null || keywordsCsv.isBlank()) {
            return new ValidationResult(false, "No validation keywords configured for this mission");
        }

        try {
            String base64 = base64Content.contains(",") ? base64Content.split(",")[1] : base64Content;
            byte[] bytes = Base64.getDecoder().decode(base64);

            ImageAnalysisResult result = client.analyze(
                BinaryData.fromBytes(bytes),
                List.of(VisualFeatures.TAGS),
                new ImageAnalysisOptions().setLanguage("en")
            );

            Set<String> detectedTags = result.getTags().getValues().stream()
                .filter(tag -> tag.getConfidence() >= confidenceThreshold)
                .map(DetectedTag::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

            List<String> expected = Arrays.stream(keywordsCsv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .toList();

            List<String> matched = expected.stream()
                .filter(detectedTags::contains)
                .toList();

            boolean valid = !matched.isEmpty();
            String reason = valid
                ? "Detected expected elements: " + String.join(", ", matched)
                : "Expected elements not found. Detected: " + String.join(", ", detectedTags);

            return new ValidationResult(valid, reason);

        } catch (Exception e) {
            return new ValidationResult(false, "Validation service unavailable: " + e.getMessage());
        }
    }
}