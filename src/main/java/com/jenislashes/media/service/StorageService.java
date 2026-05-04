package com.jenislashes.media.service;

import com.jenislashes.config.StorageProperties;
import com.jenislashes.common.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;
    private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

    private final StorageProperties storageProperties;

    public StorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public StoredFile uploadGalleryImage(MultipartFile multipartFile) {
        return uploadImage(multipartFile, "gallery");
    }

    public StoredFile uploadHeroBackgroundImage(MultipartFile multipartFile) {
        return uploadImage(multipartFile, "branding");
    }

    private StoredFile uploadImage(MultipartFile multipartFile, String rootFolder) {
        validateStorageConfigured();
        validateFile(multipartFile);

        String extension = resolveExtension(multipartFile.getOriginalFilename(), multipartFile.getContentType());
        YearMonth yearMonth = YearMonth.now();
        String fileKey = rootFolder + "/" + yearMonth.getYear() + "/" + String.format("%02d", yearMonth.getMonthValue()) + "/" + UUID.randomUUID() + extension;

        try (S3Client s3Client = buildClient()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(storageProperties.getBucket())
                            .key(fileKey)
                            .contentType(multipartFile.getContentType())
                            .acl("public-read")
                            .build(),
                    RequestBody.fromBytes(multipartFile.getBytes())
            );
        } catch (IOException exception) {
            throw new BadRequestException("Failed to read uploaded file");
        } catch (IllegalArgumentException | SdkException exception) {
            logger.error("Failed to upload gallery image to storage", exception);
            throw new BadRequestException("No se pudo subir la imagen al almacenamiento");
        }

        return new StoredFile(fileKey, buildPublicUrl(fileKey));
    }

    public void deleteObject(String fileKey) {
        validateStorageConfigured();

        try (S3Client s3Client = buildClient()) {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(storageProperties.getBucket())
                    .key(fileKey)
                    .build());
        } catch (IllegalArgumentException | SdkException exception) {
            logger.error("Failed to delete storage object {}", fileKey, exception);
            throw new BadRequestException("No se pudo eliminar la imagen del almacenamiento");
        }
    }

    private S3Client buildClient() {
        return S3Client.builder()
                .endpointOverride(URI.create(storageProperties.getEndpoint()))
                .region(Region.of(storageProperties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(storageProperties.getAccessKey(), storageProperties.getSecretKey())
                ))
                .forcePathStyle(false)
                .build();
    }

    private void validateStorageConfigured() {
        if (!StringUtils.hasText(storageProperties.getEndpoint())
                || !StringUtils.hasText(storageProperties.getRegion())
                || !StringUtils.hasText(storageProperties.getBucket())
                || !StringUtils.hasText(storageProperties.getAccessKey())
                || !StringUtils.hasText(storageProperties.getSecretKey())) {
            throw new BadRequestException("DigitalOcean Spaces is not configured");
        }
    }

    private void validateFile(MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new BadRequestException("An image file is required");
        }
        if (multipartFile.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BadRequestException("Image file exceeds the 10MB size limit");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(multipartFile.getContentType())) {
            throw new BadRequestException("Only JPEG, PNG, and WEBP images are supported");
        }
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    private String buildPublicUrl(String fileKey) {
        URI endpoint = URI.create(storageProperties.getEndpoint());
        String host = endpoint.getHost();
        return endpoint.getScheme() + "://" + storageProperties.getBucket() + "." + host + "/" + fileKey;
    }

    public record StoredFile(String fileKey, String publicUrl) {
    }
}
