package com.mongs.springazuredemo.file;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AzureBlobService {

    private final BlobServiceClient blobServiceClient;

    @Value("${azure.blob.container-name}")
    private String containerName;
    /**
     * upload image to Azure Blob Storage
     * @param file The image file to upload
     * @return String url of uploaded image
     */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        try{
        //generate a unique file name
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + extension;

        //get the blob client
        BlobClient client = blobServiceClient
                .getBlobContainerClient(containerName)
                .getBlobClient(uniqueFileName);
        //set the content type
        BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType(file.getContentType());

        //upload the file now


            client.upload(
                    file.getInputStream(),
                    file.getSize(),
                    true
            );
            client.setHttpHeaders(headers);
            return client.getBlobUrl();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to Azure Blob Storage:", e);
        }
    }

    /**
     *  Retrieve image from Azure Blob Storage
     * @param fileName The name of the file to retrieve
     * @return byte[] The image as a byte array
     */

    public byte[] getImage(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }

        try{
            //get the blob client
            BlobClient client = blobServiceClient
                    .getBlobContainerClient(containerName)
                    .getBlobClient(fileName);
            if (!client.exists()){
                throw new RuntimeException("File not found in Azure Blob Storage:" + fileName);
            }

            return client.downloadContent().toBytes();
        } catch (Exception e){
            throw new RuntimeException("Failed to download file from Azure Blob Storage:", e);
        }
    }

    public byte[] getImageByUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("File URL cannot be null or empty");
        }
        try {
            //check if the url is from the storage account
            String storageAccount = blobServiceClient.getAccountUrl();
            if (!url.startsWith(storageAccount)){
                throw new IllegalArgumentException("File URL is not valid");
            }
            //remove the storage account from the url
            String path = url.substring(storageAccount.length());
            if (path.startsWith("/")){
                path = path.substring(1);
            }
            String[] pathParts = path.split("/", 2);
            if (pathParts.length != 2) {
                throw new IllegalArgumentException("Invalid blob URL format");
            }

            String containerNameFromUrl = pathParts[0];
            String blobName = pathParts[1];

            // Check if the container matches our configured container
            if (!containerNameFromUrl.equals(containerName)) {
                throw new IllegalArgumentException("URL container does not match the configured container");
            }
            BlobClient client = blobServiceClient
                    .getBlobContainerClient(containerName)
                    .getBlobClient(blobName);
            if(!client.exists()) throw new RuntimeException("File not found in Azure Blob Storage:" + blobName);
            return client.downloadContent().toBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from Azure Blob Storage:", e);
        }
    }
}
