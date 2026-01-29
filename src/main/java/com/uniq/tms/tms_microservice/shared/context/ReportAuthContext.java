package com.uniq.tms.tms_microservice.shared.context;

public record ReportAuthContext(
        String userId,
        String orgId,
        String role,
        String schema
) {}
