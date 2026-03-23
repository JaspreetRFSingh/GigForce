package com.gigforce.analytics;

import com.gigforce.analytics.dto.DashboardStats;
import com.gigforce.crm.contact.ContactRepository;
import com.gigforce.crm.deal.DealRepository;
import com.gigforce.crm.deal.DealStage;
import com.gigforce.invoice.InvoiceRepository;
import com.gigforce.invoice.InvoiceStatus;
import com.gigforce.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ContactRepository contactRepo;
    private final DealRepository    dealRepo;
    private final InvoiceRepository invoiceRepo;

    @Cacheable(value = "dashboard",
               key = "#root.methodName + '_' + T(com.gigforce.multitenancy.TenantContext).get()")
    public DashboardStats getDashboardStats() {
        String tid = TenantContext.get();

        // Single GROUP BY query instead of one query per stage
        Map<DealStage, Long> byStage = Arrays.stream(DealStage.values())
            .collect(Collectors.toMap(s -> s, s -> 0L));
        dealRepo.countGroupedByStage(tid)
            .forEach(row -> byStage.put((DealStage) row[0], (Long) row[1]));

        long openDeals = byStage.entrySet().stream()
            .filter(e -> e.getKey() != DealStage.CLOSED_WON && e.getKey() != DealStage.CLOSED_LOST)
            .mapToLong(Map.Entry::getValue).sum();

        return DashboardStats.builder()
            .totalContacts(contactRepo.countByTenantId(tid))
            .totalDeals(byStage.values().stream().mapToLong(Long::longValue).sum())
            .openDeals(openDeals)
            .wonRevenue(dealRepo.sumWonValueByTenantId(tid, DealStage.CLOSED_WON))
            .totalInvoices(invoiceRepo.countByTenantId(tid))
            .paidInvoices(invoiceRepo.countByTenantIdAndStatus(tid, InvoiceStatus.PAID))
            .overdueInvoices(invoiceRepo.countByTenantIdAndStatus(tid, InvoiceStatus.OVERDUE))
            .paidRevenue(invoiceRepo.sumPaidTotalByTenantId(tid, InvoiceStatus.PAID))
            .dealsByStage(byStage)
            .build();
    }
}
