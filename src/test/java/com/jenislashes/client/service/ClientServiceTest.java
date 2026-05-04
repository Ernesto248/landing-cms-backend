package com.jenislashes.client.service;

import com.jenislashes.client.dto.UpsertClientRequest;
import com.jenislashes.client.model.ClientRecord;
import com.jenislashes.client.repository.ClientRepository;
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
@DisplayName("ClientService")
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void requireClient_should_throw_when_client_does_not_exist() {
        UUID clientId = UUID.randomUUID();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> clientService.requireClient(clientId));
    }

    @Test
    void createClient_should_trim_optional_fields_and_start_with_zero_stats() {
        var response = clientService.createClient(new UpsertClientRequest(
                "  Maria Perez  ",
                "  5551111  ",
                "   ",
                "  clienta frecuente  "
        ));

        ArgumentCaptor<ClientRecord> recordCaptor = ArgumentCaptor.forClass(ClientRecord.class);
        verify(clientRepository).insert(recordCaptor.capture());

        ClientRecord savedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals("Maria Perez", savedRecord.fullName()),
                () -> assertEquals("5551111", savedRecord.phone()),
                () -> assertNull(savedRecord.whatsapp()),
                () -> assertEquals("clienta frecuente", savedRecord.notes()),
                () -> assertEquals(0, savedRecord.totalAppointments()),
                () -> assertNull(savedRecord.lastVisitAt()),
                () -> assertEquals("Maria Perez", response.fullName())
        );
    }

    @Test
    void updateClient_should_preserve_history_fields_and_trim_values() {
        UUID clientId = UUID.randomUUID();
        OffsetDateTime createdAt = OffsetDateTime.parse("2026-05-01T10:00:00Z");
        OffsetDateTime lastVisitAt = OffsetDateTime.parse("2026-05-10T10:00:00Z");
        ClientRecord existing = new ClientRecord(clientId, "Maria", "1", "2", "old", lastVisitAt, 3, createdAt, createdAt);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existing));

        var response = clientService.updateClient(clientId, new UpsertClientRequest(
                "  Maria Perez  ",
                "   ",
                "  5551111  ",
                "  nota nueva  "
        ));

        ArgumentCaptor<ClientRecord> recordCaptor = ArgumentCaptor.forClass(ClientRecord.class);
        verify(clientRepository).update(recordCaptor.capture());

        ClientRecord updatedRecord = recordCaptor.getValue();

        assertAll(
                () -> assertEquals(clientId, updatedRecord.id()),
                () -> assertEquals(createdAt, updatedRecord.createdAt()),
                () -> assertEquals(lastVisitAt, updatedRecord.lastVisitAt()),
                () -> assertEquals(3, updatedRecord.totalAppointments()),
                () -> assertEquals("Maria Perez", updatedRecord.fullName()),
                () -> assertNull(updatedRecord.phone()),
                () -> assertEquals("5551111", updatedRecord.whatsapp()),
                () -> assertEquals("nota nueva", updatedRecord.notes()),
                () -> assertEquals("Maria Perez", response.fullName())
        );
    }
}
