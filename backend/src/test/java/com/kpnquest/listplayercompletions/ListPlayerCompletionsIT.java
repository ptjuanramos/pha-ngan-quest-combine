package com.kpnquest.listplayercompletions;

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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@MicronautTest(transactional = false, environments = "test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ListPlayerCompletionsIT extends MssqlContainerExtension implements TestPropertyProvider {

    @Inject @Client("/") HttpClient client;
    @Inject ObjectMapper objectMapper;

    private long playerId;
    private String jwt;

    @Override
    public Map<String, String> getProperties() {
        return datasourceProperties();
    }

    @BeforeAll
    void authenticate() throws Exception {
        playerId = createTestPlayer("list-completions-user");
        var body = Map.of("username", "list-completions-user");
        String json = client.toBlocking().retrieve(HttpRequest.POST("/api/v1/players/identify", body));
        jwt = objectMapper.readTree(json).path("data").path("token").asText();
    }

    @Test
    void listCompletions_emptyWhenNoneCompleted() throws Exception {
        String json = client.toBlocking().retrieve(
            HttpRequest.GET("/api/v1/players/" + playerId + "/completions").bearerAuth(jwt)
        );

        JsonNode data = objectMapper.readTree(json).path("data");
        assertThat(data.isArray()).isTrue();
        assertThat(data.size()).isZero();
    }

    @Test
    void listCompletions_returnsCompletedMissions() throws Exception {
        String uniqueUsername = "list-completions-" + UUID.randomUUID();
        long freshPlayerId = createTestPlayer(uniqueUsername);
        var identifyBody = Map.of("username", uniqueUsername);
        String identifyJson = client.toBlocking().retrieve(HttpRequest.POST("/api/v1/players/identify", identifyBody));
        String freshJwt = objectMapper.readTree(identifyJson).path("data").path("token").asText();

        // Complete missions 4 and 5
        client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/4/complete", Map.of("photoId", 1)).bearerAuth(freshJwt));
        client.toBlocking().retrieve(
            HttpRequest.POST("/api/v1/missions/5/complete", Map.of("photoId", 1)).bearerAuth(freshJwt));

        String json = client.toBlocking().retrieve(
            HttpRequest.GET("/api/v1/players/" + freshPlayerId + "/completions").bearerAuth(freshJwt)
        );

        JsonNode data = objectMapper.readTree(json).path("data");
        assertThat(data.isArray()).isTrue();
        assertThat(data.size()).isEqualTo(2);
        assertThat(data.get(0).path("missionId").asInt()).isIn(4, 5);
        assertThat(data.get(0).path("completedAt").asText()).isNotBlank();
    }

    @Test
    void listCompletions_withoutToken_returns401() {
        var ex = catchThrowableOfType(
            () -> client.toBlocking().retrieve(
                HttpRequest.GET("/api/v1/players/" + playerId + "/completions")),
            HttpClientResponseException.class
        );
        assertThat(ex.getStatus().getCode()).isEqualTo(401);
    }
}