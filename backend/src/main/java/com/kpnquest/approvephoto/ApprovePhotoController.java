package com.kpnquest.approvephoto;

import com.kpnquest.shared.web.ApiResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import jakarta.validation.Valid;

@Controller("/api/v1/missions")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ApprovePhotoController {

    private final ApprovePhotoService service;

    public ApprovePhotoController(ApprovePhotoService service) {
        this.service = service;
    }

    @Post("/{missionId}/photos/{photoId}/approve")
    public ApiResponse<ApprovePhotoResponse> approve(
        @PathVariable Integer missionId,
        @PathVariable Long photoId,
        @Valid @Body ApprovePhotoRequest request,
        Authentication authentication
    ) {
        return ApiResponse.ok(service.approve(photoId, request.approved(), authentication));
    }
}