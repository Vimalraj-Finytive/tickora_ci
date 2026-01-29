package com.uniq.tms.tms_microservice.modules.ReportManagement.template;

import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportStatus;
import com.uniq.tms.tms_microservice.shared.context.ReportAuthContext;
import com.uniq.tms.tms_microservice.shared.util.ExportStatusTracker;
import org.springframework.data.redis.core.RedisTemplate;
import java.io.File;
import java.time.Duration;
import java.util.List;

public abstract class AbstractReportGenerator<T, R> {

    protected abstract List<R> fetchData(
            T request,
            ReportAuthContext context
    );
    protected abstract void writeCsv(List<R> data, File file, T dto) throws Exception;
    protected abstract void writeXlsx(List<R> data, File file, T dto) throws Exception;
    protected abstract boolean isCsv(T request);

    protected final void execute(
            String exportKey,
            File file,
            ReportAuthContext context,
            T request,
            RedisTemplate<String, Object> redis,
            ExportStatusTracker tracker,
            Duration ttl
    ) {
        try {
            update(exportKey, file, ReportStatus.PROCESSING, redis, tracker, ttl);
            List<R> data = fetchData(request, context);
            if (isCsv(request)) writeCsv(data, file, request);
            else writeXlsx(data, file, request);
            update(exportKey, file, ReportStatus.COMPLETED, redis, tracker, ttl);
        } catch (Exception e) {
            if (file.exists()) file.delete();
            update(exportKey, file, ReportStatus.FAILED, redis, tracker, ttl);
            throw new RuntimeException(e);
        }
    }

    public final void run(
            String key,
            File file,
            ReportAuthContext context,
            T dto,
            RedisTemplate<String, Object> redis,
            ExportStatusTracker tracker,
            Duration ttl
    ) {
        execute(key, file, context,dto, redis, tracker, ttl);
    }

    private void update(
            String key,
            File file,
            ReportStatus status,
            RedisTemplate<String, Object> redis,
            ExportStatusTracker tracker,
            Duration ttl
    ) {
        if (redis != null)
            redis.opsForValue().set(key, status.getValues(), ttl);
        else
            tracker.writeStatus(file, status.getValues());
    }
}
