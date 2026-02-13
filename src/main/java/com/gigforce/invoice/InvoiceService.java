package com.gigforce.invoice;

import com.gigforce.invoice.dto.InvoiceRequest;
import com.gigforce.multitenancy.TenantContext;
import com.gigforce.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository        repo;
    private final ApplicationEventPublisher events;

    public List<Invoice> findAll() {
        return repo.findAllByTenantIdOrderByCreatedAtDesc(TenantContext.get());
    }

    public Invoice findById(UUID id) {
        return repo.findByIdAndTenantId(id, TenantContext.get())
            .orElseThrow(() -> new NoSuchElementException("Invoice not found: " + id));
    }

    @Transactional
    public Invoice create(InvoiceRequest req) {
        String tid = TenantContext.get();
        String num = String.format("INV-%s-%04d", tid.toUpperCase(), repo.countByTenantId(tid) + 1);

        Invoice inv = Invoice.builder()
            .invoiceNumber(num).clientName(req.clientName()).clientEmail(req.clientEmail())
            .dueDate(req.dueDate()).taxRate(req.taxRate()).status(InvoiceStatus.DRAFT)
            .build();
        inv.setTenantId(tid);

        List<InvoiceItem> items = req.items().stream()
            .map(i -> InvoiceItem.builder().description(i.description())
                .quantity(i.quantity()).unitPrice(i.unitPrice()).invoice(inv).build())
            .toList();

        inv.setItems(items);
        inv.recalculateTotals();
        Invoice saved = repo.save(inv);
        publish(saved, "Invoice created: " + saved.getInvoiceNumber());
        return saved;
    }

    public Invoice markSent(UUID id)    { return transition(id, InvoiceStatus.DRAFT,    InvoiceStatus.SENT,    "Invoice sent"); }
    public Invoice markPaid(UUID id)    { return transition(id, InvoiceStatus.SENT,     InvoiceStatus.PAID,    "Invoice paid"); }

    public Invoice markOverdue(UUID id) {
        Invoice inv = findById(id);
        if (inv.getStatus() == InvoiceStatus.PAID)
            throw new IllegalArgumentException("Cannot mark a paid invoice as overdue");
        inv.setStatus(InvoiceStatus.OVERDUE);
        Invoice saved = repo.save(inv);
        publish(saved, "Invoice overdue: " + saved.getInvoiceNumber());
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        Invoice inv = findById(id);
        if (inv.getStatus() != InvoiceStatus.DRAFT)
            throw new IllegalArgumentException("Only DRAFT invoices can be deleted");
        repo.delete(inv);
    }

    private Invoice transition(UUID id, InvoiceStatus from, InvoiceStatus to, String msg) {
        Invoice inv = findById(id);
        if (inv.getStatus() != from)
            throw new IllegalArgumentException(
                String.format("Invoice must be %s to %s (currently: %s)", from, msg.toLowerCase(), inv.getStatus()));
        inv.setStatus(to);
        Invoice saved = repo.save(inv);
        publish(saved, msg + ": " + saved.getInvoiceNumber());
        return saved;
    }

    private void publish(Invoice inv, String msg) {
        events.publishEvent(new NotificationEvent(this, inv.getTenantId(), "INVOICE", msg));
    }
}
