package com.jenislashes.business.service;

import com.jenislashes.business.dto.CreateScheduleBlockRequest;
import com.jenislashes.business.dto.ScheduleBlockResponse;
import com.jenislashes.business.model.ScheduleBlockRecord;
import com.jenislashes.business.repository.ScheduleBlockRepository;
import com.jenislashes.common.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ScheduleBlockService {

    private final ScheduleBlockRepository scheduleBlockRepository;

    public ScheduleBlockService(ScheduleBlockRepository scheduleBlockRepository) {
        this.scheduleBlockRepository = scheduleBlockRepository;
    }

    public List<ScheduleBlockResponse> listBlocks(LocalDate from, LocalDate toInclusive) {
        return scheduleBlockRepository.findBetween(from, toInclusive).stream().map(this::toResponse).toList();
    }

    @Transactional
    public ScheduleBlockResponse createBlock(CreateScheduleBlockRequest request) {
        validateRequest(request);

        ScheduleBlockRecord record = new ScheduleBlockRecord(
                UUID.randomUUID(),
                request.blockDate(),
                request.isFullDay() ? null : request.startTime(),
                request.isFullDay() ? null : request.endTime(),
                normalizeNullable(request.reason()),
                request.isFullDay(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        scheduleBlockRepository.insert(record);
        return toResponse(record);
    }

    @Transactional
    public void deleteBlock(UUID scheduleBlockId) {
        scheduleBlockRepository.delete(scheduleBlockId);
    }

    private void validateRequest(CreateScheduleBlockRequest request) {
        if (request.isFullDay()) {
            return;
        }
        if (request.startTime() == null || request.endTime() == null) {
            throw new BadRequestException("Start and end time are required for partial schedule blocks");
        }
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BadRequestException("Start time must be before end time");
        }
    }

    private ScheduleBlockResponse toResponse(ScheduleBlockRecord record) {
        return new ScheduleBlockResponse(
                record.id(),
                record.blockDate(),
                record.startTime(),
                record.endTime(),
                record.reason(),
                record.isFullDay(),
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
