package com.kpnquest.getphoto;

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

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@MicronautTest(transactional = false, environments = "test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GetPhotoIT extends MssqlContainerExtension implements TestPropertyProvider {

    private static final String STUB_BASE64 =
        "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=";
    private static final String STUB_BLOB_URL = "https://stub.blob.core.windows.net/photos/test.jpg";

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
        when(mock.buildUrl(anyString(), anyString())).thenReturn(STUB_BLOB_URL);
        return mock;
    }

    @Override
    public Map<String, String> getProperties() {
        return datasourceProperties();
    }

    @BeforeAll
    void authenticate() throws Exception {
        playerId = createTestPlayer("get-photo-user");
        var body = Map.of("username", "get-photo-user");
        String json = client.toBlocking().retrieve(HttpRequest.POST("/api/v1/players/identify", body));
        jwt = objectMapper.readTree(json).path("data").path("token").asText();

        // Upload a photo so we have something to retrieve
        var uploadBody = Map.of("base64Content", STUB_BASE64);
        client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/1/photos", uploadBody).bearerAuth(jwt));
    }

    @Test
    void getPhoto_returnsPhotoWithBlobUrlAndStatus() throws Exception {
        String json = client.toBlocking().retrieve(
            HttpRequest.GET("/api/v1/players/" + playerId + "/missions/1/photo").bearerAuth(jwt)
        );

        JsonNode data = objectMapper.readTree(json).path("data");
        assertThat(data.path("photoId").asLong()).isPositive();
        assertThat(data.path("blobUrl").asText()).isEqualTo(STUB_BLOB_URL);
        assertThat(data.path("validationStatus").asText()).isEqualTo("PENDING");
    }

    @Test
    void getPhoto_notFound_returns404() {
        var ex = catchThrowableOfType(
            () -> client.toBlocking().retrieve(
                HttpRequest.GET("/api/v1/players/" + playerId + "/missions/99/photo").bearerAuth(jwt)),
            HttpClientResponseException.class
        );
        assertThat(ex.getStatus().getCode()).isEqualTo(404);
    }

    @Test
    void getPhoto_withoutToken_returns401() {
        var ex = catchThrowableOfType(
            () -> client.toBlocking().retrieve(
                HttpRequest.GET("/api/v1/players/" + playerId + "/missions/1/photo")),
            HttpClientResponseException.class
        );
        assertThat(ex.getStatus().getCode()).isEqualTo(401);
    }
}