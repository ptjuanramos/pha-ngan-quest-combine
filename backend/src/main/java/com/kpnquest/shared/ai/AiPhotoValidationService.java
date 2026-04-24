package com.kpnquest.shared.ai;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatMessageContentItem;
import com.azure.ai.openai.models.ChatMessageImageContentItem;
import com.azure.ai.openai.models.ChatMessageImageUrl;
import com.azure.ai.openai.models.ChatMessageTextContentItem;
import com.azure.ai.openai.models.ChatRequestUserMessage;
import com.azure.core.credential.AzureKeyCredential;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class AiPhotoValidationService {

    private final OpenAIClient client;
    private final String deploymentName;
    private final PhotoValidationPrompt prompt;

    public AiPhotoValidationService(
        @Value("${azure.openai.endpoint}") String endpoint,
        @Value("${azure.openai.api-key}") String apiKey,
        @Value("${azure.openai.deployment-name:gpt-4o-mini}") String deploymentName,
        PhotoValidationPrompt prompt
    ) {
        this.client = new OpenAIClientBuilder()
            .endpoint(endpoint)
            .credential(new AzureKeyCredential(apiKey))
            .buildClient();
        this.deploymentName = deploymentName;
        this.prompt = prompt;
    }

    public ValidationResult validate(String base64Content, String keywordsCsv) {
        if (keywordsCsv == null || keywordsCsv.isBlank()) {
            return new ValidationResult(false, "No validation keywords configured for this mission");
        }

        try {
            String base64 = base64Content.contains(",") ? base64Content.split(",")[1] : base64Content;
            String dataUrl = "data:image/jpeg;base64," + base64;

            List<ChatMessageContentItem> content = List.of(
                new ChatMessageTextContentItem(prompt.build(keywordsCsv)),
                new ChatMessageImageContentItem(new ChatMessageImageUrl(dataUrl))
            );

            String answer = client.getChatCompletions(
                deploymentName,
                new ChatCompletionsOptions(List.of(new ChatRequestUserMessage(content)))
            ).getChoices().getFirst().getMessage().getContent().trim();

            boolean valid = answer.toUpperCase().startsWith("YES");

            String reason = valid
                ? "Photo contains all expected elements: " + keywordsCsv
                : "Photo does not clearly show all expected elements: " + keywordsCsv;

            return new ValidationResult(valid, reason);

        } catch (Exception e) {
            return new ValidationResult(false, "Validation service unavailable: " + e.getMessage());
        }
    }
}