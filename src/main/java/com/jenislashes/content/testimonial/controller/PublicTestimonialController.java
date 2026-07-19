package com.jenislashes.content.testimonial.controller;

import com.jenislashes.content.testimonial.dto.TestimonialResponse;
import com.jenislashes.publicapi.PublicCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/public/testimonials")
public class PublicTestimonialController {

    private final PublicCacheService publicCacheService;

    public PublicTestimonialController(PublicCacheService publicCacheService) {
        this.publicCacheService = publicCacheService;
    }

    @GetMapping
    public ResponseEntity<List<TestimonialResponse>> list() {
        return ResponseEntity.ok(publicCacheService.testimonials());
    }
}
