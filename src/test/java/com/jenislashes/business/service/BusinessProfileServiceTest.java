package com.jenislashes.business.service;

import com.jenislashes.business.dto.UpdateBusinessProfileRequest;
import com.jenislashes.business.model.BusinessProfileRecord;
import com.jenislashes.business.repository.BusinessProfileRepository;
import com.jenislashes.common.exception.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BusinessProfileService")
class BusinessProfileServiceTest {

    @Mock
    private BusinessProfileRepository businessProfileRepository;

    @InjectMocks
    private BusinessProfileService businessProfileService;

    @Test
    void getProfile_should_throw_when_profile_is_missing() {
        when(businessProfileRepository.findCurrent()).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> businessProfileService.getProfile());
    }

    @Test
    void updateProfile_should_trim_optional_fields_and_uppercase_currency() {
        UUID profileId = UUID.randomUUID();
        OffsetDateTime updatedAt = OffsetDateTime.parse("2026-05-01T10:00:00Z");
        BusinessProfileRecord existing = new BusinessProfileRecord(
                profileId,
                "Jeni's",
                "Old",
                "Old",
                "5350000000",
                "Old address",
                "Havana",
                "Cuba",
                "CUP",
                "America/Havana",
                null,
                null,
                true,
                true,
                true,
                updatedAt
        );
        when(businessProfileRepository.findCurrent()).thenReturn(Optional.of(existing));

        var response = businessProfileService.updateProfile(new UpdateBusinessProfileRequest(
                "  Jeni's Lashes & Brows  ",
                "   ",
                "  Expertas en brows y lashes  ",
                "  +5355512345  ",
                "   ",
                "  Havana  ",
                "  Cuba  ",
                "  cup  ",
                "  America/Havana  ",
                "  https://instagram.com/jenis  ",
                "   ",
                true,
                true,
                false
        ));

        ArgumentCaptor<BusinessProfileRecord> recordCaptor = ArgumentCaptor.forClass(BusinessProfileRecord.class);
        verify(businessProfileRepository).update(recordCaptor.capture());

        BusinessProfileRecord updatedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals(profileId, updatedRecord.id()),
                () -> assertEquals("Jeni's Lashes & Brows", updatedRecord.brandName()),
                () -> assertNull(updatedRecord.tagline()),
                () -> assertEquals("Expertas en brows y lashes", updatedRecord.description()),
                () -> assertEquals("+5355512345", updatedRecord.phoneWhatsapp()),
                () -> assertNull(updatedRecord.addressLine()),
                () -> assertEquals("Havana", updatedRecord.city()),
                () -> assertEquals("Cuba", updatedRecord.country()),
                () -> assertEquals("CUP", updatedRecord.currencyCode()),
                () -> assertEquals("America/Havana", updatedRecord.timezone()),
                () -> assertEquals("https://instagram.com/jenis", updatedRecord.instagramUrl()),
                () -> assertNull(updatedRecord.facebookUrl()),
                () -> assertEquals(false, updatedRecord.supportsStudioService()),
                () -> assertEquals("CUP", response.currencyCode())
        );
    }
}
