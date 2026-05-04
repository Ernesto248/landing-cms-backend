package com.jenislashes.publicapi;

import com.jenislashes.business.dto.BusinessHourResponse;
import com.jenislashes.business.dto.BusinessProfileResponse;
import com.jenislashes.business.service.BusinessHoursService;
import com.jenislashes.business.service.BusinessProfileService;
import com.jenislashes.servicecatalog.dto.ServiceResponse;
import com.jenislashes.servicecatalog.service.ServiceCatalogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class PublicController {

    private final ServiceCatalogService serviceCatalogService;
    private final BusinessProfileService businessProfileService;
    private final BusinessHoursService businessHoursService;

    public PublicController(
            ServiceCatalogService serviceCatalogService,
            BusinessProfileService businessProfileService,
            BusinessHoursService businessHoursService
    ) {
        this.serviceCatalogService = serviceCatalogService;
        this.businessProfileService = businessProfileService;
        this.businessHoursService = businessHoursService;
    }

    @GetMapping("/health-check")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponse>> services() {
        return ResponseEntity.ok(serviceCatalogService.getPublicServices());
    }

    @GetMapping("/business-profile")
    public ResponseEntity<BusinessProfileResponse> businessProfile() {
        return ResponseEntity.ok(businessProfileService.getProfile());
    }

    @GetMapping("/business-hours")
    public ResponseEntity<List<BusinessHourResponse>> businessHours() {
        return ResponseEntity.ok(businessHoursService.listHours());
    }
}
