package com.kpnquest.validatephoto;

import com.kpnquest.loadmissions.Mission;
import com.kpnquest.loadmissions.MissionRepository;
import com.kpnquest.shared.ai.AiPhotoValidationService;
import com.kpnquest.shared.ai.ValidationResult;
import com.kpnquest.shared.domain.Photo;
import com.kpnquest.shared.exception.ApiException;
import io.micronaut.http.HttpStatus;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;

@Singleton
public class ValidatePhotoService {

    private final MissionKeywordsRepository missionKeywordsRepository;
    private final MissionRepository missionRepository;
    private final PhotoRepository photoRepository;
    private final AiPhotoValidationService aiValidationService;

    public ValidatePhotoService(
        MissionKeywordsRepository missionKeywordsRepository,
        MissionRepository missionRepository,
        PhotoRepository photoRepository,
        AiPhotoValidationService aiValidationService
    ) {
        this.missionKeywordsRepository = missionKeywordsRepository;
        this.missionRepository = missionRepository;
        this.photoRepository = photoRepository;
        this.aiValidationService = aiValidationService;
    }

    public ValidatePhotoResponse validate(Integer missionId, Long playerId, String photo) {

        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MISSION_NOT_FOUND", "Mission not found"));

        //Ok by default when it is spicy.
        if(mission.isSpicy())
            return ValidatePhotoResponse.okResponse();

        String keywords = missionKeywordsRepository.findById(missionId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "MISSION_NOT_FOUND", "Mission not found"))
            .validationKeywords();

        ValidationResult result = aiValidationService.validate(photo, keywords);

        String newStatus = result.valid() ? "AI_APPROVED" : "AI_REJECTED";
        photoRepository.findByPlayerIdAndMissionId(playerId, missionId)
            .ifPresent(existing -> photoRepository.update(
                new Photo(existing.id(), existing.playerId(), existing.missionId(),
                    existing.blobPath(), existing.sasToken(), existing.sasExpiresAt(),
                    newStatus, existing.createdAt(), LocalDateTime.now())
            ));

        return new ValidatePhotoResponse(result.valid(), result.reason());
    }
}