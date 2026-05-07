package com.jenislashes.media.service;

import com.jenislashes.common.exception.NotFoundException;
import com.jenislashes.media.dto.GalleryItemResponse;
import com.jenislashes.media.dto.UpdateGalleryItemRequest;
import com.jenislashes.media.model.GalleryItemRecord;
import com.jenislashes.media.repository.GalleryItemRepository;
import com.jenislashes.servicecatalog.model.ServiceCatalogItem;
import com.jenislashes.servicecatalog.repository.ServiceCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class GalleryService {

    private final GalleryItemRepository galleryItemRepository;
    private final StorageService storageService;
    private final ServiceCatalogRepository serviceCatalogRepository;

    public GalleryService(
            GalleryItemRepository galleryItemRepository,
            StorageService storageService,
            ServiceCatalogRepository serviceCatalogRepository
    ) {
        this.galleryItemRepository = galleryItemRepository;
        this.storageService = storageService;
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    public List<GalleryItemResponse> listAdmin() {
        return galleryItemRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<GalleryItemResponse> listPublic() {
        return galleryItemRepository.findPublic().stream().map(this::toResponse).toList();
    }

    @Transactional
    public GalleryItemResponse upload(MultipartFile file, String altText, String caption, Integer sortOrder, Boolean isActive, UUID serviceId) {
        StorageService.StoredFile storedFile = storageService.uploadGalleryImage(file);

        String serviceName = null;
        String serviceCategory = null;
        if (serviceId != null) {
            var svc = serviceCatalogRepository.findById(serviceId);
            serviceName = svc.map(ServiceCatalogItem::name).orElse(null);
            serviceCategory = svc.map(ServiceCatalogItem::category).orElse(null);
        }

        GalleryItemRecord record = new GalleryItemRecord(
                UUID.randomUUID(),
                storedFile.fileKey(),
                storedFile.publicUrl(),
                normalizeNullable(altText),
                normalizeNullable(caption),
                sortOrder == null ? 0 : sortOrder,
                isActive == null || isActive,
                serviceId,
                serviceName,
                serviceCategory,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        galleryItemRepository.insert(record);
        return toResponse(record);
    }

    @Transactional
    public GalleryItemResponse update(UUID galleryItemId, UpdateGalleryItemRequest request) {
        GalleryItemRecord existing = galleryItemRepository.findById(galleryItemId)
                .orElseThrow(() -> new NotFoundException("Gallery item not found"));

        UUID serviceId = request.serviceId();
        String serviceName = null;
        String serviceCategory = null;
        if (serviceId != null) {
            var svc = serviceCatalogRepository.findById(serviceId);
            serviceName = svc.map(ServiceCatalogItem::name).orElse(null);
            serviceCategory = svc.map(ServiceCatalogItem::category).orElse(null);
        }

        GalleryItemRecord updated = new GalleryItemRecord(
                existing.id(),
                existing.fileKey(),
                existing.publicUrl(),
                normalizeNullable(request.altText()),
                normalizeNullable(request.caption()),
                request.sortOrder(),
                request.isActive(),
                serviceId,
                serviceName,
                serviceCategory,
                existing.createdAt()
        );

        galleryItemRepository.update(updated);
        return toResponse(updated);
    }

    @Transactional
    public void delete(UUID galleryItemId) {
        GalleryItemRecord existing = galleryItemRepository.findById(galleryItemId)
                .orElseThrow(() -> new NotFoundException("Gallery item not found"));

        storageService.deleteObject(existing.fileKey());
        galleryItemRepository.delete(existing.id());
    }

    private GalleryItemResponse toResponse(GalleryItemRecord record) {
        return new GalleryItemResponse(
                record.id(),
                record.fileKey(),
                record.publicUrl(),
                record.altText(),
                record.caption(),
                record.sortOrder(),
                record.isActive(),
                record.serviceId(),
                record.serviceName(),
                record.serviceCategory(),
                record.createdAt()
        );
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
