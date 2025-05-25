package com.mongs.springazuredemo.file;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Controller", description = "API endpoints for managing files in Azure Blob Storage")
public class FileController {

    private final AzureBlobService azureBlobService;

    /**
     * Upload an image to Azure Blob Storage
     * @param file The image file to upload
     * @return JSON response with the image URL
     */
    @PostMapping("/upload")
    @Operation(summary = "Upload an image", description = "Uploads an image file to Azure Blob Storage and returns the URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    public ResponseEntity<Map<String, String>> uploadImage(
            @Parameter(description = "Image file to upload (JPG, PNG, GIF, etc.)", required = true)
            @RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = azureBlobService.uploadImage(file);

            Map<String, String> response = new HashMap<>();
            response.put("url", imageUrl);
            response.put("message", "Image uploaded successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Retrieve an image by its filename
     * @param fileName The name of the file to retrieve
     * @return The image file as a response entity
     */
    @GetMapping("/{fileName}")
    @Operation(summary = "Get image by filename", description = "Retrieves an image from Azure Blob Storage by its filename")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image retrieved successfully",
                    content = @Content(mediaType = "image/*")),
            @ApiResponse(responseCode = "404", description = "Image not found",
                    content = @Content)
    })
    public ResponseEntity<byte[]> getImage(
            @Parameter(description = "Filename of the image to retrieve", required = true)
            @PathVariable String fileName) {
        try {
            byte[] imageData = azureBlobService.getImage(fileName);

            // Try to determine the media type from the filename
            String mediaType = getMediaTypeFromFileName(fileName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mediaType));

            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Retrieve an image by its URL
     * @param url The complete URL of the image
     * @return The image file as a response entity
     */
    @GetMapping("/by-url")
    @Operation(summary = "Get image by URL", description = "Retrieves an image from Azure Blob Storage by its full URL")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image retrieved successfully",
                    content = @Content(mediaType = "image/*")),
            @ApiResponse(responseCode = "404", description = "Image not found",
                    content = @Content)
    })
    public ResponseEntity<byte[]> getImageByUrl(
            @Parameter(description = "Full URL of the image to retrieve", required = true, example = "https://mongsstorage.blob.core.windows.net/images/example.jpg")
            @RequestParam String url) {
        try {
            byte[] imageData = azureBlobService.getImageByUrl(url);

            // Try to determine the media type from the URL
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            String mediaType = getMediaTypeFromFileName(fileName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(mediaType));

            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Helper method to determine media type from file name
     * @param fileName The name of the file
     * @return The media type as a string
     */
    private String getMediaTypeFromFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        return switch (extension) {
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "gif" -> "image/gif";
            case "svg" -> "image/svg+xml";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
}