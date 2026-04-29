package com.kpnquest.listplayercompletions;

import com.kpnquest.completemission.CompletionRepository;
import com.kpnquest.completemission.MissionCompletion;
import com.kpnquest.identifyplayer.PlayerRepository;
import com.kpnquest.loadmissions.Mission;
import com.kpnquest.loadmissions.MissionRepository;
import com.kpnquest.shared.domain.Photo;
import com.kpnquest.shared.storage.BlobStorageService;
import com.kpnquest.uploadphoto.PhotoRepository;
import jakarta.inject.Singleton;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
public class ListPlayerCompletionsService {

    private final PlayerCompletionsRepository completionsRepository;
    private final PlayerRepository playerRepository;
    private final MissionRepository missionRepository;
    private final CompletionRepository completionRepository;
    private final PhotoRepository photoRepository;
    private final BlobStorageService blobStorageService;

    public ListPlayerCompletionsService(
        PlayerCompletionsRepository completionsRepository,
        PlayerRepository playerRepository,
        MissionRepository missionRepository,
        CompletionRepository completionRepository,
        PhotoRepository photoRepository,
        BlobStorageService blobStorageService
    ) {
        this.completionsRepository = completionsRepository;
        this.playerRepository = playerRepository;
        this.missionRepository = missionRepository;
        this.completionRepository = completionRepository;
        this.photoRepository = photoRepository;
        this.blobStorageService = blobStorageService;
    }

    public List<PlayerCompletionResponse> list(Long playerId) {
        return completionsRepository.findByPlayerIdOrderByCompletedAt(playerId).stream()
            .map(c -> new PlayerCompletionResponse(c.missionId(), c.completedAt()))
            .toList();
    }

    public List<PlayerMissionStatusResponse> listAllPlayersMissionStatus() {
        List<Mission> missions = missionRepository.findAllOrderById();

        Map<Long, List<MissionCompletion>> completionsByPlayer = completionRepository.findAll()
            .stream()
            .collect(Collectors.groupingBy(MissionCompletion::playerId));

        Map<Long, List<Photo>> photosByPlayer = photoRepository.findAll()
            .stream()
            .collect(Collectors.groupingBy(Photo::playerId));

        return playerRepository.findNonAdminPlayers()
            .stream()
            .map(player -> {
                Map<Integer, MissionCompletion> completionsByMission = completionsByPlayer
                    .getOrDefault(player.id(), List.of())
                    .stream()
                    .collect(Collectors.toMap(MissionCompletion::missionId, c -> c));

                Map<Integer, Photo> photosByMission = photosByPlayer
                    .getOrDefault(player.id(), List.of())
                    .stream()
                    .collect(Collectors.toMap(Photo::missionId, p -> p));

                List<MissionStatusResponse> missionStatuses = missions.stream()
                    .map(mission -> buildStatus(mission, completionsByMission, photosByMission))
                    .toList();

                return new PlayerMissionStatusResponse(player.id(), player.username(), missionStatuses);
            })
            .toList();
    }

    private MissionStatusResponse buildStatus(
        Mission mission,
        Map<Integer, MissionCompletion> completions,
        Map<Integer, Photo> photos
    ) {
        MissionCompletion completion = completions.get(mission.id());
        Photo photo = photos.get(mission.id());

        String photoUrl = null;
        String validationStatus = null;
        if (photo != null) {
            photo = refreshSasIfNeeded(photo);
            photoUrl = blobStorageService.buildUrl(photo.blobPath(), photo.sasToken());
            validationStatus = photo.validationStatus();
        }

        return new MissionStatusResponse(
            mission.id(),
            mission.title(),
            completion != null,
            completion != null ? completion.completedAt() : null,
            photoUrl,
            validationStatus
        );
    }

    private Photo refreshSasIfNeeded(Photo photo) {
        LocalDateTime threshold = LocalDateTime.now().plusHours(24);
        if (photo.sasExpiresAt() == null || photo.sasExpiresAt().isBefore(threshold)) {
            BlobStorageService.SasResult sas = blobStorageService.generateSas(photo.blobPath());
            Photo updated = new Photo(
                photo.id(), photo.playerId(), photo.missionId(),
                photo.blobPath(), sas.token(), sas.expiresAt(),
                photo.validationStatus(), photo.createdAt(), LocalDateTime.now()
            );
            return photoRepository.update(updated);
        }
        return photo;
    }
}