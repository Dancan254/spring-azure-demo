package com.mongs.springazuredemo.file;


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
public class FileController {

    private final AzureBlobService azureBlobService;


    /**
     * @param file the image file to ipload
     * @return Json with url of uploaded image
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam MultipartFile file) {

        try{
            String imageUrl = azureBlobService.uploadImage(file);

            Map<String, String> response  = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "Image uploaded successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e){
            Map<String, String> response  = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Retrieve an image by its file name
     * @param fileName
     * @return The image file as a response entity
     */
    @GetMapping("/{fileName}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileName) {
        try{
            byte[] image = azureBlobService.getImage(fileName);
            return ResponseEntity.ok(image);
        } catch (Exception e){
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieve an image by its URL
     * @param url The complete URL of the image
     * @return The image file as a response entity
     */
    @GetMapping("/by-url")
    public ResponseEntity<byte[]> getImageByUrl(@RequestParam String url) {
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
