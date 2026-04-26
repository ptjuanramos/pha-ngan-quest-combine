package com.kpnquest.resetprogress;

import com.kpnquest.completemission.CompletionRepository;
import com.kpnquest.shared.exception.UnauthorizedException;
import com.kpnquest.shared.storage.BlobStorageService;
import com.kpnquest.uploadphoto.PhotoRepository;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.transaction.annotation.Transactional;
import jakarta.inject.Singleton;

@Singleton
public class ResetProgressService {

    private final CompletionRepository completionRepository;
    private final PhotoRepository photoRepository;
    private final BlobStorageService blobStorageService;

    public ResetProgressService(CompletionRepository completionRepository, PhotoRepository photoRepository, BlobStorageService blobStorageService) {
        this.completionRepository = completionRepository;
        this.photoRepository = photoRepository;
        this.blobStorageService = blobStorageService;
    }

    @Transactional
    public ResetProgressResponse reset(Authentication authentication) {
        boolean isAdmin = (boolean) authentication.getAttributes().getOrDefault("is_admin", false);
        if (!isAdmin) {
            throw UnauthorizedException.getUnauthorizedException();
        }

        long deletedPhotos = photoRepository.count();
        long deletedCompletions = completionRepository.count();

        photoRepository.deleteAll();
        completionRepository.deleteAll();
        blobStorageService.deleteAllContainersFiles();

        return new ResetProgressResponse(deletedCompletions, deletedPhotos);
    }
}