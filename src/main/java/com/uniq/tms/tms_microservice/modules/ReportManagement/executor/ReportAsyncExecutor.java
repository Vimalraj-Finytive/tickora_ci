package com.uniq.tms.tms_microservice.modules.ReportManagement.executor;

import com.uniq.tms.tms_microservice.modules.ReportManagement.template.AbstractReportGenerator;
import com.uniq.tms.tms_microservice.shared.context.ReportAuthContext;
import com.uniq.tms.tms_microservice.shared.security.schema.TenantContext;
import com.uniq.tms.tms_microservice.shared.util.ExportStatusTracker;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import java.io.File;
import java.time.Duration;

@Component
public class ReportAsyncExecutor {

    @Async
    public <T> void executeAsync(
            AbstractReportGenerator<T, ?> generator,
            String key,
            File file,
            T dto,
            ReportAuthContext context,
            RedisTemplate<String, Object> redis,
            ExportStatusTracker tracker,
            Duration ttl
    ) {
        TenantContext.setCurrentTenant(context.schema());
        try {
            generator.run(key, file, context,dto, redis, tracker, ttl);
        } finally {
            TenantContext.clear();
        }
    }
}
