package com.jenislashes.appointment.service;

import com.jenislashes.appointment.dto.AppointmentItemRequest;
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
import com.jenislashes.servicecatalog.model.ServiceCatalogItem;
import com.jenislashes.servicecatalog.repository.ServiceCatalogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AppointmentService")
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ClientService clientService;

    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;

    @Mock
    private BusinessHoursRepository businessHoursRepository;

    @Mock
    private ScheduleBlockRepository scheduleBlockRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void createAppointment_should_calculate_totals_and_snapshots_when_request_is_valid() {
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-05-10T14:00:00Z");
        ClientRecord client = new ClientRecord(clientId, "Maria Perez", "5551111", "5551111", null, null, 0, start, start);
        ServiceCatalogItem service = new ServiceCatalogItem(
                serviceId,
                "LASHES",
                "Aplicacion de Volumen 2D",
                "volumen-2d",
                "desc",
                new BigDecimal("3000.00"),
                150,
                true,
                new BigDecimal("500.00"),
                true,
                1,
                start,
                start
        );

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                clientId,
                start,
                List.of(new AppointmentItemRequest(serviceId, true)),
                "HOME",
                new BigDecimal("300.00"),
                "Centro Habana",
                "Prueba"
        );

        when(clientService.requireClient(clientId)).thenReturn(client);
        when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.of(service));
        allowBusinessSchedule(start, start.plusMinutes(150), List.of());
        when(appointmentRepository.existsOverlap(any(), any(), eq(null))).thenReturn(false);

        var response = appointmentService.createAppointment(request);

        ArgumentCaptor<AppointmentRecord> appointmentCaptor = ArgumentCaptor.forClass(AppointmentRecord.class);
        ArgumentCaptor<List<AppointmentItemRecord>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(appointmentRepository).insertAppointment(appointmentCaptor.capture());
        verify(appointmentRepository).insertItems(itemsCaptor.capture());
        verify(clientService).refreshAppointmentStats(clientId);

        AppointmentRecord savedAppointment = appointmentCaptor.getValue();
        AppointmentItemRecord savedItem = itemsCaptor.getValue().getFirst();

        assertAll(
                () -> assertEquals("CONFIRMED", response.status()),
                () -> assertEquals("HOME", response.appointmentMode()),
                () -> assertEquals(new BigDecimal("2500.00"), response.subtotalAmount()),
                () -> assertEquals(new BigDecimal("300.00"), response.travelFee()),
                () -> assertEquals(new BigDecimal("2800.00"), response.totalAmount()),
                () -> assertEquals(start.plusMinutes(150), response.scheduledEnd()),
                () -> assertEquals(new BigDecimal("500.00"), savedItem.discountAmount()),
                () -> assertEquals(new BigDecimal("2500.00"), savedItem.finalPrice()),
                () -> assertEquals(new BigDecimal("2800.00"), savedAppointment.totalAmount())
        );
    }

    @Test
    void createAppointment_should_throw_conflict_when_overlap_exists() {
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-05-10T14:00:00Z");

        when(clientService.requireClient(clientId)).thenReturn(new ClientRecord(clientId, "Maria", null, null, null, null, 0, start, start));
        when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.of(new ServiceCatalogItem(
                serviceId,
                "BROWS",
                "Diseno",
                "diseno",
                null,
                new BigDecimal("300.00"),
                15,
                false,
                BigDecimal.ZERO,
                true,
                1,
                start,
                start
        )));
        allowBusinessSchedule(start, start.plusMinutes(15), List.of());
        when(appointmentRepository.existsOverlap(any(), any(), eq(null))).thenReturn(true);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                clientId,
                start,
                List.of(new AppointmentItemRequest(serviceId, false)),
                "STUDIO",
                BigDecimal.ZERO,
                null,
                null
        );

        assertThrows(ConflictException.class, () -> appointmentService.createAppointment(request));
        verify(appointmentRepository, never()).insertAppointment(any());
    }

    @Test
    void createAppointment_should_throw_bad_request_when_touch_up_is_not_supported() {
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-05-10T14:00:00Z");

        when(clientService.requireClient(clientId)).thenReturn(new ClientRecord(clientId, "Maria", null, null, null, null, 0, start, start));
        when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.of(new ServiceCatalogItem(
                serviceId,
                "BROWS",
                "Diseno",
                "diseno",
                null,
                new BigDecimal("300.00"),
                15,
                false,
                BigDecimal.ZERO,
                true,
                1,
                start,
                start
        )));

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                clientId,
                start,
                List.of(new AppointmentItemRequest(serviceId, true)),
                "STUDIO",
                BigDecimal.ZERO,
                null,
                null
        );

        assertThrows(BadRequestException.class, () -> appointmentService.createAppointment(request));
    }

    @Test
    void updateStatus_should_require_cancel_reason_when_status_is_cancelled() {
        UUID appointmentId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-05-10T14:00:00Z");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(new AppointmentRecord(
                appointmentId,
                clientId,
                "Maria",
                "CONFIRMED",
                "STUDIO",
                start,
                start.plusMinutes(30),
                null,
                null,
                new BigDecimal("300.00"),
                BigDecimal.ZERO,
                new BigDecimal("300.00"),
                null,
                null,
                null,
                start,
                start
        )));

        assertThrows(BadRequestException.class, () -> appointmentService.updateStatus(appointmentId, new UpdateAppointmentStatusRequest("cancelled", "  ")));
        verify(appointmentRepository, never()).updateStatus(any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateAppointment_should_replace_items_and_refresh_both_clients_when_client_changes() {
        UUID appointmentId = UUID.randomUUID();
        UUID currentClientId = UUID.randomUUID();
        UUID nextClientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-05-10T14:00:00Z");
        OffsetDateTime completedAt = OffsetDateTime.parse("2026-05-10T16:30:00Z");

        AppointmentRecord existing = new AppointmentRecord(
                appointmentId,
                currentClientId,
                "Maria",
                "COMPLETED",
                "STUDIO",
                start,
                start.plusMinutes(30),
                null,
                "nota vieja",
                new BigDecimal("300.00"),
                BigDecimal.ZERO,
                new BigDecimal("300.00"),
                completedAt,
                null,
                null,
                start,
                start
        );
        ClientRecord nextClient = new ClientRecord(nextClientId, "Laura", "5552222", "5552222", null, null, 0, start, start);
        ServiceCatalogItem service = new ServiceCatalogItem(
                serviceId,
                "LASHES",
                "Aplicacion de Clasicas",
                "clasicas",
                null,
                new BigDecimal("3000.00"),
                150,
                true,
                new BigDecimal("500.00"),
                true,
                1,
                start,
                start
        );

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existing));
        when(clientService.requireClient(nextClientId)).thenReturn(nextClient);
        when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.of(service));
        allowBusinessSchedule(start.plusDays(1), start.plusDays(1).plusMinutes(150), List.of());
        when(appointmentRepository.existsOverlap(any(), any(), eq(appointmentId))).thenReturn(false);

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                nextClientId,
                start.plusDays(1),
                List.of(new AppointmentItemRequest(serviceId, true)),
                "HOME",
                new BigDecimal("400.00"),
                "Vedado",
                "nueva nota"
        );

        var response = appointmentService.updateAppointment(appointmentId, request);

        ArgumentCaptor<AppointmentRecord> appointmentCaptor = ArgumentCaptor.forClass(AppointmentRecord.class);
        ArgumentCaptor<List<AppointmentItemRecord>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        verify(appointmentRepository).updateAppointment(appointmentCaptor.capture());
        verify(appointmentRepository).deleteItemsByAppointmentId(appointmentId);
        verify(appointmentRepository).insertItems(itemsCaptor.capture());
        verify(clientService).refreshAppointmentStats(currentClientId);
        verify(clientService).refreshAppointmentStats(nextClientId);

        AppointmentRecord updatedAppointment = appointmentCaptor.getValue();
        AppointmentItemRecord updatedItem = itemsCaptor.getValue().getFirst();

        assertAll(
                () -> assertEquals(nextClientId, updatedAppointment.clientId()),
                () -> assertEquals("COMPLETED", updatedAppointment.status()),
                () -> assertEquals("HOME", updatedAppointment.appointmentMode()),
                () -> assertEquals(start.plusDays(1).plusMinutes(150), updatedAppointment.scheduledEnd()),
                () -> assertEquals(completedAt, updatedAppointment.completedAt()),
                () -> assertNull(updatedAppointment.cancelledAt()),
                () -> assertEquals(new BigDecimal("2900.00"), updatedAppointment.totalAmount()),
                () -> assertEquals(new BigDecimal("500.00"), updatedItem.discountAmount()),
                () -> assertEquals(new BigDecimal("2500.00"), updatedItem.finalPrice()),
                () -> assertEquals(new BigDecimal("2900.00"), response.totalAmount())
        );
    }

    @Test
    void deleteAppointment_should_remove_appointment_and_refresh_client_stats() {
        UUID appointmentId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-05-10T14:00:00Z");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(new AppointmentRecord(
                appointmentId,
                clientId,
                "Maria",
                "CONFIRMED",
                "STUDIO",
                start,
                start.plusMinutes(30),
                null,
                null,
                new BigDecimal("300.00"),
                BigDecimal.ZERO,
                new BigDecimal("300.00"),
                null,
                null,
                null,
                start,
                start
        )));

        appointmentService.deleteAppointment(appointmentId);

        verify(appointmentRepository).deleteAppointment(appointmentId);
        verify(clientService).refreshAppointmentStats(clientId);
    }

    @Test
    void createAppointment_should_reject_when_outside_business_hours() {
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-05-10T22:00:00Z");

        when(clientService.requireClient(clientId)).thenReturn(new ClientRecord(clientId, "Maria", null, null, null, null, 0, start, start));
        when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.of(activeService(serviceId, start, 60)));
        when(businessHoursRepository.findByDayOfWeek((short) 7)).thenReturn(Optional.of(new BusinessHourRecord(
                UUID.randomUUID(),
                (short) 7,
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                false
        )));

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                clientId,
                start,
                List.of(new AppointmentItemRequest(serviceId, false)),
                "STUDIO",
                BigDecimal.ZERO,
                null,
                null
        );

        assertThrows(ConflictException.class, () -> appointmentService.createAppointment(request));
        verify(appointmentRepository, never()).insertAppointment(any());
    }

    @Test
    void createAppointment_should_reject_when_day_is_closed() {
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-05-10T14:00:00Z");

        when(clientService.requireClient(clientId)).thenReturn(new ClientRecord(clientId, "Maria", null, null, null, null, 0, start, start));
        when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.of(activeService(serviceId, start, 60)));
        when(businessHoursRepository.findByDayOfWeek((short) 7)).thenReturn(Optional.of(new BusinessHourRecord(
                UUID.randomUUID(),
                (short) 7,
                null,
                null,
                true
        )));

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                clientId,
                start,
                List.of(new AppointmentItemRequest(serviceId, false)),
                "STUDIO",
                BigDecimal.ZERO,
                null,
                null
        );

        assertThrows(ConflictException.class, () -> appointmentService.createAppointment(request));
        verify(appointmentRepository, never()).insertAppointment(any());
    }

    @Test
    void createAppointment_should_reject_when_schedule_block_overlaps() {
        UUID clientId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        OffsetDateTime start = OffsetDateTime.parse("2026-05-10T14:00:00Z");
        LocalDate localDate = start.atZoneSameInstant(ZoneId.of("America/Havana")).toLocalDate();

        when(clientService.requireClient(clientId)).thenReturn(new ClientRecord(clientId, "Maria", null, null, null, null, 0, start, start));
        when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.of(activeService(serviceId, start, 60)));
        allowBusinessSchedule(start, start.plusMinutes(60), List.of(new ScheduleBlockRecord(
                UUID.randomUUID(),
                localDate,
                LocalTime.of(9, 30),
                LocalTime.of(10, 30),
                "Bloqueo",
                false,
                start
        )));

        CreateAppointmentRequest request = new CreateAppointmentRequest(
                clientId,
                start,
                List.of(new AppointmentItemRequest(serviceId, false)),
                "STUDIO",
                BigDecimal.ZERO,
                null,
                null
        );

        assertThrows(ConflictException.class, () -> appointmentService.createAppointment(request));
        verify(appointmentRepository, never()).insertAppointment(any());
    }

    private void allowBusinessSchedule(OffsetDateTime start, OffsetDateTime end, List<ScheduleBlockRecord> blocks) {
        var localStart = start.atZoneSameInstant(ZoneId.of("America/Havana")).toLocalDateTime();
        var localEnd = end.atZoneSameInstant(ZoneId.of("America/Havana")).toLocalDateTime();
        short dayOfWeek = (short) localStart.toLocalDate().getDayOfWeek().getValue();
        when(businessHoursRepository.findByDayOfWeek(dayOfWeek)).thenReturn(Optional.of(new BusinessHourRecord(
                UUID.randomUUID(),
                dayOfWeek,
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                false
        )));
        when(scheduleBlockRepository.findBetween(localStart.toLocalDate(), localEnd.toLocalDate())).thenReturn(blocks);
    }

    private ServiceCatalogItem activeService(UUID serviceId, OffsetDateTime now, int durationMinutes) {
        return new ServiceCatalogItem(
                serviceId,
                "BROWS",
                "Diseno",
                "diseno",
                null,
                new BigDecimal("300.00"),
                durationMinutes,
                false,
                BigDecimal.ZERO,
                true,
                1,
                now,
                now
        );
    }
}
