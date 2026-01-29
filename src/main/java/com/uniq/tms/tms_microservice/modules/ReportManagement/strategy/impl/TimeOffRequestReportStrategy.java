package com.uniq.tms.tms_microservice.modules.ReportManagement.strategy.impl;

import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportStatus;
import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportType;
import com.uniq.tms.tms_microservice.modules.ReportManagement.executor.ReportAsyncExecutor;
import com.uniq.tms.tms_microservice.modules.ReportManagement.helper.ExportStatusResolver;
import com.uniq.tms.tms_microservice.modules.ReportManagement.strategy.ReportStrategy;
import com.uniq.tms.tms_microservice.modules.ReportManagement.template.AbstractReportGenerator;
import com.uniq.tms.tms_microservice.modules.ReportManagement.util.ReportUtil;
import com.uniq.tms.tms_microservice.modules.ReportManagement.writer.TimeOffRequestWriter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.TimeOffExportRequestDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeOffExportRequest;
import com.uniq.tms.tms_microservice.modules.leavemanagement.projection.TimeOffExportView;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl.TimeOffRequestServiceImpl;
import com.uniq.tms.tms_microservice.shared.context.ReportAuthContext;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.shared.util.ExportStatusTracker;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Duration;
import java.util.List;

@Component
public class TimeOffRequestReportStrategy
        extends AbstractReportGenerator<TimeOffExportRequestDto, TimeOffExportView>
        implements ReportStrategy {

    private static final Duration TTL = Duration.ofHours(1);

    @Value("${csv.request.download.dir}")
    private String downloadDir;

    @Value("${cache.redis.enabled}")
    private boolean isRedisEnabled;

    private final TimeOffRequestServiceImpl service;
    private final TimeOffPolicyDtoMapper mapper;
    private final TimeOffRequestWriter writer;
    private final CacheKeyUtil cacheKey;
    private final RedisTemplate<String, Object> redis;
    private final ExportStatusTracker tracker;
    private final AuthHelper auth;
    private final ExportStatusResolver statusResolver;
    private final ReportAsyncExecutor reportAsyncExecutor;

    public TimeOffRequestReportStrategy(
            TimeOffRequestServiceImpl service,
            TimeOffPolicyDtoMapper mapper,
            TimeOffRequestWriter writer,
            CacheKeyUtil cacheKey,
            @Nullable RedisTemplate<String, Object> redis,
            ExportStatusTracker tracker,
            AuthHelper auth,
            ExportStatusResolver statusResolver, ReportAsyncExecutor reportAsyncExecutor
    ) {
        this.service = service;
        this.mapper = mapper;
        this.writer = writer;
        this.cacheKey = cacheKey;
        this.redis = redis;
        this.tracker = tracker;
        this.auth = auth;
        this.statusResolver = statusResolver;
        this.reportAsyncExecutor = reportAsyncExecutor;
    }

    @Override
    public ReportType getType() {
        return ReportType.TIMEOFF_REQUEST;
    }

    @Override
    public ApiResponse<String> startExport(Object req) {

        TimeOffExportRequestDto dto = (TimeOffExportRequestDto) req;

        String baseName = "TimeOffRequest_" + dto.getFromDate();

        File file = ReportUtil.generateFileName(
                downloadDir,
                baseName,
                dto.getFormat()
        ).toFile();

        String key = cacheKey.getExport(
                auth.getSchema(),
                auth.getOrgId(),
                file.getName()
        );
        if(isRedisEnabled) {
            assert redis != null;
            redis.opsForValue().set(key, ReportStatus.PENDING.getValues(), TTL);
        }
        ReportAuthContext context = new ReportAuthContext(
                auth.getUserId(),
                auth.getOrgId(),
                auth.getRole(),
                auth.getSchema()
        );

        reportAsyncExecutor.executeAsync(
                this,
                key,
                file,
                dto,
                context,
                redis,
                tracker,
                TTL
        );

        return new ApiResponse<>(202, "Export started", file.getName());
    }

    @Override
    protected List<TimeOffExportView> fetchData(TimeOffExportRequestDto dto,ReportAuthContext context) {
        TimeOffExportRequest model = mapper.toModel(dto);
        return service.fetchExportRows(model, context.userId());
    }

    @Override
    protected void writeCsv(List<TimeOffExportView> data, File file, TimeOffExportRequestDto dto)
            throws Exception {
        writer.writeCsv(data, file);
    }

    @Override
    protected void writeXlsx(List<TimeOffExportView> data, File file, TimeOffExportRequestDto dto)
            throws Exception {
        writer.writeXlsx(data, file);
    }

    @Override
    protected boolean isCsv(TimeOffExportRequestDto dto) {
        return "csv".equalsIgnoreCase(dto.getFormat());
    }

    @Override
    public ApiResponse<String> checkStatus(String exportId, ReportType type) {

        String key = cacheKey.getExportKey(
                type,
                auth.getSchema(),
                auth.getOrgId(),
                exportId
        );

        File file = new File(downloadDir + exportId);

        String status = statusResolver.resolve(key, file, redis, tracker);

        return new ApiResponse<>(200, "Status fetched", status);
    }
}
