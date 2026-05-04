package com.jenislashes.content.testimonial.service;

import com.jenislashes.common.exception.NotFoundException;
import com.jenislashes.content.testimonial.dto.TestimonialResponse;
import com.jenislashes.content.testimonial.dto.UpsertTestimonialRequest;
import com.jenislashes.content.testimonial.model.TestimonialRecord;
import com.jenislashes.content.testimonial.repository.TestimonialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class TestimonialService {

    private final TestimonialRepository testimonialRepository;

    public TestimonialService(TestimonialRepository testimonialRepository) {
        this.testimonialRepository = testimonialRepository;
    }

    public List<TestimonialResponse> listAdmin() {
        return testimonialRepository.findAll().stream().map(this::toResponse).toList();
    }

    public List<TestimonialResponse> listPublic() {
        return testimonialRepository.findPublic().stream().map(this::toResponse).toList();
    }

    @Transactional
    public TestimonialResponse create(UpsertTestimonialRequest request) {
        TestimonialRecord record = new TestimonialRecord(
                UUID.randomUUID(),
                request.clientName().trim(),
                request.text().trim(),
                request.rating(),
                request.isFeatured(),
                request.sortOrder(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        testimonialRepository.insert(record);
        return toResponse(record);
    }

    @Transactional
    public TestimonialResponse update(UUID testimonialId, UpsertTestimonialRequest request) {
        TestimonialRecord existing = testimonialRepository.findById(testimonialId)
                .orElseThrow(() -> new NotFoundException("Testimonial not found"));

        TestimonialRecord updated = new TestimonialRecord(
                existing.id(),
                request.clientName().trim(),
                request.text().trim(),
                request.rating(),
                request.isFeatured(),
                request.sortOrder(),
                existing.createdAt()
        );
        testimonialRepository.update(updated);
        return toResponse(updated);
    }

    private TestimonialResponse toResponse(TestimonialRecord record) {
        return new TestimonialResponse(
                record.id(),
                record.clientName(),
                record.text(),
                record.rating(),
                record.isFeatured(),
                record.sortOrder(),
                record.createdAt()
        );
    }
}
