package com.gigforce.invoice;

import com.gigforce.invoice.dto.InvoiceItemRequest;
import com.gigforce.invoice.dto.InvoiceRequest;
import com.gigforce.multitenancy.TenantContext;
import com.gigforce.notification.NotificationEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceService")
class InvoiceServiceTest {

    @Mock InvoiceRepository        repo;
    @Mock ApplicationEventPublisher events;
    @InjectMocks InvoiceService    service;

    @BeforeEach void setTenant()   { TenantContext.set("acme"); }
    @AfterEach  void clearTenant() { TenantContext.clear(); }

    @Test
    @DisplayName("create: computes totals correctly with tax")
    void create_computesTotals() {
        when(repo.countByTenantId("acme")).thenReturn(0L);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var req = new InvoiceRequest("Client Corp", "bill@corp.com", null,
            new BigDecimal("10"),
            List.of(new InvoiceItemRequest("Dev work", 10, new BigDecimal("1000"))));

        Invoice inv = service.create(req);

        assertThat(inv.getStatus()).isEqualTo(InvoiceStatus.DRAFT);
        assertThat(inv.getSubtotal()).isEqualByComparingTo("10000");
        assertThat(inv.getTaxAmount()).isEqualByComparingTo("1000.00");
        assertThat(inv.getTotal()).isEqualByComparingTo("11000.00");
        assertThat(inv.getInvoiceNumber()).isEqualTo("INV-ACME-0001");
        verify(events).publishEvent(any(NotificationEvent.class));
    }

    @Test
    @DisplayName("create: zero tax rate gives no tax amount")
    void create_zeroTax() {
        when(repo.countByTenantId("acme")).thenReturn(5L);
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        var req = new InvoiceRequest("Solo", null, null,
            BigDecimal.ZERO,
            List.of(new InvoiceItemRequest("Consulting", 5, new BigDecimal("2000"))));

        Invoice inv = service.create(req);

        assertThat(inv.getTaxAmount()).isEqualByComparingTo("0.00");
        assertThat(inv.getTotal()).isEqualByComparingTo("10000.00");
    }

    @Test
    @DisplayName("markPaid: requires SENT status first")
    void markPaid_fromDraft_throws() {
        UUID id = UUID.randomUUID();
        Invoice draft = Invoice.builder().status(InvoiceStatus.DRAFT).build();
        draft.setTenantId("acme");
        when(repo.findByIdAndTenantId(id, "acme")).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> service.markPaid(id))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("SENT");
    }

    @Test
    @DisplayName("delete: only DRAFT invoices can be deleted")
    void delete_sentInvoice_throws() {
        UUID id = UUID.randomUUID();
        Invoice sent = Invoice.builder().status(InvoiceStatus.SENT).build();
        sent.setTenantId("acme");
        when(repo.findByIdAndTenantId(id, "acme")).thenReturn(Optional.of(sent));

        assertThatThrownBy(() -> service.delete(id))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("DRAFT");
    }

    @Test
    @DisplayName("markOverdue: cannot mark PAID invoice as overdue")
    void markOverdue_paidInvoice_throws() {
        UUID id = UUID.randomUUID();
        Invoice paid = Invoice.builder().status(InvoiceStatus.PAID).build();
        paid.setTenantId("acme");
        when(repo.findByIdAndTenantId(id, "acme")).thenReturn(Optional.of(paid));

        assertThatThrownBy(() -> service.markOverdue(id))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("paid");
    }
}
