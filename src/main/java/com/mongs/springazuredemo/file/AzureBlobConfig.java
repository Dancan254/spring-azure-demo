package com.mongs.springazuredemo.file;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobConfig {
    @Bean
    public BlobServiceClient blobServiceClient(
            @Value("${spring.cloud.azure.storage.blob.account-name}") String accountName,
            @Value("${spring.cloud.azure.storage.blob.account-key}") String accountKey) {
        String endpoint = String.format("https://%s.blob.core.windows.net", accountName);
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);
        return new BlobServiceClientBuilder()
                .endpoint(endpoint)
                .credential(credential)
                .buildClient();
    }
}