package com.jenislashes.content.testimonial.controller;

import com.jenislashes.content.testimonial.dto.TestimonialResponse;
import com.jenislashes.content.testimonial.dto.UpsertTestimonialRequest;
import com.jenislashes.content.testimonial.service.TestimonialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/testimonials")
public class AdminTestimonialController {

    private final TestimonialService testimonialService;

    public AdminTestimonialController(TestimonialService testimonialService) {
        this.testimonialService = testimonialService;
    }

    @GetMapping
    public ResponseEntity<List<TestimonialResponse>> list() {
        return ResponseEntity.ok(testimonialService.listAdmin());
    }

    @PostMapping
    public ResponseEntity<TestimonialResponse> create(@Valid @RequestBody UpsertTestimonialRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testimonialService.create(request));
    }

    @PutMapping("/{testimonialId}")
    public ResponseEntity<TestimonialResponse> update(
            @PathVariable UUID testimonialId,
            @Valid @RequestBody UpsertTestimonialRequest request
    ) {
        return ResponseEntity.ok(testimonialService.update(testimonialId, request));
    }
}
