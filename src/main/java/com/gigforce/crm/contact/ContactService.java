package com.gigforce.crm.contact;

import com.gigforce.crm.contact.dto.ContactRequest;
import com.gigforce.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository repo;

    public List<Contact> findAll() {
        return repo.findAllByTenantId(TenantContext.get());
    }

    public Contact findById(UUID id) {
        return repo.findByIdAndTenantId(id, TenantContext.get())
            .orElseThrow(() -> new NoSuchElementException("Contact not found: " + id));
    }

    public Contact create(ContactRequest req) {
        Contact c = Contact.builder()
            .name(req.name()).email(req.email())
            .phone(req.phone()).company(req.company())
            .notes(req.notes()).build();
        c.setTenantId(TenantContext.get());
        return repo.save(c);
    }

    public Contact update(UUID id, ContactRequest req) {
        Contact c = findById(id);
        c.setName(req.name()); c.setEmail(req.email());
        c.setPhone(req.phone()); c.setCompany(req.company());
        c.setNotes(req.notes());
        return repo.save(c);
    }

    public void delete(UUID id) { repo.delete(findById(id)); }
}
