package com.gigforce.crm.deal;

import com.gigforce.crm.deal.dto.DealRequest;
import com.gigforce.multitenancy.TenantContext;
import com.gigforce.notification.NotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DealService {

    private final DealRepository       repo;
    private final ApplicationEventPublisher events;

    public List<Deal> findAll() { return repo.findAllByTenantId(TenantContext.get()); }

    public Deal findById(UUID id) {
        return repo.findByIdAndTenantId(id, TenantContext.get())
            .orElseThrow(() -> new NoSuchElementException("Deal not found: " + id));
    }

    public Deal create(DealRequest req) {
        Deal d = Deal.builder()
            .title(req.title()).value(req.value())
            .stage(req.stage()).contactId(req.contactId())
            .notes(req.notes()).build();
        d.setTenantId(TenantContext.get());
        Deal saved = repo.save(d);
        publish(saved, "Deal created: " + saved.getTitle());
        return saved;
    }

    public Deal update(UUID id, DealRequest req) {
        Deal d = findById(id);
        d.setTitle(req.title()); d.setValue(req.value()); d.setNotes(req.notes());
        return repo.save(d);
    }

    public Deal updateStage(UUID id, DealStage newStage) {
        Deal d = findById(id);
        DealStage old = d.getStage();
        d.setStage(newStage);
        if (newStage == DealStage.CLOSED_WON || newStage == DealStage.CLOSED_LOST)
            d.setClosedAt(LocalDate.now());
        Deal saved = repo.save(d);
        publish(saved, String.format("Deal '%s' moved: %s -> %s", d.getTitle(), old, newStage));
        return saved;
    }

    public void delete(UUID id) { repo.delete(findById(id)); }

    private void publish(Deal d, String msg) {
        events.publishEvent(new NotificationEvent(this, d.getTenantId(), "DEAL", msg));
    }
}
