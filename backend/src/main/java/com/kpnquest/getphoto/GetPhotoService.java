package com.kpnquest.getphoto;

import com.kpnquest.shared.domain.Photo;
import com.kpnquest.shared.exception.ApiException;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;

@Singleton
public class GetPhotoService {

    private final PhotoRepository repository;

    public GetPhotoService(PhotoRepository repository) {
        this.repository = repository;
    }

    public PhotoResponse getPhoto(Long playerId, Integer missionId) {
        Photo photo = repository.findByPlayerIdAndMissionId(playerId, missionId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PHOTO_NOT_FOUND", "No photo found for this player and mission"));
        return new PhotoResponse(photo.id(), photo.blobUrl(), photo.validationStatus());
    }
}