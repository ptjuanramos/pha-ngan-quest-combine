package com.kpnquest.uploadphoto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpnquest.shared.MssqlContainerExtension;
import com.kpnquest.shared.storage.BlobStorageService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@MicronautTest(transactional = false, environments = "test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UploadPhotoIT extends MssqlContainerExtension implements TestPropertyProvider {

    private static final String STUB_BASE64 =
        "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/wAARC";

    @Inject @Client("/") HttpClient client;
    @Inject ObjectMapper objectMapper;

    private String jwt;

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
    void authenticate() throws Exception {
        createTestPlayer("upload-photo-user");
        var body = Map.of("username", "upload-photo-user");
        String json = client.toBlocking().retrieve(HttpRequest.POST("/api/v1/players/identify", body));
        jwt = objectMapper.readTree(json).path("data").path("token").asText();
    }

    @Test
    void uploadPhoto_success() throws Exception {
        var body = Map.of("base64Content", STUB_BASE64);

        var response = client.toBlocking().exchange(
            HttpRequest.POST("/api/v1/missions/1/photos", body).bearerAuth(jwt),
            String.class
        );

        JsonNode data = objectMapper.readTree(response.body()).path("data");
        assertThat(data.path("photoId").asLong()).isPositive();
        assertThat(data.path("missionId").asInt()).isEqualTo(1);
        assertThat(data.path("blobUrl").asText()).isNotBlank();
        assertThat(data.path("validationStatus").asText()).isEqualTo("PENDING");
    }

    @Test
    void uploadPhoto_twice_upserts() throws Exception {
        createTestPlayer("upload-photo-upsert-user");
        var identifyBody = Map.of("username", "upload-photo-upsert-user");
        String identifyJson = client.toBlocking().retrieve(HttpRequest.POST("/api/v1/players/identify", identifyBody));
        String freshJwt = objectMapper.readTree(identifyJson).path("data").path("token").asText();

        var body = Map.of("base64Content", STUB_BASE64);

        String json1 = client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/3/photos", body).bearerAuth(freshJwt));
        String json2 = client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/3/photos", body).bearerAuth(freshJwt));

        long photoId1 = objectMapper.readTree(json1).path("data").path("photoId").asLong();
        long photoId2 = objectMapper.readTree(json2).path("data").path("photoId").asLong();

        assertThat(photoId1).isEqualTo(photoId2);
    }
}