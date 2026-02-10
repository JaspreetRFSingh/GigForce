package com.gigforce.crm.contact;

import com.gigforce.common.ApiResponse;
import com.gigforce.crm.contact.dto.ContactRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
@Tag(name = "Contacts", description = "CRM contact management (tenant-scoped)")
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    @Operation(summary = "List all contacts for the current tenant")
    public ApiResponse<List<Contact>> list() { return ApiResponse.ok(contactService.findAll()); }

    @GetMapping("/{id}")
    public ApiResponse<Contact> get(@PathVariable UUID id) { return ApiResponse.ok(contactService.findById(id)); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new contact")
    public ApiResponse<Contact> create(@Valid @RequestBody ContactRequest req) {
        return ApiResponse.ok("Contact created", contactService.create(req));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a contact")
    public ApiResponse<Contact> update(@PathVariable UUID id, @Valid @RequestBody ContactRequest req) {
        return ApiResponse.ok("Contact updated", contactService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { contactService.delete(id); }
}
