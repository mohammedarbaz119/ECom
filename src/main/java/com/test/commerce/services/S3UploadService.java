package com.test.commerce.services;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.HashMap;

@Service
public class S3UploadService {

    private static final Logger logger = LoggerFactory.getLogger(S3UploadService.class);

    private S3Presigner presigner;
    private S3Client s3Client;

    @Value("${amazon.s3uploadbucket}")
    private String bucketName;

    @Value("${amazon.region}")
    private String region;

    // Additional bucket for reports (can be same as upload bucket)
    @Value("${amazon.s3reportsbucket:${amazon.s3uploadbucket}}")
    private String reportsBucket;

    @PostConstruct
    public void setS3UploadService() {
        Region awsRegion = Region.of(region);
        DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

        this.presigner = S3Presigner.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();

        this.s3Client = S3Client.builder()
                .region(awsRegion)
                .credentialsProvider(credentialsProvider)
                .build();
    }


    public URL generatePresignedUploadUrl(String objectKey, String contentType) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(objectRequest)
                .build();

        return presigner.presignPutObject(presignRequest).url();
    }

    public String getObjectUrl(String objectKey) {
        GetObjectRequest obj = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .getObjectRequest(obj)
                .signatureDuration(Duration.ofHours(5))
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    public String validateAndGetExtensionFromContentType(String contentType) {
        if (contentType == null || contentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }

        String normalized = contentType.trim().toLowerCase();

        return switch (normalized) {
            case "image/jpeg", "image/jpg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            case "image/webp" -> "webp";
            case "image/bmp" -> "bmp";
            case "image/tiff" -> "tiff";
            case "image/svg+xml" -> "svg";
            default -> throw new IllegalArgumentException("Unsupported or invalid image content type: " + contentType);
        };
    }


    /**
     * Upload a report file directly to S3 (used by report generation service)
     */
    public void uploadReport(String key, byte[] content, String contentType) {
        try {
            logger.info("Uploading report to S3: {}", key);

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(reportsBucket)
                    .key(key)
                    .contentType(contentType)
                    .contentLength((long) content.length)
                    .metadata(createReportMetadata())
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(content));

            logger.info("Successfully uploaded report: {}", key);

        } catch (Exception e) {
            logger.error("Failed to upload report to S3: {}", key, e);
            throw new RuntimeException("Failed to upload report to S3", e);
        }
    }

    /**
     * Generate presigned download URL for reports with retailer access control
     */
    public String generateReportDownloadUrl(String key, Long retailerId, Duration expiration) {
        try {

            if (!isRetailerAuthorizedForReport(key, retailerId)) {
                throw new SecurityException("Unauthorized access to report: " + key);
            }

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(reportsBucket)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(expiration)
                    .getObjectRequest(getRequest)
                    .build();

            String url = presigner.presignGetObject(presignRequest).url().toString();

            logger.info("Generated report download URL for retailer {}: {}", retailerId, key);
            return url;

        } catch (SecurityException e) {
            logger.warn("Security violation: Retailer {} attempted to access unauthorized report: {}", retailerId, key);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to generate presigned URL for report: {}", key, e);
            throw new RuntimeException("Failed to generate report download URL", e);
        }
    }

    /**
     * Get content type and extension for report files
     */
    public String getReportContentType(String reportType) {
        return switch (reportType.toUpperCase()) {
            case "CSV" -> "text/csv";
            case "PDF" -> "application/pdf";
            default -> "application/octet-stream";
        };
    }

    public String getReportFileExtension(String reportType) {
        return switch (reportType.toUpperCase()) {
            case "CSV" -> "csv";
            case "PDF" -> "pdf";
            default -> "bin";
        };
    }

    /**
     * Generate secure report file key with retailer isolation
     */
    public String generateReportKey(Long retailerId, String reportType, String startDate, String endDate, String suffix) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = String.format("sales_report_%s_%s_to_%s_%s.%s",
                reportType.toLowerCase(),
                startDate,
                endDate,
                timestamp,
                getReportFileExtension(reportType)
        );

        if (suffix != null && !suffix.trim().isEmpty()) {
            fileName = suffix + "_" + fileName;
        }

        return String.format("reports/retailer_%d/%s", retailerId, fileName);
    }

    /**
     * Delete a report file (for cleanup or user deletion requests)
     */
    public boolean deleteReport(String key, Long retailerId) {
        try {
            // Security check
            if (!isRetailerAuthorizedForReport(key, retailerId)) {
                throw new SecurityException("Unauthorized deletion attempt for report: " + key);
            }

            software.amazon.awssdk.services.s3.model.DeleteObjectRequest deleteRequest =
                    software.amazon.awssdk.services.s3.model.DeleteObjectRequest.builder()
                            .bucket(reportsBucket)
                            .key(key)
                            .build();

            s3Client.deleteObject(deleteRequest);

            logger.info("Successfully deleted report: {}", key);
            return true;

        } catch (Exception e) {
            logger.error("Failed to delete report: {}", key, e);
            return false;
        }
    }

    /**
     * Check if object exists in S3
     */
    public boolean reportExists(String key) {
        try {
            software.amazon.awssdk.services.s3.model.HeadObjectRequest headRequest =
                    software.amazon.awssdk.services.s3.model.HeadObjectRequest.builder()
                            .bucket(reportsBucket)
                            .key(key)
                            .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (software.amazon.awssdk.services.s3.model.NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            logger.error("Error checking if report exists: {}", key, e);
            return false;
        }
    }



    private Map<String, String> createReportMetadata() {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("generated-by", "Ecom Store");
        metadata.put("type", "sales-report");
        metadata.put("generated-at", String.valueOf(System.currentTimeMillis()));
        return metadata;
    }

    private boolean isRetailerAuthorizedForReport(String key, Long retailerId) {
        // Check if the key follows the expected pattern: reports/retailer_{id}/filename
        String expectedPrefix = "reports/retailer_" + retailerId + "/";
        return key.startsWith(expectedPrefix);
    }

    private String ensureRetailerIsolation(String objectKey, Long retailerId) {
        String expectedPrefix = "reports/retailer_" + retailerId + "/";

        if (objectKey.startsWith(expectedPrefix)) {
            return objectKey;
        }

        String fileName = objectKey.replaceFirst("^reports/retailer_\\d+/", "");
        return expectedPrefix + fileName;
    }


}