package com.jenislashes.media.service;

import com.jenislashes.common.exception.BadRequestException;
import com.jenislashes.config.StorageProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("StorageService")
class StorageServiceTest {

    @Test
    void uploadGalleryImage_should_fail_when_storage_is_not_configured() {
        StorageProperties storageProperties = new StorageProperties();
        StorageService storageService = new StorageService(storageProperties);

        assertThrows(BadRequestException.class, () -> storageService.uploadGalleryImage(
                new MockMultipartFile("file", "test.png", "image/png", new byte[]{1, 2, 3})
        ));
    }

    @Test
    void uploadHeroBackgroundImage_should_fail_when_storage_is_not_configured() {
        StorageProperties storageProperties = new StorageProperties();
        StorageService storageService = new StorageService(storageProperties);

        assertThrows(BadRequestException.class, () -> storageService.uploadHeroBackgroundImage(
                new MockMultipartFile("file", "hero.png", "image/png", new byte[]{1, 2, 3})
        ));
    }

    @Test
    void uploadGalleryImage_should_reject_unsupported_content_type() {
        StorageProperties storageProperties = configuredStorageProperties();
        StorageService storageService = new StorageService(storageProperties);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> storageService.uploadGalleryImage(
                new MockMultipartFile("file", "test.gif", "image/gif", new byte[]{1, 2, 3})
        ));

        assertEquals("Only JPEG, PNG, and WEBP images are supported", exception.getMessage());
    }

    @Test
    void uploadGalleryImage_should_reject_files_over_ten_megabytes() {
        StorageProperties storageProperties = configuredStorageProperties();
        StorageService storageService = new StorageService(storageProperties);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> storageService.uploadGalleryImage(
                new MockMultipartFile("file", "test.png", "image/png", new byte[10 * 1024 * 1024 + 1])
        ));

        assertEquals("Image file exceeds the 10MB size limit", exception.getMessage());
    }

    private StorageProperties configuredStorageProperties() {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setEndpoint("https://nyc3.digitaloceanspaces.com");
        storageProperties.setRegion("nyc3");
        storageProperties.setBucket("landing-cms");
        storageProperties.setAccessKey("key");
        storageProperties.setSecretKey("secret");
        return storageProperties;
    }
}
