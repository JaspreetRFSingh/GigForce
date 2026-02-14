package com.gigforce;

import com.gigforce.auth.model.User;
import com.gigforce.auth.model.UserRole;
import com.gigforce.auth.repository.UserRepository;
import com.gigforce.crm.contact.Contact;
import com.gigforce.crm.contact.ContactRepository;
import com.gigforce.crm.deal.Deal;
import com.gigforce.crm.deal.DealRepository;
import com.gigforce.crm.deal.DealStage;
import com.gigforce.invoice.Invoice;
import com.gigforce.invoice.InvoiceItem;
import com.gigforce.invoice.InvoiceRepository;
import com.gigforce.invoice.InvoiceStatus;
import com.gigforce.tenant.Tenant;
import com.gigforce.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TenantRepository  tenantRepo;
    private final UserRepository    userRepo;
    private final ContactRepository contactRepo;
    private final DealRepository    dealRepo;
    private final InvoiceRepository invoiceRepo;
    private final PasswordEncoder   passwordEncoder;

    @Override
    public void run(String... args) {
        if (tenantRepo.count() > 0) return;

        log.info("=== Seeding demo data ===");

        // Tenants
        tenantRepo.save(Tenant.builder().id("acme").name("Acme Corp").plan("PRO").active(true).build());
        tenantRepo.save(Tenant.builder().id("solo").name("Solo Ventures").plan("STARTER").active(true).build());

        String pw = passwordEncoder.encode("password123");

        // Users
        userRepo.save(User.builder().name("Jaspreet Singh").email("admin@acme.com")
            .password(pw).tenantId("acme").role(UserRole.OWNER).build());
        userRepo.save(User.builder().name("Solo Admin").email("admin@solo.com")
            .password(pw).tenantId("solo").role(UserRole.OWNER).build());

        // Contacts (acme)
        Contact alice = contactRepo.save(Contact.builder()
            .name("Alice Sharma").email("alice@clientcorp.com")
            .phone("+91-9876543210").company("Client Corp")
            .notes("Met at DevFest 2024").tenantId("acme").build());

        Contact bob = contactRepo.save(Contact.builder()
            .name("Bob Kumar").email("bob@techstart.in")
            .phone("+91-9812345678").company("TechStart")
            .notes("Referred by Alice").tenantId("acme").build());

        // Deals (acme)
        dealRepo.save(Deal.builder().title("API Integration Project")
            .value(new BigDecimal("150000")).stage(DealStage.PROPOSAL)
            .contactId(alice.getId()).tenantId("acme").build());

        dealRepo.save(Deal.builder().title("Mobile App Development")
            .value(new BigDecimal("300000")).stage(DealStage.NEGOTIATION)
            .contactId(bob.getId()).tenantId("acme").build());

        dealRepo.save(Deal.builder().title("Cloud Migration")
            .value(new BigDecimal("500000")).stage(DealStage.CLOSED_WON)
            .contactId(alice.getId()).tenantId("acme")
            .closedAt(LocalDate.now().minusDays(10)).build());

        dealRepo.save(Deal.builder().title("Legacy Refactor")
            .value(new BigDecimal("80000")).stage(DealStage.QUALIFIED)
            .contactId(bob.getId()).tenantId("acme").build());

        // Invoice (acme)
        Invoice inv = Invoice.builder()
            .invoiceNumber("INV-ACME-0001")
            .clientName("Client Corp").clientEmail("billing@clientcorp.com")
            .taxRate(new BigDecimal("18"))
            .dueDate(LocalDate.now().plusDays(30))
            .status(InvoiceStatus.SENT).tenantId("acme").build();

        InvoiceItem item = InvoiceItem.builder()
            .description("API Integration — 20 days")
            .quantity(20).unitPrice(new BigDecimal("5000")).invoice(inv).build();

        inv.setItems(List.of(item));
        inv.recalculateTotals();
        invoiceRepo.save(inv);

        log.info("=== Seed complete ===");
        log.info("  Tenant: acme  |  Login: admin@acme.com / password123");
        log.info("  Tenant: solo  |  Login: admin@solo.com / password123");
        log.info("  Swagger UI  -> http://localhost:8080/swagger-ui.html");
        log.info("  H2 Console  -> http://localhost:8080/h2-console");
    }
}
