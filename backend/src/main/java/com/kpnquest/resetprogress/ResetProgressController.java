package com.kpnquest.resetprogress;

import com.kpnquest.shared.web.ApiResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;

@Controller("/api/v1/admin")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class ResetProgressController {

    private final ResetProgressService service;

    public ResetProgressController(ResetProgressService service) {
        this.service = service;
    }

    @Post("/reset")
    public ApiResponse<ResetProgressResponse> reset(Authentication authentication) {
        return ApiResponse.ok(service.reset(authentication));
    }
}