package com.kpnquest.uploadphoto;

import com.kpnquest.shared.domain.Photo;
import com.kpnquest.shared.storage.BlobStorageService;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Singleton
public class UploadPhotoService {

    private final PhotoRepository photoRepository;
    private final BlobStorageService blobStorageService;

    public UploadPhotoService(PhotoRepository photoRepository, BlobStorageService blobStorageService) {
        this.photoRepository = photoRepository;
        this.blobStorageService = blobStorageService;
    }

    @Transactional
    public UploadPhotoResponse upload(Long playerId, Integer missionId, String base64Content) {
        byte[] bytes = decodeBase64(base64Content);
        String blobPath = playerId + "/" + missionId + "/" + UUID.randomUUID() + ".jpg";
        String blobUrl = blobStorageService.upload(blobPath, bytes);

        LocalDateTime now = LocalDateTime.now();
        Photo photo = photoRepository.findByPlayerIdAndMissionId(playerId, missionId)
            .map(existing -> photoRepository.update(
                new Photo(existing.id(), existing.playerId(), existing.missionId(), blobUrl, existing.validationStatus(), existing.createdAt(), now)
            ))
            .orElseGet(() -> photoRepository.save(
                new Photo(null, playerId, missionId, blobUrl, "PENDING", now, now)
            ));

        return new UploadPhotoResponse(photo.id(), missionId, photo.blobUrl(), photo.validationStatus());
    }

    private byte[] decodeBase64(String base64Content) {
        String data = base64Content.contains(",") ? base64Content.split(",")[1] : base64Content;
        return Base64.getDecoder().decode(data);
    }
}