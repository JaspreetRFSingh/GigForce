package com.gigforce.analytics.dto;

import com.gigforce.crm.deal.DealStage;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter @Builder
public class DashboardStats {
    private long                  totalContacts;
    private long                  totalDeals;
    private long                  openDeals;
    private BigDecimal            wonRevenue;
    private long                  totalInvoices;
    private long                  paidInvoices;
    private long                  overdueInvoices;
    private BigDecimal            paidRevenue;
    private Map<DealStage, Long>  dealsByStage;
}
