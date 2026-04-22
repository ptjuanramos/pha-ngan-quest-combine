package com.kpnquest.shared.web;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Error;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Singleton;

import java.io.InputStream;

@Secured(SecurityRule.IS_ANONYMOUS)
@Singleton
public class SpaNotFoundHandler {

    @Error(status = HttpStatus.NOT_FOUND, global = true)
    public HttpResponse<?> notFound(HttpRequest<?> request) {
        String path = request.getPath();
        if (path.startsWith("/api") || path.startsWith("/swagger") || path.startsWith("/docs")) {
            return HttpResponse.notFound();
        }
        InputStream index = getClass().getClassLoader().getResourceAsStream("public/index.html");
        if (index == null) {
            return HttpResponse.notFound();
        }
        return HttpResponse.ok(index).contentType(MediaType.TEXT_HTML_TYPE);
    }
}