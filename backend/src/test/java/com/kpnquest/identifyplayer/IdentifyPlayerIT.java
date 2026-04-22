package com.kpnquest.identifyplayer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpnquest.shared.MssqlContainerExtension;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@MicronautTest(transactional = false, environments = "test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IdentifyPlayerIT extends MssqlContainerExtension implements TestPropertyProvider {

    @Inject @Client("/") HttpClient client;
    @Inject ObjectMapper objectMapper;

    @Override
    public Map<String, String> getProperties() {
        return datasourceProperties();
    }

    @Test
    void knownUsername_returnsPlayerIdAndToken() throws Exception {
        var body = Map.of("username", "elchico");

        var response = client.toBlocking().exchange(
            HttpRequest.POST("/api/v1/players/identify", body),
            String.class
        );

        JsonNode data = objectMapper.readTree(response.body()).path("data");
        assertThat(data.path("playerId").asLong()).isPositive();
        assertThat(data.path("token").asText()).isNotBlank();
    }

    @Test
    void sameUsername_returnsSamePlayerId() throws Exception {
        var body = Map.of("username", "coelhinha");

        String json1 = client.toBlocking().retrieve(HttpRequest.POST("/api/v1/players/identify", body));
        String json2 = client.toBlocking().retrieve(HttpRequest.POST("/api/v1/players/identify", body));

        long id1 = objectMapper.readTree(json1).path("data").path("playerId").asLong();
        long id2 = objectMapper.readTree(json2).path("data").path("playerId").asLong();

        assertThat(id1).isEqualTo(id2);
    }

    @Test
    void unknownUsername_returns401() {
        var body = Map.of("username", "nobody");

        var ex = catchThrowableOfType(
            () -> client.toBlocking().retrieve(HttpRequest.POST("/api/v1/players/identify", body)),
            HttpClientResponseException.class
        );

        assertThat(ex.getStatus().getCode()).isEqualTo(401);
    }

    @Test
    void adminUser_tokenContainsIsAdminClaim() throws Exception {
        var body = Map.of("username", "godmod");

        String json = client.toBlocking().retrieve(HttpRequest.POST("/api/v1/players/identify", body));
        String token = objectMapper.readTree(json).path("data").path("token").asText();

        // Decode JWT payload (middle part, base64url)
        String[] parts = token.split("\\.");
        String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
        JsonNode claims = objectMapper.readTree(payload);

        assertThat(claims.path("is_admin").asBoolean()).isTrue();
    }
}