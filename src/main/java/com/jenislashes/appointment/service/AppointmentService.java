package com.jenislashes.appointment.service;

import com.jenislashes.appointment.dto.AppointmentItemRequest;
import com.jenislashes.appointment.dto.AppointmentItemResponse;
import com.jenislashes.appointment.dto.AppointmentResponse;
import com.jenislashes.appointment.dto.CreateAppointmentRequest;
import com.jenislashes.appointment.dto.UpdateAppointmentStatusRequest;
import com.jenislashes.appointment.model.AppointmentItemRecord;
import com.jenislashes.appointment.model.AppointmentRecord;
import com.jenislashes.appointment.repository.AppointmentRepository;
import com.jenislashes.business.model.BusinessHourRecord;
import com.jenislashes.business.model.ScheduleBlockRecord;
import com.jenislashes.business.repository.BusinessHoursRepository;
import com.jenislashes.business.repository.ScheduleBlockRepository;
import com.jenislashes.client.model.ClientRecord;
import com.jenislashes.client.service.ClientService;
import com.jenislashes.common.exception.BadRequestException;
import com.jenislashes.common.exception.ConflictException;
import com.jenislashes.common.exception.NotFoundException;
import com.jenislashes.servicecatalog.model.ServiceCatalogItem;
import com.jenislashes.servicecatalog.repository.ServiceCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AppointmentService {

    private static final List<String> ACTIVE_STATUSES = List.of("CONFIRMED", "COMPLETED");
    private static final List<String> ALL_STATUSES = List.of("CONFIRMED", "COMPLETED", "CANCELLED");
    private static final List<String> ALL_MODES = List.of("STUDIO", "HOME");
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Havana");

    private final AppointmentRepository appointmentRepository;
    private final ClientService clientService;
    private final ServiceCatalogRepository serviceCatalogRepository;
    private final BusinessHoursRepository businessHoursRepository;
    private final ScheduleBlockRepository scheduleBlockRepository;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            ClientService clientService,
            ServiceCatalogRepository serviceCatalogRepository,
            BusinessHoursRepository businessHoursRepository,
            ScheduleBlockRepository scheduleBlockRepository
    ) {
        this.appointmentRepository = appointmentRepository;
        this.clientService = clientService;
        this.serviceCatalogRepository = serviceCatalogRepository;
        this.businessHoursRepository = businessHoursRepository;
        this.scheduleBlockRepository = scheduleBlockRepository;
    }

    public List<AppointmentResponse> listAppointments(OffsetDateTime from, OffsetDateTime to) {
        List<AppointmentRecord> appointments = appointmentRepository.findBetween(from, to);
        Map<UUID, List<AppointmentItemRecord>> itemsByAppointmentId = groupItems(appointments);

        return appointments.stream()
                .map(appointment -> toResponse(appointment, itemsByAppointmentId.getOrDefault(appointment.id(), List.of())))
                .toList();
    }

    public AppointmentResponse getAppointment(UUID appointmentId) {
        AppointmentRecord appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        return toResponse(appointment, appointmentRepository.findItemsByAppointmentId(appointmentId));
    }

    @Transactional
    public AppointmentResponse updateAppointment(UUID appointmentId, CreateAppointmentRequest request) {
        AppointmentRecord existing = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        if (request.items() == null || request.items().isEmpty()) {
            throw new BadRequestException("At least one service is required");
        }

        String mode = normalizeMode(request.mode());
        ClientRecord client = clientService.requireClient(request.clientId());
        List<AppointmentItemRecord> appointmentItems = buildAppointmentItems(request.items());

        int totalDurationMinutes = appointmentItems.stream().mapToInt(AppointmentItemRecord::durationSnapshotMinutes).sum();
        OffsetDateTime scheduledEnd = request.scheduledStart().plusMinutes(totalDurationMinutes);
        if (ACTIVE_STATUSES.contains(existing.status())) {
            validateScheduleAvailability(request.scheduledStart(), scheduledEnd);
            validateOverlap(request.scheduledStart(), scheduledEnd, existing.id());
        }

        BigDecimal subtotal = appointmentItems.stream()
                .map(AppointmentItemRecord::finalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal travelFee = request.travelFee();
        BigDecimal total = subtotal.add(travelFee);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        AppointmentRecord updatedAppointment = new AppointmentRecord(
                existing.id(),
                client.id(),
                client.fullName(),
                existing.status(),
                mode,
                request.scheduledStart(),
                scheduledEnd,
                normalizeNullable(request.addressSnapshot()),
                normalizeNullable(request.notes()),
                subtotal,
                travelFee,
                total,
                "COMPLETED".equals(existing.status()) ? existing.completedAt() : null,
                "CANCELLED".equals(existing.status()) ? existing.cancelledAt() : null,
                "CANCELLED".equals(existing.status()) ? existing.cancelReason() : null,
                existing.createdAt(),
                now
        );

        List<AppointmentItemRecord> persistedItems = appointmentItems.stream()
                .map(item -> new AppointmentItemRecord(
                        UUID.randomUUID(),
                        existing.id(),
                        item.serviceId(),
                        item.serviceNameSnapshot(),
                        item.unitPriceSnapshot(),
                        item.durationSnapshotMinutes(),
                        item.isTouchUp(),
                        item.discountAmount(),
                        item.finalPrice()
                ))
                .toList();

        appointmentRepository.updateAppointment(updatedAppointment);
        appointmentRepository.deleteItemsByAppointmentId(existing.id());
        appointmentRepository.insertItems(persistedItems);
        clientService.refreshAppointmentStats(existing.clientId());
        if (!existing.clientId().equals(client.id())) {
            clientService.refreshAppointmentStats(client.id());
        }

        return toResponse(updatedAppointment, persistedItems);
    }

    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new BadRequestException("At least one service is required");
        }

        String mode = normalizeMode(request.mode());
        ClientRecord client = clientService.requireClient(request.clientId());
        List<AppointmentItemRecord> appointmentItems = buildAppointmentItems(request.items());

        int totalDurationMinutes = appointmentItems.stream().mapToInt(AppointmentItemRecord::durationSnapshotMinutes).sum();
        OffsetDateTime scheduledEnd = request.scheduledStart().plusMinutes(totalDurationMinutes);
        validateScheduleAvailability(request.scheduledStart(), scheduledEnd);
        validateOverlap(request.scheduledStart(), scheduledEnd, null);

        BigDecimal subtotal = appointmentItems.stream()
                .map(AppointmentItemRecord::finalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal travelFee = request.travelFee();
        BigDecimal total = subtotal.add(travelFee);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        UUID appointmentId = UUID.randomUUID();

        AppointmentRecord appointmentRecord = new AppointmentRecord(
                appointmentId,
                client.id(),
                client.fullName(),
                "CONFIRMED",
                mode,
                request.scheduledStart(),
                scheduledEnd,
                normalizeNullable(request.addressSnapshot()),
                normalizeNullable(request.notes()),
                subtotal,
                travelFee,
                total,
                null,
                null,
                null,
                now,
                now
        );

        List<AppointmentItemRecord> persistedItems = appointmentItems.stream()
                .map(item -> new AppointmentItemRecord(
                        UUID.randomUUID(),
                        appointmentId,
                        item.serviceId(),
                        item.serviceNameSnapshot(),
                        item.unitPriceSnapshot(),
                        item.durationSnapshotMinutes(),
                        item.isTouchUp(),
                        item.discountAmount(),
                        item.finalPrice()
                ))
                .toList();

        appointmentRepository.insertAppointment(appointmentRecord);
        appointmentRepository.insertItems(persistedItems);
        clientService.refreshAppointmentStats(client.id());

        return toResponse(appointmentRecord, persistedItems);
    }

    @Transactional
    public AppointmentResponse updateStatus(UUID appointmentId, UpdateAppointmentStatusRequest request) {
        AppointmentRecord existing = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        String status = request.status().trim().toUpperCase();
        if (!ALL_STATUSES.contains(status)) {
            throw new BadRequestException("Unsupported appointment status");
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime completedAt = null;
        OffsetDateTime cancelledAt = null;
        String cancelReason = null;

        if ("COMPLETED".equals(status)) {
            validateOverlap(existing.scheduledStart(), existing.scheduledEnd(), existing.id());
            completedAt = now;
        } else if ("CANCELLED".equals(status)) {
            cancelReason = normalizeNullable(request.cancelReason());
            if (cancelReason == null) {
                throw new BadRequestException("Cancel reason is required when cancelling an appointment");
            }
            cancelledAt = now;
        } else {
            validateScheduleAvailability(existing.scheduledStart(), existing.scheduledEnd());
            validateOverlap(existing.scheduledStart(), existing.scheduledEnd(), existing.id());
        }

        appointmentRepository.updateStatus(existing.id(), status, completedAt, cancelledAt, cancelReason, now);
        clientService.refreshAppointmentStats(existing.clientId());

        AppointmentRecord updated = appointmentRepository.findById(existing.id())
                .orElseThrow(() -> new NotFoundException("Appointment not found after update"));

        return toResponse(updated, appointmentRepository.findItemsByAppointmentId(existing.id()));
    }

    @Transactional
    public void deleteAppointment(UUID appointmentId) {
        AppointmentRecord existing = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new NotFoundException("Appointment not found"));

        appointmentRepository.deleteAppointment(existing.id());
        clientService.refreshAppointmentStats(existing.clientId());
    }

    private List<AppointmentItemRecord> buildAppointmentItems(List<AppointmentItemRequest> items) {
        return items.stream().map(itemRequest -> {
            ServiceCatalogItem service = serviceCatalogRepository.findById(itemRequest.serviceId())
                    .orElseThrow(() -> new NotFoundException("Service not found"));

            if (!service.isActive()) {
                throw new BadRequestException("Inactive services cannot be booked");
            }

            if (itemRequest.touchUp() && !service.supportsTouchUp()) {
                throw new BadRequestException("Selected service does not support touch-up pricing");
            }

            BigDecimal discount = itemRequest.touchUp() ? service.touchUpDiscount() : BigDecimal.ZERO;
            BigDecimal finalPrice = service.basePrice().subtract(discount);
            if (finalPrice.signum() < 0) {
                throw new BadRequestException("Calculated appointment item price cannot be negative");
            }

            return new AppointmentItemRecord(
                    null,
                    null,
                    service.id(),
                    service.name(),
                    service.basePrice(),
                    service.durationMinutes(),
                    itemRequest.touchUp(),
                    discount,
                    finalPrice
            );
        }).toList();
    }

    private void validateOverlap(OffsetDateTime start, OffsetDateTime end, UUID excludedAppointmentId) {
        if (appointmentRepository.existsOverlap(start, end, excludedAppointmentId)) {
            throw new ConflictException("Appointment overlaps with an existing confirmed or completed appointment");
        }
    }

    private void validateScheduleAvailability(OffsetDateTime start, OffsetDateTime end) {
        var localStart = start.atZoneSameInstant(BUSINESS_ZONE).toLocalDateTime();
        var localEnd = end.atZoneSameInstant(BUSINESS_ZONE).toLocalDateTime();
        LocalDate appointmentDate = localStart.toLocalDate();

        if (!appointmentDate.equals(localEnd.toLocalDate())) {
            throw new ConflictException("Appointment cannot cross business days");
        }

        short dayOfWeek = (short) appointmentDate.getDayOfWeek().getValue();
        BusinessHourRecord businessHour = businessHoursRepository.findByDayOfWeek(dayOfWeek)
                .orElseThrow(() -> new ConflictException("Business hours are not configured for this day"));

        LocalTime startTime = localStart.toLocalTime();
        LocalTime endTime = localEnd.toLocalTime();
        if (businessHour.isClosed()
                || businessHour.openTime() == null
                || businessHour.closeTime() == null
                || startTime.isBefore(businessHour.openTime())
                || endTime.isAfter(businessHour.closeTime())) {
            throw new ConflictException("Appointment is outside business hours");
        }

        List<ScheduleBlockRecord> blocks = scheduleBlockRepository.findBetween(appointmentDate, appointmentDate);
        for (ScheduleBlockRecord block : blocks) {
            if (block.isFullDay()) {
                throw new ConflictException("Appointment overlaps with a schedule block");
            }

            if (block.startTime() == null || block.endTime() == null) {
                throw new ConflictException("Appointment overlaps with a schedule block");
            }

            if (startTime.isBefore(block.endTime()) && endTime.isAfter(block.startTime())) {
                throw new ConflictException("Appointment overlaps with a schedule block");
            }
        }
    }

    private String normalizeMode(String mode) {
        String normalized = mode == null ? "" : mode.trim().toUpperCase();
        if (!ALL_MODES.contains(normalized)) {
            throw new BadRequestException("Unsupported appointment mode");
        }
        return normalized;
    }

    private Map<UUID, List<AppointmentItemRecord>> groupItems(List<AppointmentRecord> appointments) {
        List<UUID> appointmentIds = appointments.stream().map(AppointmentRecord::id).toList();
        Map<UUID, List<AppointmentItemRecord>> itemsByAppointmentId = new LinkedHashMap<>();
        for (AppointmentItemRecord item : appointmentRepository.findItemsByAppointmentIds(appointmentIds)) {
            itemsByAppointmentId.computeIfAbsent(item.appointmentId(), ignored -> new java.util.ArrayList<>()).add(item);
        }
        return itemsByAppointmentId;
    }

    private AppointmentResponse toResponse(AppointmentRecord appointment, List<AppointmentItemRecord> items) {
        return new AppointmentResponse(
                appointment.id(),
                appointment.clientId(),
                appointment.clientName(),
                appointment.status(),
                appointment.appointmentMode(),
                appointment.scheduledStart(),
                appointment.scheduledEnd(),
                appointment.addressSnapshot(),
                appointment.notes(),
                appointment.subtotalAmount(),
                appointment.travelFee(),
                appointment.totalAmount(),
                appointment.completedAt(),
                appointment.cancelledAt(),
                appointment.cancelReason(),
                items.stream().map(item -> new AppointmentItemResponse(
                        item.id(),
                        item.serviceId(),
                        item.serviceNameSnapshot(),
                        item.unitPriceSnapshot(),
                        item.durationSnapshotMinutes(),
                        item.isTouchUp(),
                        item.discountAmount(),
                        item.finalPrice()
                )).toList()
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
