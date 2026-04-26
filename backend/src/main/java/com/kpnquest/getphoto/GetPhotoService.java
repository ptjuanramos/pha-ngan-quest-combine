package com.kpnquest.getphoto;

import com.kpnquest.shared.domain.Photo;
import com.kpnquest.shared.exception.ApiException;
import com.kpnquest.shared.storage.BlobStorageService;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;

@Singleton
public class GetPhotoService {

    private static final int SAS_REFRESH_THRESHOLD_HOURS = 24;

    private final PhotoRepository repository;
    private final BlobStorageService blobStorageService;

    public GetPhotoService(PhotoRepository repository, BlobStorageService blobStorageService) {
        this.repository = repository;
        this.blobStorageService = blobStorageService;
    }

    public PhotoResponse getPhoto(Long playerId, Integer missionId) {
        Photo photo = repository.findByPlayerIdAndMissionId(playerId, missionId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PHOTO_NOT_FOUND", "No photo found for this player and mission"));

        if (needsSasRefresh(photo)) {
            BlobStorageService.SasResult sas = blobStorageService.generateSas(photo.blobPath());
            photo = repository.update(new Photo(
                photo.id(), photo.playerId(), photo.missionId(), photo.blobPath(),
                sas.token(), sas.expiresAt(), photo.validationStatus(), photo.createdAt(), LocalDateTime.now()
            ));
        }

        return new PhotoResponse(photo.id(), blobStorageService.buildUrl(photo.blobPath(), photo.sasToken()), photo.validationStatus());
    }

    private boolean needsSasRefresh(Photo photo) {
        return photo.sasToken() == null
            || photo.sasExpiresAt() == null
            || photo.sasExpiresAt().isBefore(LocalDateTime.now().plusHours(SAS_REFRESH_THRESHOLD_HOURS));
    }
}