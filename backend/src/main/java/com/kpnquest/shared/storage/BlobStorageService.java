package com.kpnquest.shared.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.PublicAccessType;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.io.ByteArrayInputStream;

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

        if (!containerClient.exists()) {
            containerClient.createWithResponse(null, PublicAccessType.BLOB, null, null);
        } else {
            containerClient.setAccessPolicy(PublicAccessType.BLOB, null);
        }
    }

    public String upload(String blobPath, byte[] bytes) {
        BlobClient blobClient = containerClient.getBlobClient(blobPath);
        blobClient.upload(new ByteArrayInputStream(bytes), bytes.length, true);
        return blobClient.getBlobUrl();
    }
}