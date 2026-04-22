package com.kpnquest.approvephoto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpnquest.shared.MssqlContainerExtension;
import com.kpnquest.shared.storage.BlobStorageService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@MicronautTest(transactional = false, environments = "test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApprovePhotoIT extends MssqlContainerExtension implements TestPropertyProvider {

    private static final String STUB_BASE64 =
        "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/wAARC";

    @Inject @Client("/") HttpClient client;
    @Inject ObjectMapper objectMapper;

    private String adminJwt;
    private String userJwt;
    private long photoId;

    @MockBean(BlobStorageService.class)
    BlobStorageService blobStorageService() {
        BlobStorageService mock = Mockito.mock(BlobStorageService.class);
        when(mock.upload(anyString(), any(byte[].class))).thenReturn("https://stub.blob.core.windows.net/photos/test.jpg");
        return mock;
    }

    @Override
    public Map<String, String> getProperties() {
        return datasourceProperties();
    }

    @BeforeAll
    void setup() throws Exception {
        // Admin JWT — godmod is seeded with is_admin = true
        String adminJson = client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/players/identify", Map.of("username", "godmod")));
        adminJwt = objectMapper.readTree(adminJson).path("data").path("token").asText();

        // Regular user JWT
        createTestPlayer("approve-photo-user");
        String userJson = client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/players/identify", Map.of("username", "approve-photo-user")));
        userJwt = objectMapper.readTree(userJson).path("data").path("token").asText();

        // Upload a photo as the regular user to get a photoId
        String uploadJson = client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/1/photos", Map.of("base64Content", STUB_BASE64)).bearerAuth(userJwt));
        photoId = objectMapper.readTree(uploadJson).path("data").path("photoId").asLong();
    }

    @Test
    void approvePhoto_adminApproves_returnsAdminApprovedStatus() throws Exception {
        String json = client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/1/photos/" + photoId + "/approve",
                Map.of("approved", true)).bearerAuth(adminJwt)
        );

        JsonNode data = objectMapper.readTree(json).path("data");
        assertThat(data.path("photoId").asLong()).isEqualTo(photoId);
        assertThat(data.path("validationStatus").asText()).isEqualTo("ADMIN_APPROVED");
    }

    @Test
    void approvePhoto_adminRejects_returnsAdminRejectedStatus() throws Exception {
        // Upload a fresh photo for this test
        String uploadJson = client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/2/photos", Map.of("base64Content", STUB_BASE64)).bearerAuth(userJwt));
        long freshPhotoId = objectMapper.readTree(uploadJson).path("data").path("photoId").asLong();

        String json = client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/2/photos/" + freshPhotoId + "/approve",
                Map.of("approved", false)).bearerAuth(adminJwt)
        );

        JsonNode data = objectMapper.readTree(json).path("data");
        assertThat(data.path("validationStatus").asText()).isEqualTo("ADMIN_REJECTED");
    }

    @Test
    void approvePhoto_nonAdmin_returns401() {
        var ex = catchThrowableOfType(
            () -> client.toBlocking().retrieve(
                HttpRequest.POST("/api/v1/missions/1/photos/" + photoId + "/approve",
                    Map.of("approved", true)).bearerAuth(userJwt)),
            HttpClientResponseException.class
        );
        assertThat(ex.getStatus().getCode()).isEqualTo(401);
    }

    @Test
    void approvePhoto_withoutToken_returns401() {
        var ex = catchThrowableOfType(
            () -> client.toBlocking().retrieve(
                HttpRequest.POST("/api/v1/missions/1/photos/" + photoId + "/approve",
                    Map.of("approved", true))),
            HttpClientResponseException.class
        );
        assertThat(ex.getStatus().getCode()).isEqualTo(401);
    }
}