package com.gigforce.multitenancy;

/**
 * ThreadLocal store for the current request's tenant identifier.
 *
 * Set by TenantFilter at the start of each request, cleared in a finally
 * block after the response is sent — ensuring no tenant context leaks
 * across threads in a thread-pool environment.
 *
 * Production extension: swap the ThreadLocal for a DataSourceRouter
 * to enable true schema-per-tenant isolation on Azure PostgreSQL.
 */
public final class TenantContext {

    private static final ThreadLocal<String> CURRENT = new InheritableThreadLocal<>();

    private TenantContext() {}

    public static void set(String tenantId)  { CURRENT.set(tenantId); }
    public static String  get()              { return CURRENT.get(); }
    public static void    clear()            { CURRENT.remove(); }
}
