package com.mongs.springazuredemo.file;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobConfig {

    @Value("${spring.cloud.azure.storage.blob.account-name}")
    private String accountName;

    @Value("${spring.cloud.azure.storage.blob.account-key}")
    private String accountKey;

    @Value("${spring.cloud.azure.storage.blob.container-name}")
    private String containerName;


    @Bean
    public BlobServiceClient blobServiceClient() {
        String endpoint = String.format("https://%s.blob.core.windows.net", accountName);
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();
    }
    
    @Bean
    public BlobContainerClient blobContainerClient(BlobServiceClient blobServiceClient) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        // Create the container if it doesn't exist
        if (!containerClient.exists()) {
            containerClient.create();
        }
        return containerClient;
    }
}