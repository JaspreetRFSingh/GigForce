package com.gigforce.crm;

import com.gigforce.crm.contact.Contact;
import com.gigforce.crm.contact.ContactRepository;
import com.gigforce.crm.contact.ContactService;
import com.gigforce.crm.contact.dto.ContactRequest;
import com.gigforce.multitenancy.TenantContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactService")
class ContactServiceTest {

    @Mock ContactRepository repo;
    @InjectMocks ContactService service;

    @BeforeEach void setTenant()   { TenantContext.set("acme"); }
    @AfterEach  void clearTenant() { TenantContext.clear(); }

    @Test
    @DisplayName("findAll: returns only current tenant's contacts")
    void findAll_scopedToTenant() {
        when(repo.findAllByTenantId("acme")).thenReturn(List.of(
            Contact.builder().name("Alice").build(),
            Contact.builder().name("Bob").build()
        ));
        assertThat(service.findAll()).hasSize(2);
        verify(repo, never()).findAll();
    }

    @Test
    @DisplayName("create: saves with tenantId set from context")
    void create_setsTenantId() {
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        Contact saved = service.create(new ContactRequest("Charlie", "c@test.com", null, "Acme", null));

        assertThat(saved.getTenantId()).isEqualTo("acme");
        assertThat(saved.getName()).isEqualTo("Charlie");
    }

    @Test
    @DisplayName("findById: cross-tenant access returns 404")
    void findById_wrongTenant_throws() {
        UUID id = UUID.randomUUID();
        when(repo.findByIdAndTenantId(id, "acme")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(id))
            .isInstanceOf(NoSuchElementException.class);
    }

    @Test
    @DisplayName("delete: delegates to repository")
    void delete_callsRepo() {
        UUID id = UUID.randomUUID();
        Contact c = Contact.builder().name("To Delete").build();
        c.setTenantId("acme");
        when(repo.findByIdAndTenantId(id, "acme")).thenReturn(Optional.of(c));

        service.delete(id);
        verify(repo).delete(c);
    }
}
