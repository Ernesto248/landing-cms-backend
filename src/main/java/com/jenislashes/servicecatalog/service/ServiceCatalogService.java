package com.jenislashes.servicecatalog.service;

import com.jenislashes.common.exception.BadRequestException;
import com.jenislashes.common.exception.NotFoundException;
import com.jenislashes.common.util.SlugUtils;
import com.jenislashes.servicecatalog.dto.ServiceResponse;
import com.jenislashes.servicecatalog.dto.UpsertServiceRequest;
import com.jenislashes.servicecatalog.model.ServiceCatalogItem;
import com.jenislashes.servicecatalog.repository.ServiceCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ServiceCatalogService {

    private final ServiceCatalogRepository serviceCatalogRepository;

    public ServiceCatalogService(ServiceCatalogRepository serviceCatalogRepository) {
        this.serviceCatalogRepository = serviceCatalogRepository;
    }

    public List<ServiceResponse> getPublicServices() {
        return serviceCatalogRepository.findPublicActive().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ServiceResponse> getAdminServices() {
        return serviceCatalogRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ServiceResponse createService(UpsertServiceRequest request) {
        validateTouchUp(request);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        String slug = uniqueSlug(request.name(), null);
        ServiceCatalogItem item = new ServiceCatalogItem(
                UUID.randomUUID(),
                request.category().trim().toUpperCase(Locale.ROOT),
                request.name().trim(),
                slug,
                normalizeNullable(request.description()),
                request.basePrice(),
                request.durationMinutes(),
                request.supportsTouchUp(),
                request.touchUpDiscount(),
                request.isActive(),
                request.sortOrder(),
                now,
                now
        );
        serviceCatalogRepository.insert(item);
        return toResponse(item);
    }

    @Transactional
    public ServiceResponse updateService(UUID serviceId, UpsertServiceRequest request) {
        validateTouchUp(request);

        ServiceCatalogItem existing = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        ServiceCatalogItem updated = new ServiceCatalogItem(
                existing.id(),
                request.category().trim().toUpperCase(Locale.ROOT),
                request.name().trim(),
                uniqueSlug(request.name(), existing.id()),
                normalizeNullable(request.description()),
                request.basePrice(),
                request.durationMinutes(),
                request.supportsTouchUp(),
                request.touchUpDiscount(),
                request.isActive(),
                request.sortOrder(),
                existing.createdAt(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );

        serviceCatalogRepository.update(updated);
        return toResponse(updated);
    }

    @Transactional
    public void archiveService(UUID serviceId) {
        ServiceCatalogItem existing = serviceCatalogRepository.findById(serviceId)
                .orElseThrow(() -> new NotFoundException("Service not found"));

        if (!existing.isActive()) {
            return;
        }

        serviceCatalogRepository.updateActive(serviceId, false, OffsetDateTime.now(ZoneOffset.UTC));
    }

    private void validateTouchUp(UpsertServiceRequest request) {
        if (!request.supportsTouchUp() && request.touchUpDiscount().signum() > 0) {
            throw new BadRequestException("Touch-up discount requires supportsTouchUp=true");
        }
    }

    private String uniqueSlug(String name, UUID currentId) {
        String baseSlug = SlugUtils.slugify(name);
        String candidate = baseSlug;
        int suffix = 2;

        while (true) {
            ServiceCatalogItem existing = serviceCatalogRepository.findBySlug(candidate).orElse(null);
            if (existing == null || existing.id().equals(currentId)) {
                return candidate;
            }

            candidate = baseSlug + "-" + suffix;
            suffix++;
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ServiceResponse toResponse(ServiceCatalogItem item) {
        return new ServiceResponse(
                item.id(),
                item.category(),
                item.name(),
                item.slug(),
                item.description(),
                item.basePrice(),
                item.durationMinutes(),
                item.supportsTouchUp(),
                item.touchUpDiscount(),
                item.isActive(),
                item.sortOrder()
        );
    }
}
