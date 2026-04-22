package com.kpnquest.getphoto;

import com.kpnquest.shared.web.ApiResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller("/api/v1/players")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class GetPhotoController {

    private final GetPhotoService service;

    public GetPhotoController(GetPhotoService service) {
        this.service = service;
    }

    @Get("/{playerId}/missions/{missionId}/photo")
    public ApiResponse<PhotoResponse> getPhoto(@PathVariable Long playerId, @PathVariable Integer missionId) {
        return ApiResponse.ok(service.getPhoto(playerId, missionId));
    }
}