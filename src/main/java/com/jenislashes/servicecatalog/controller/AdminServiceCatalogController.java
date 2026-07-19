package com.jenislashes.servicecatalog.controller;

import com.jenislashes.servicecatalog.dto.ServiceResponse;
import com.jenislashes.servicecatalog.dto.UpsertServiceRequest;
import com.jenislashes.publicapi.PublicCacheService;
import com.jenislashes.servicecatalog.service.ServiceCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/services")
public class AdminServiceCatalogController {

    private final ServiceCatalogService serviceCatalogService;
    private final PublicCacheService publicCacheService;

    public AdminServiceCatalogController(
            ServiceCatalogService serviceCatalogService,
            PublicCacheService publicCacheService
    ) {
        this.serviceCatalogService = serviceCatalogService;
        this.publicCacheService = publicCacheService;
    }

    @GetMapping
    public ResponseEntity<List<ServiceResponse>> list() {
        return ResponseEntity.ok(serviceCatalogService.getAdminServices());
    }

    @PostMapping
    public ResponseEntity<ServiceResponse> create(@Valid @RequestBody UpsertServiceRequest request) {
        ServiceResponse response = serviceCatalogService.createService(request);
        publicCacheService.evictAll();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{serviceId}")
    public ResponseEntity<ServiceResponse> update(
            @PathVariable UUID serviceId,
            @Valid @RequestBody UpsertServiceRequest request
    ) {
        ServiceResponse response = serviceCatalogService.updateService(serviceId, request);
        publicCacheService.evictAll();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> archive(@PathVariable UUID serviceId) {
        serviceCatalogService.archiveService(serviceId);
        publicCacheService.evictAll();
        return ResponseEntity.noContent().build();
    }
}
