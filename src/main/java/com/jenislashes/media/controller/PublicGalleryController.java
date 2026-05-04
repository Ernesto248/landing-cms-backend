package com.jenislashes.media.controller;

import com.jenislashes.media.dto.GalleryItemResponse;
import com.jenislashes.media.service.GalleryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/gallery")
public class PublicGalleryController {

    private final GalleryService galleryService;

    public PublicGalleryController(GalleryService galleryService) {
        this.galleryService = galleryService;
    }

    @GetMapping
    public ResponseEntity<List<GalleryItemResponse>> list() {
        return ResponseEntity.ok(galleryService.listPublic());
    }
}
