package com.kpnquest.validatephoto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpnquest.shared.MssqlContainerExtension;
import com.kpnquest.shared.ai.AiPhotoValidationService;
import com.kpnquest.shared.ai.ValidationResult;
import com.kpnquest.shared.storage.BlobStorageService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@MicronautTest(transactional = false, environments = "test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ValidatePhotoIT extends MssqlContainerExtension implements TestPropertyProvider {

    private static final String STUB_BASE64 =
        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";

    @Inject @Client("/") HttpClient client;
    @Inject ObjectMapper objectMapper;

    private long playerId;
    private String jwt;

    @MockBean(BlobStorageService.class)
    BlobStorageService blobStorageService() {
        BlobStorageService mock = Mockito.mock(BlobStorageService.class);
        when(mock.upload(anyString(), any(byte[].class))).thenReturn("1/1/test.jpg");
        when(mock.generateSas(anyString())).thenReturn(
            new BlobStorageService.SasResult("stub-token", LocalDateTime.now().plusDays(30)));
        when(mock.buildUrl(anyString(), anyString())).thenReturn(
            "https://stub.blob.core.windows.net/photos/test.jpg?sv=stub");
        return mock;
    }

    @MockBean(AiPhotoValidationService.class)
    AiPhotoValidationService aiPhotoValidationService() {
        return Mockito.mock(AiPhotoValidationService.class);
    }

    @Inject AiPhotoValidationService aiValidationService;

    @BeforeEach
    void resetMocks() {
        Mockito.reset(aiValidationService);
    }

    @Override
    public Map<String, String> getProperties() {
        return datasourceProperties();
    }

    @BeforeAll
    void authenticate() throws Exception {
        playerId = createTestPlayer("validate-photo-user");
        var body = Map.of("username", "validate-photo-user");
        String json = client.toBlocking().retrieve(HttpRequest.POST("/api/v1/players/identify", body));
        jwt = objectMapper.readTree(json).path("data").path("token").asText();

        // Upload a photo for mission 1 so validate can update the status
        var uploadBody = Map.of("base64Content", STUB_BASE64);
        client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/1/photos", uploadBody).bearerAuth(jwt));
    }

    @Test
    void validate_aiApproves_returnsValidTrue() throws Exception {
        when(aiValidationService.validate(anyString(), anyString()))
            .thenReturn(new ValidationResult(true, "Detected expected elements: beach"));

        var body = Map.of("playerId", playerId, "photo", STUB_BASE64);

        String json = client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/1/photos/validate", body).bearerAuth(jwt)
        );

        JsonNode data = objectMapper.readTree(json).path("data");
        assertThat(data.path("valid").asBoolean()).isTrue();
        assertThat(data.path("reason").asText()).contains("beach");
    }

    @Test
    void validate_aiRejects_returnsValidFalse() throws Exception {
        when(aiValidationService.validate(anyString(), anyString()))
            .thenReturn(new ValidationResult(false, "Expected elements not found"));

        var body = Map.of("playerId", playerId, "photo", STUB_BASE64);

        String json = client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/2/photos/validate", body).bearerAuth(jwt)
        );

        JsonNode data = objectMapper.readTree(json).path("data");
        assertThat(data.path("valid").asBoolean()).isFalse();
        assertThat(data.path("reason").asText()).isNotBlank();
    }

    @Test
    void validate_spicyMission_autoApproves() throws Exception {
        // Missions 7 and 8 are spicy — AI is never called
        var body = Map.of("playerId", playerId, "photo", STUB_BASE64);

        String json = client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/7/photos/validate", body).bearerAuth(jwt)
        );

        JsonNode data = objectMapper.readTree(json).path("data");
        assertThat(data.path("valid").asBoolean()).isTrue();

        // Confirm AI was never invoked
        Mockito.verify(aiValidationService, Mockito.never()).validate(anyString(), anyString());
    }
}