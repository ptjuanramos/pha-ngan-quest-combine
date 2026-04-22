package com.kpnquest.listplayercompletions;

import com.kpnquest.shared.web.ApiResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.util.List;

@Controller("/api/v1/players")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ListPlayerCompletionsController {

    private final ListPlayerCompletionsService service;

    public ListPlayerCompletionsController(ListPlayerCompletionsService service) {
        this.service = service;
    }

    @Get("/{playerId}/completions")
    public ApiResponse<List<PlayerCompletionResponse>> list(@PathVariable Long playerId) {
        return ApiResponse.ok(service.list(playerId));
    }
}