package com.kpnquest.approvephoto;

import com.kpnquest.shared.domain.Photo;
import com.kpnquest.shared.exception.ApiException;
import com.kpnquest.shared.exception.UnauthorizedException;
import io.micronaut.http.HttpStatus;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;

@Singleton
public class ApprovePhotoService {

    private final PhotoRepository repository;

    public ApprovePhotoService(PhotoRepository repository) {
        this.repository = repository;
    }

    public ApprovePhotoResponse approve(Long photoId, boolean approved, Authentication authentication) {
        boolean isAdmin = (boolean) authentication.getAttributes().getOrDefault("is_admin", false);
        if (!isAdmin) {
            throw UnauthorizedException.getUnauthorizedException();
        }

        Photo photo = repository.findById(photoId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PHOTO_NOT_FOUND", "Photo not found"));

        String newStatus = approved ? "ADMIN_APPROVED" : "ADMIN_REJECTED";
        Photo updated = repository.update(
            new Photo(photo.id(), photo.playerId(), photo.missionId(),
                photo.blobPath(), photo.sasToken(), photo.sasExpiresAt(),
                newStatus, photo.createdAt(), LocalDateTime.now())
        );

        return new ApprovePhotoResponse(updated.id(), updated.validationStatus());
    }
}