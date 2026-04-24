package com.kpnquest.validatephoto;

import com.kpnquest.shared.web.ApiResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;

@Controller("/api/v1/missions")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ValidatePhotoController {

    private final ValidatePhotoService service;

    public ValidatePhotoController(ValidatePhotoService service) {
        this.service = service;
    }

    @Post("/{missionId}/photos/validate")
    public ApiResponse<ValidatePhotoResponse> validate(
        @PathVariable Integer missionId,
        @Valid @Body ValidatePhotoRequest request
    ) {
        return ApiResponse.ok(service.validate(missionId, request.playerId(), request.photo()));
    }
}