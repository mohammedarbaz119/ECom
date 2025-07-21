package com.test.commerce.controllers;

import com.test.commerce.services.S3UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/uploads")
@Secured("RETAILER")
public class UploadController {

    private final S3UploadService s3UploadService;

    public UploadController(S3UploadService s3UploadService) {
        this.s3UploadService = s3UploadService;
    }

    @GetMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@RequestParam String contentType) {
        String filename = "products/" +UUID.randomUUID() + "." + s3UploadService.validateAndGetExtensionFromContentType(contentType);
        URL uploadUrl = s3UploadService.generatePresignedUploadUrl(filename, contentType);
        Map<String, String> response = new HashMap<>();
        response.put("uploadUrl", uploadUrl.toString());
        response.put("fileUrl", filename);
        return ResponseEntity.ok(response);
    }

}
