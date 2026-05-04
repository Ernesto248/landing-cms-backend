package com.jenislashes.client.service;

import com.jenislashes.client.dto.ClientResponse;
import com.jenislashes.client.dto.UpsertClientRequest;
import com.jenislashes.client.model.ClientRecord;
import com.jenislashes.client.repository.ClientRepository;
import com.jenislashes.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<ClientResponse> listClients() {
        return clientRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ClientRecord requireClient(UUID clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));
    }

    @Transactional
    public ClientResponse createClient(UpsertClientRequest request) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        ClientRecord clientRecord = new ClientRecord(
                UUID.randomUUID(),
                request.fullName().trim(),
                normalizeNullable(request.phone()),
                normalizeNullable(request.whatsapp()),
                normalizeNullable(request.notes()),
                null,
                0,
                now,
                now
        );
        clientRepository.insert(clientRecord);
        return toResponse(clientRecord);
    }

    @Transactional
    public ClientResponse updateClient(UUID clientId, UpsertClientRequest request) {
        ClientRecord existing = requireClient(clientId);
        ClientRecord updated = new ClientRecord(
                existing.id(),
                request.fullName().trim(),
                normalizeNullable(request.phone()),
                normalizeNullable(request.whatsapp()),
                normalizeNullable(request.notes()),
                existing.lastVisitAt(),
                existing.totalAppointments(),
                existing.createdAt(),
                OffsetDateTime.now(ZoneOffset.UTC)
        );
        clientRepository.update(updated);
        return toResponse(updated);
    }

    public void refreshAppointmentStats(UUID clientId) {
        clientRepository.refreshAppointmentStats(clientId);
    }

    private ClientResponse toResponse(ClientRecord clientRecord) {
        return new ClientResponse(
                clientRecord.id(),
                clientRecord.fullName(),
                clientRecord.phone(),
                clientRecord.whatsapp(),
                clientRecord.notes(),
                clientRecord.lastVisitAt(),
                clientRecord.totalAppointments(),
                clientRecord.createdAt(),
                clientRecord.updatedAt()
        );
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
