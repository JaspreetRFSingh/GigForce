package com.gigforce.tenant;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TenantService {

    private final TenantRepository repo;

    public List<Tenant> findAll()       { return repo.findAll(); }

    public Tenant findById(String id)   {
        return repo.findById(id).orElseThrow(() -> new NoSuchElementException("Tenant not found: " + id));
    }

    public Tenant create(Tenant t) {
        if (repo.existsById(t.getId())) throw new IllegalArgumentException("Tenant already exists: " + t.getId());
        return repo.save(t);
    }

    public Tenant update(String id, Tenant u) {
        Tenant t = findById(id);
        t.setName(u.getName()); t.setPlan(u.getPlan()); t.setActive(u.isActive());
        return repo.save(t);
    }
}
