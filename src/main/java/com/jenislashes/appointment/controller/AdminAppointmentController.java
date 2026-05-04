package com.jenislashes.appointment.controller;

import com.jenislashes.appointment.dto.AppointmentResponse;
import com.jenislashes.appointment.dto.CreateAppointmentRequest;
import com.jenislashes.appointment.dto.UpdateAppointmentStatusRequest;
import com.jenislashes.appointment.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/appointments")
public class AdminAppointmentController {

    private final AppointmentService appointmentService;

    public AdminAppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponse>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to
    ) {
        return ResponseEntity.ok(appointmentService.listAppointments(from, to));
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> get(@PathVariable UUID appointmentId) {
        return ResponseEntity.ok(appointmentService.getAppointment(appointmentId));
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> create(@Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.createAppointment(request));
    }

    @PutMapping("/{appointmentId}")
    public ResponseEntity<AppointmentResponse> update(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        return ResponseEntity.ok(appointmentService.updateAppointment(appointmentId, request));
    }

    @PatchMapping("/{appointmentId}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable UUID appointmentId,
            @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        return ResponseEntity.ok(appointmentService.updateStatus(appointmentId, request));
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<Void> delete(@PathVariable UUID appointmentId) {
        appointmentService.deleteAppointment(appointmentId);
        return ResponseEntity.noContent().build();
    }
}
