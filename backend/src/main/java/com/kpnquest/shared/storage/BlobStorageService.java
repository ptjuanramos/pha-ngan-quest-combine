package com.kpnquest.shared.storage;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Singleton
public class BlobStorageService {

    private final BlobContainerClient containerClient;

    public BlobStorageService(
        @Value("${azure.storage.connection-string}") String connectionString,
        @Value("${azure.storage.container-name:photos}") String containerName
    ) {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        this.containerClient = serviceClient.getBlobContainerClient(containerName);
    }

    public record SasResult(String token, LocalDateTime expiresAt) {}

    public String upload(String blobPath, byte[] bytes) {
        containerClient.getBlobClient(blobPath)
            .upload(new ByteArrayInputStream(bytes), bytes.length, true);
        return blobPath;
    }

    public void deleteAllContainersFiles() {
         containerClient.listBlobs().forEach(blob ->
                 containerClient.getBlobClient(blob.getName())
                         .delete());
    }

    public SasResult generateSas(String blobPath) {
        OffsetDateTime expiry = OffsetDateTime.now().plusDays(30);
        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(
            expiry, new BlobSasPermission().setReadPermission(true)
        );
        String token = containerClient.getBlobClient(blobPath).generateSas(values);
        return new SasResult(token, expiry.toLocalDateTime());
    }

    public String buildUrl(String blobPath, String sasToken) {
        return containerClient.getBlobClient(blobPath).getBlobUrl() + "?" + sasToken;
    }
}