package com.jenislashes.business.service;

import com.jenislashes.business.dto.BusinessHourResponse;
import com.jenislashes.business.dto.UpsertBusinessHourRequest;
import com.jenislashes.business.model.BusinessHourRecord;
import com.jenislashes.business.repository.BusinessHoursRepository;
import com.jenislashes.common.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BusinessHoursService {

    private final BusinessHoursRepository businessHoursRepository;

    public BusinessHoursService(BusinessHoursRepository businessHoursRepository) {
        this.businessHoursRepository = businessHoursRepository;
    }

    public List<BusinessHourResponse> listHours() {
        return businessHoursRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<BusinessHourResponse> upsertHours(List<UpsertBusinessHourRequest> requests) {
        for (UpsertBusinessHourRequest request : requests) {
            validateHourRequest(request);
            BusinessHourRecord existing = businessHoursRepository.findByDayOfWeek(request.dayOfWeek()).orElse(null);
            if (existing == null) {
                businessHoursRepository.insert(new BusinessHourRecord(
                        UUID.randomUUID(),
                        request.dayOfWeek(),
                        request.isClosed() ? null : request.openTime(),
                        request.isClosed() ? null : request.closeTime(),
                        request.isClosed()
                ));
            } else {
                businessHoursRepository.update(new BusinessHourRecord(
                        existing.id(),
                        existing.dayOfWeek(),
                        request.isClosed() ? null : request.openTime(),
                        request.isClosed() ? null : request.closeTime(),
                        request.isClosed()
                ));
            }
        }

        return listHours();
    }

    private void validateHourRequest(UpsertBusinessHourRequest request) {
        if (request.isClosed()) {
            return;
        }
        if (request.openTime() == null || request.closeTime() == null) {
            throw new BadRequestException("Open and close time are required when the day is open");
        }
        if (!request.openTime().isBefore(request.closeTime())) {
            throw new BadRequestException("Open time must be before close time");
        }
    }

    private BusinessHourResponse toResponse(BusinessHourRecord record) {
        return new BusinessHourResponse(
                record.id(),
                record.dayOfWeek(),
                record.openTime(),
                record.closeTime(),
                record.isClosed()
        );
    }
}
