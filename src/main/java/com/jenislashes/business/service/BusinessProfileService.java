package com.jenislashes.business.service;

import com.jenislashes.business.dto.BusinessProfileResponse;
import com.jenislashes.business.dto.UpdateBusinessProfileRequest;
import com.jenislashes.business.model.BusinessProfileRecord;
import com.jenislashes.business.repository.BusinessProfileRepository;
import com.jenislashes.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class BusinessProfileService {

    private final BusinessProfileRepository businessProfileRepository;

    public BusinessProfileService(BusinessProfileRepository businessProfileRepository) {
        this.businessProfileRepository = businessProfileRepository;
    }

    public BusinessProfileResponse getProfile() {
        return toResponse(requireProfile());
    }

    @Transactional
    public BusinessProfileResponse updateProfile(UpdateBusinessProfileRequest request) {
        BusinessProfileRecord existing = requireProfile();
        BusinessProfileRecord updated = new BusinessProfileRecord(
                existing.id(),
                request.brandName().trim(),
                normalizeNullable(request.tagline()),
                normalizeNullable(request.description()),
                request.phoneWhatsapp().trim(),
                normalizeNullable(request.addressLine()),
                request.city().trim(),
                request.country().trim(),
                request.currencyCode().trim().toUpperCase(),
                request.timezone().trim(),
                normalizeNullable(request.instagramUrl()),
                normalizeNullable(request.facebookUrl()),
                request.bookingEnabled(),
                request.supportsHomeService(),
                request.supportsStudioService(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        businessProfileRepository.update(updated);
        return toResponse(updated);
    }

    private BusinessProfileRecord requireProfile() {
        return businessProfileRepository.findCurrent()
                .orElseThrow(() -> new NotFoundException("Business profile not found"));
    }

    private BusinessProfileResponse toResponse(BusinessProfileRecord record) {
        return new BusinessProfileResponse(
                record.id(),
                record.brandName(),
                record.tagline(),
                record.description(),
                record.phoneWhatsapp(),
                record.addressLine(),
                record.city(),
                record.country(),
                record.currencyCode(),
                record.timezone(),
                record.instagramUrl(),
                record.facebookUrl(),
                record.bookingEnabled(),
                record.supportsHomeService(),
                record.supportsStudioService(),
                record.updatedAt()
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
