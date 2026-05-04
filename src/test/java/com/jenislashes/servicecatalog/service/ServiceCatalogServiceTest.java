package com.jenislashes.servicecatalog.service;

import com.jenislashes.common.exception.BadRequestException;
import com.jenislashes.common.exception.NotFoundException;
import com.jenislashes.servicecatalog.dto.UpsertServiceRequest;
import com.jenislashes.servicecatalog.model.ServiceCatalogItem;
import com.jenislashes.servicecatalog.repository.ServiceCatalogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceCatalogService")
class ServiceCatalogServiceTest {

    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;

    @InjectMocks
    private ServiceCatalogService serviceCatalogService;

    @Test
    void createService_should_throw_when_touch_up_discount_is_set_without_support() {
        assertThrows(BadRequestException.class, () -> serviceCatalogService.createService(new UpsertServiceRequest(
                "lashes",
                "Clasicas",
                null,
                new BigDecimal("3000.00"),
                150,
                false,
                new BigDecimal("500.00"),
                true,
                1
        )));
    }

    @Test
    void createService_should_generate_unique_slug_and_normalize_fields() {
        ServiceCatalogItem existingSlug = new ServiceCatalogItem(
                UUID.randomUUID(),
                "LASHES",
                "Aplicacion de Volumen 2D",
                "aplicacion-de-volumen-2d",
                null,
                new BigDecimal("3000.00"),
                150,
                true,
                new BigDecimal("500.00"),
                true,
                1,
                OffsetDateTime.parse("2026-05-01T10:00:00Z"),
                OffsetDateTime.parse("2026-05-01T10:00:00Z")
        );
        when(serviceCatalogRepository.findBySlug("aplicacion-de-volumen-2d")).thenReturn(Optional.of(existingSlug));
        when(serviceCatalogRepository.findBySlug("aplicacion-de-volumen-2d-2")).thenReturn(Optional.empty());

        var response = serviceCatalogService.createService(new UpsertServiceRequest(
                "  lashes  ",
                "  Aplicacion de Volumen 2D  ",
                "   ",
                new BigDecimal("3300.00"),
                150,
                true,
                new BigDecimal("500.00"),
                true,
                2
        ));

        ArgumentCaptor<ServiceCatalogItem> itemCaptor = ArgumentCaptor.forClass(ServiceCatalogItem.class);
        verify(serviceCatalogRepository).insert(itemCaptor.capture());

        ServiceCatalogItem savedItem = itemCaptor.getValue();

        assertAll(
                () -> assertEquals("LASHES", savedItem.category()),
                () -> assertEquals("Aplicacion de Volumen 2D", savedItem.name()),
                () -> assertEquals("aplicacion-de-volumen-2d-2", savedItem.slug()),
                () -> assertNull(savedItem.description()),
                () -> assertEquals("aplicacion-de-volumen-2d-2", response.slug()),
                () -> assertEquals("LASHES", response.category())
        );
    }

    @Test
    void updateService_should_throw_when_service_does_not_exist() {
        UUID serviceId = UUID.randomUUID();
        when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> serviceCatalogService.updateService(serviceId, new UpsertServiceRequest(
                "lashes",
                "Clasicas",
                null,
                new BigDecimal("3000.00"),
                150,
                true,
                new BigDecimal("500.00"),
                true,
                1
        )));
    }

    @Test
    void updateService_should_keep_matching_slug_owned_by_same_service() {
        UUID serviceId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-05-01T10:00:00Z");
        ServiceCatalogItem existing = new ServiceCatalogItem(
                serviceId,
                "LASHES",
                "Clasicas",
                "clasicas",
                "old",
                new BigDecimal("3000.00"),
                150,
                true,
                new BigDecimal("500.00"),
                true,
                1,
                createdAt,
                createdAt
        );
        when(serviceCatalogRepository.findById(serviceId)).thenReturn(Optional.of(existing));
        when(serviceCatalogRepository.findBySlug("clasicas")).thenReturn(Optional.of(existing));

        var response = serviceCatalogService.updateService(serviceId, new UpsertServiceRequest(
                "  lashes  ",
                "  Clasicas  ",
                "  natural  ",
                new BigDecimal("3200.00"),
                160,
                true,
                new BigDecimal("500.00"),
                false,
                4
        ));

        ArgumentCaptor<ServiceCatalogItem> itemCaptor = ArgumentCaptor.forClass(ServiceCatalogItem.class);
        verify(serviceCatalogRepository).update(itemCaptor.capture());
        verify(serviceCatalogRepository).findBySlug(eq("clasicas"));

        ServiceCatalogItem updatedItem = itemCaptor.getValue();

        assertAll(
                () -> assertEquals(serviceId, updatedItem.id()),
                () -> assertEquals(createdAt, updatedItem.createdAt()),
                () -> assertEquals("clasicas", updatedItem.slug()),
                () -> assertEquals("natural", updatedItem.description()),
                () -> assertEquals(false, updatedItem.isActive()),
                () -> assertEquals("clasicas", response.slug())
        );
    }
}
