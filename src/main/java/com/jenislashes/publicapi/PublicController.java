package com.jenislashes.publicapi;

import com.jenislashes.business.dto.BusinessHourResponse;
import com.jenislashes.business.dto.BusinessProfileResponse;
import com.jenislashes.servicecatalog.dto.ServiceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final PublicCacheService publicCacheService;

    public PublicController(PublicCacheService publicCacheService) {
        this.publicCacheService = publicCacheService;
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponse>> services() {
        return ResponseEntity.ok(publicCacheService.services());
    }

    @GetMapping("/business-profile")
    public ResponseEntity<BusinessProfileResponse> businessProfile() {
        return ResponseEntity.ok(publicCacheService.businessProfile());
    }

    @GetMapping("/business-hours")
    public ResponseEntity<List<BusinessHourResponse>> businessHours() {
        return ResponseEntity.ok(publicCacheService.businessHours());
    }
}
