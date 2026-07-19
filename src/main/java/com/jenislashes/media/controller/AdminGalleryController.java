package com.jenislashes.media.controller;

import com.jenislashes.media.dto.GalleryItemResponse;
import com.jenislashes.media.dto.UpdateGalleryItemRequest;
import com.jenislashes.media.service.GalleryService;
import com.jenislashes.publicapi.PublicCacheService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/gallery")
public class AdminGalleryController {

    private final GalleryService galleryService;
    private final PublicCacheService publicCacheService;

    public AdminGalleryController(
            GalleryService galleryService,
            PublicCacheService publicCacheService
    ) {
        this.galleryService = galleryService;
        this.publicCacheService = publicCacheService;
    }

    @GetMapping
    public ResponseEntity<List<GalleryItemResponse>> list() {
        return ResponseEntity.ok(galleryService.listAdmin());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GalleryItemResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String altText,
            @RequestParam(required = false) String caption,
            @RequestParam(required = false) Integer sortOrder,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) UUID serviceId
    ) {
        GalleryItemResponse response = galleryService.upload(file, altText, caption, sortOrder, isActive, serviceId);
        publicCacheService.evictAll();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{galleryItemId}")
    public ResponseEntity<GalleryItemResponse> update(
            @PathVariable UUID galleryItemId,
            @Valid @org.springframework.web.bind.annotation.RequestBody UpdateGalleryItemRequest request
    ) {
        GalleryItemResponse response = galleryService.update(galleryItemId, request);
        publicCacheService.evictAll();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{galleryItemId}")
    public ResponseEntity<Void> delete(@PathVariable UUID galleryItemId) {
        galleryService.delete(galleryItemId);
        publicCacheService.evictAll();
        return ResponseEntity.noContent().build();
    }
}
