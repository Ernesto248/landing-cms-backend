package com.jenislashes.media.service;

import com.jenislashes.common.exception.NotFoundException;
import com.jenislashes.media.dto.UpdateGalleryItemRequest;
import com.jenislashes.media.model.GalleryItemRecord;
import com.jenislashes.media.repository.GalleryItemRepository;
import com.jenislashes.servicecatalog.model.ServiceCatalogItem;
import com.jenislashes.servicecatalog.repository.ServiceCatalogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GalleryService")
class GalleryServiceTest {

    @Mock
    private GalleryItemRepository galleryItemRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private GalleryService galleryService;

    @Test
    void upload_should_store_trimmed_metadata_and_default_values() {
        when(storageService.uploadGalleryImage(multipartFile)).thenReturn(
                new StorageService.StoredFile("gallery/2026/05/file.png", "https://cdn.example.com/file.png")
        );

        var response = galleryService.upload(multipartFile, "  pestanas volumen  ", "   ", null, null, null);

        ArgumentCaptor<GalleryItemRecord> recordCaptor = ArgumentCaptor.forClass(GalleryItemRecord.class);
        verify(galleryItemRepository).insert(recordCaptor.capture());

        GalleryItemRecord savedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals("gallery/2026/05/file.png", savedRecord.fileKey()),
                () -> assertEquals("https://cdn.example.com/file.png", savedRecord.publicUrl()),
                () -> assertEquals("pestanas volumen", savedRecord.altText()),
                () -> assertNull(savedRecord.caption()),
                () -> assertEquals(0, savedRecord.sortOrder()),
                () -> assertEquals(true, savedRecord.isActive()),
                () -> assertNull(savedRecord.serviceId()),
                () -> assertNull(savedRecord.serviceName()),
                () -> assertNull(savedRecord.serviceCategory()),
                () -> assertEquals(savedRecord.id(), response.id()),
                () -> assertEquals("pestanas volumen", response.altText()),
                () -> assertNull(response.caption()),
                () -> assertEquals(0, response.sortOrder()),
                () -> assertEquals(true, response.isActive())
        );
    }

    @Test
    void upload_should_resolve_service_name_and_category_when_service_id_present() {
        UUID serviceId = UUID.randomUUID();
        when(storageService.uploadGalleryImage(multipartFile)).thenReturn(
                new StorageService.StoredFile("gallery/2026/05/file.png", "https://cdn.example.com/file.png")
        );
        when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.of(
                new ServiceCatalogItem(serviceId, "Brows", "Diseno de cejas", "diseno-cejas",
                        null, new BigDecimal("300"), 15, false, BigDecimal.ZERO,
                        true, 0, OffsetDateTime.now(), OffsetDateTime.now())
        ));

        var response = galleryService.upload(multipartFile, null, null, 2, null, serviceId);

        ArgumentCaptor<GalleryItemRecord> recordCaptor = ArgumentCaptor.forClass(GalleryItemRecord.class);
        verify(galleryItemRepository).insert(recordCaptor.capture());

        GalleryItemRecord savedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals(serviceId, savedRecord.serviceId()),
                () -> assertEquals("Diseno de cejas", savedRecord.serviceName()),
                () -> assertEquals("Brows", savedRecord.serviceCategory()),
                () -> assertEquals("Diseno de cejas", response.serviceName()),
                () -> assertEquals("Brows", response.serviceCategory()),
                () -> assertEquals(serviceId, response.serviceId())
        );
    }

    @Test
    void update_should_resolve_service_metadata_from_service_id() {
        UUID galleryItemId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-05-01T10:00:00Z");
        GalleryItemRecord existing = new GalleryItemRecord(
                galleryItemId, "gallery/2026/05/file.png", "https://cdn.example.com/file.png",
                "old alt", "old caption", 1, true, null, null, null, createdAt
        );

        when(galleryItemRepository.findById(galleryItemId)).thenReturn(Optional.of(existing));
        when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.of(
                new ServiceCatalogItem(serviceId, "Lashes", "Volumen 2D", "volumen-2d",
                        null, new BigDecimal("3000"), 150, true, new BigDecimal("500"),
                        true, 0, OffsetDateTime.now(), OffsetDateTime.now())
        ));

        var response = galleryService.update(galleryItemId,
                new UpdateGalleryItemRequest("   ", "  nueva foto  ", 4, false, serviceId));

        ArgumentCaptor<GalleryItemRecord> recordCaptor = ArgumentCaptor.forClass(GalleryItemRecord.class);
        verify(galleryItemRepository).update(recordCaptor.capture());

        GalleryItemRecord updatedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertNull(updatedRecord.altText()),
                () -> assertEquals("nueva foto", updatedRecord.caption()),
                () -> assertEquals(4, updatedRecord.sortOrder()),
                () -> assertEquals(false, updatedRecord.isActive()),
                () -> assertEquals(serviceId, updatedRecord.serviceId()),
                () -> assertEquals("Volumen 2D", updatedRecord.serviceName()),
                () -> assertEquals("Lashes", updatedRecord.serviceCategory()),
                () -> assertEquals("Volumen 2D", response.serviceName()),
                () -> assertEquals("Lashes", response.serviceCategory())
        );
    }

    @Test
    void update_should_keep_existing_file_data() {
        UUID galleryItemId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-05-01T10:00:00Z");
        GalleryItemRecord existing = new GalleryItemRecord(
                galleryItemId, "gallery/2026/05/file.png", "https://cdn.example.com/file.png",
                "old alt", "old caption", 1, true, null, null, null, createdAt
        );

        when(galleryItemRepository.findById(galleryItemId)).thenReturn(Optional.of(existing));

        var response = galleryService.update(galleryItemId,
                new UpdateGalleryItemRequest("   ", "  nueva foto  ", 4, false, null));

        ArgumentCaptor<GalleryItemRecord> recordCaptor = ArgumentCaptor.forClass(GalleryItemRecord.class);
        verify(galleryItemRepository).update(recordCaptor.capture());

        GalleryItemRecord updatedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals(existing.fileKey(), updatedRecord.fileKey()),
                () -> assertEquals(existing.publicUrl(), updatedRecord.publicUrl()),
                () -> assertEquals(createdAt, updatedRecord.createdAt()),
                () -> assertNull(response.altText()),
                () -> assertEquals("nueva foto", response.caption())
        );
    }

    @Test
    void listPublic_should_delegate_to_repository_findPublic() {
        UUID serviceId = UUID.randomUUID();
        when(galleryItemRepository.findPublic()).thenReturn(List.of(
                new GalleryItemRecord(UUID.randomUUID(), "key1", "url1", "alt", "cap",
                        0, true, serviceId, "Volumen 2D", "Lashes",
                        OffsetDateTime.parse("2026-05-01T10:00:00Z"))
        ));

        var result = galleryService.listPublic();

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("Volumen 2D", result.get(0).serviceName()),
                () -> assertEquals("Lashes", result.get(0).serviceCategory()),
                () -> assertEquals(serviceId, result.get(0).serviceId())
        );
    }

    @Test
    void delete_should_throw_when_gallery_item_does_not_exist() {
        UUID galleryItemId = UUID.randomUUID();
        when(galleryItemRepository.findById(galleryItemId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> galleryService.delete(galleryItemId));

        verify(storageService, never()).deleteObject(anyString());
        verify(galleryItemRepository, never()).delete(galleryItemId);
    }
}
