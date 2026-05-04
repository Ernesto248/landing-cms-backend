package com.jenislashes.client.controller;

import com.jenislashes.client.dto.ClientResponse;
import com.jenislashes.client.dto.UpsertClientRequest;
import com.jenislashes.client.service.ClientService;
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
@RequestMapping("/api/v1/admin/clients")
public class AdminClientController {

    private final ClientService clientService;

    public AdminClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> list() {
        return ResponseEntity.ok(clientService.listClients());
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody UpsertClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.createClient(request));
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ClientResponse> update(
            @PathVariable UUID clientId,
            @Valid @RequestBody UpsertClientRequest request
    ) {
        return ResponseEntity.ok(clientService.updateClient(clientId, request));
    }
}
