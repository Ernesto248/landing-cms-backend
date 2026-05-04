package com.jenislashes.business.controller;

import com.jenislashes.business.dto.BusinessHourResponse;
import com.jenislashes.business.dto.BusinessProfileResponse;
import com.jenislashes.business.dto.CreateScheduleBlockRequest;
import com.jenislashes.business.dto.ScheduleBlockResponse;
import com.jenislashes.business.dto.UpdateBusinessProfileRequest;
import com.jenislashes.business.dto.UpsertBusinessHourRequest;
import com.jenislashes.business.service.BusinessHoursService;
import com.jenislashes.business.service.BusinessProfileService;
import com.jenislashes.business.service.ScheduleBlockService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/business")
public class AdminBusinessController {

    private final BusinessProfileService businessProfileService;
    private final BusinessHoursService businessHoursService;
    private final ScheduleBlockService scheduleBlockService;

    public AdminBusinessController(
            BusinessProfileService businessProfileService,
            BusinessHoursService businessHoursService,
            ScheduleBlockService scheduleBlockService
    ) {
        this.businessProfileService = businessProfileService;
        this.businessHoursService = businessHoursService;
        this.scheduleBlockService = scheduleBlockService;
    }

    @GetMapping("/profile")
    public ResponseEntity<BusinessProfileResponse> profile() {
        return ResponseEntity.ok(businessProfileService.getProfile());
    }

    @PutMapping("/profile")
    public ResponseEntity<BusinessProfileResponse> updateProfile(@Valid @RequestBody UpdateBusinessProfileRequest request) {
        return ResponseEntity.ok(businessProfileService.updateProfile(request));
    }

    @GetMapping("/hours")
    public ResponseEntity<List<BusinessHourResponse>> hours() {
        return ResponseEntity.ok(businessHoursService.listHours());
    }

    @PutMapping("/hours")
    public ResponseEntity<List<BusinessHourResponse>> upsertHours(@Valid @RequestBody List<@Valid UpsertBusinessHourRequest> requests) {
        return ResponseEntity.ok(businessHoursService.upsertHours(requests));
    }

    @GetMapping("/schedule-blocks")
    public ResponseEntity<List<ScheduleBlockResponse>> scheduleBlocks(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(scheduleBlockService.listBlocks(from, to));
    }

    @PostMapping("/schedule-blocks")
    public ResponseEntity<ScheduleBlockResponse> createScheduleBlock(@Valid @RequestBody CreateScheduleBlockRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleBlockService.createBlock(request));
    }

    @DeleteMapping("/schedule-blocks/{scheduleBlockId}")
    public ResponseEntity<Void> deleteScheduleBlock(@PathVariable UUID scheduleBlockId) {
        scheduleBlockService.deleteBlock(scheduleBlockId);
        return ResponseEntity.noContent().build();
    }
}
