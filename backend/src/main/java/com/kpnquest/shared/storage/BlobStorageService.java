package com.kpnquest.shared.storage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobCorsRule;
import com.azure.storage.blob.models.BlobServiceProperties;
import com.azure.storage.blob.models.PublicAccessType;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;

import java.io.ByteArrayInputStream;
import java.util.List;

@Singleton
public class BlobStorageService {

    private final BlobContainerClient containerClient;

    public BlobStorageService(
        @Value("${azure.storage.connection-string}") String connectionString,
        @Value("${azure.storage.container-name:photos}") String containerName,
        @Value("${azure.storage.cors-origin:*}") String corsOrigin
    ) {
        BlobServiceClient serviceClient = new BlobServiceClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        configureCors(serviceClient, corsOrigin);

        this.containerClient = serviceClient.getBlobContainerClient(containerName);

        if (!containerClient.exists()) {
            containerClient.createWithResponse(null, PublicAccessType.BLOB, null, null);
        } else {
            containerClient.setAccessPolicy(PublicAccessType.BLOB, null);
        }
    }

    private void configureCors(BlobServiceClient serviceClient, String corsOrigin) {
        BlobCorsRule corsRule = new BlobCorsRule()
            .setAllowedOrigins(corsOrigin)
            .setAllowedMethods("GET,HEAD,OPTIONS")
            .setAllowedHeaders("*")
            .setExposedHeaders("Content-Type,Content-Length")
            .setMaxAgeInSeconds(3600);

        BlobServiceProperties properties = serviceClient.getProperties();
        properties.setCors(List.of(corsRule));
        serviceClient.setProperties(properties);
    }

    /**
     * Upload raw bytes to the given blob path and return the public URL.
     * Callers are responsible for constructing the path and providing the bytes.
     */
    public String upload(String blobPath, byte[] bytes) {
        BlobClient blobClient = containerClient.getBlobClient(blobPath);
        blobClient.upload(new ByteArrayInputStream(bytes), bytes.length, true);
        return blobClient.getBlobUrl();
    }

    public void deleteAll(String container) {
//        BlobClient blobClient = containerClient.getBlobClient()
//        for(String )
//        blobClient.deleteIfExists();
    }
}
