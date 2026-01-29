package com.uniq.tms.tms_microservice.modules.ReportManagement.strategy.impl;

import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportStatus;
import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportType;
import com.uniq.tms.tms_microservice.modules.ReportManagement.executor.ReportAsyncExecutor;
import com.uniq.tms.tms_microservice.modules.ReportManagement.helper.ExportStatusResolver;
import com.uniq.tms.tms_microservice.modules.ReportManagement.strategy.ReportStrategy;
import com.uniq.tms.tms_microservice.modules.ReportManagement.template.AbstractReportGenerator;
import com.uniq.tms.tms_microservice.modules.ReportManagement.util.ReportUtil;
import com.uniq.tms.tms_microservice.modules.ReportManagement.writer.TimesheetWriter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.TimesheetReportDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.UserTimesheetResponseDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.Timeperiod;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.TimesheetService;
import com.uniq.tms.tms_microservice.shared.context.ReportAuthContext;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import com.uniq.tms.tms_microservice.shared.helper.AuthHelper;
import com.uniq.tms.tms_microservice.shared.util.CacheKeyUtil;
import com.uniq.tms.tms_microservice.shared.util.ExportStatusTracker;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.io.File;
import java.time.Duration;
import java.util.List;

@Component
public class TimesheetReportStrategy
        extends AbstractReportGenerator<TimesheetReportDto, UserTimesheetResponseDto>
        implements ReportStrategy {

    private static final Duration TTL = Duration.ofHours(1);
    private static final Logger log = LogManager.getLogger(TimesheetReportStrategy.class);
    @Value("${csv.download.dir}")
    private String downloadDir;

    @Value("${cache.redis.enabled}")
    private boolean isRedisEnabled;

    private final TimesheetService timesheetService;
    private final TimesheetWriter writer;
    private final CacheKeyUtil cacheKey;
    private final RedisTemplate<String, Object> redis;
    private final ExportStatusTracker tracker;
    private final AuthHelper auth;
    private final ExportStatusResolver statusResolver;
    private final ReportAsyncExecutor reportAsyncExecutor;

    public TimesheetReportStrategy(
            TimesheetService timesheetService,
            TimesheetWriter writer,
            CacheKeyUtil cacheKey,
            @Nullable RedisTemplate<String, Object> redis,
            ExportStatusTracker tracker,
            AuthHelper auth, ExportStatusResolver statusResolver, ReportAsyncExecutor reportAsyncExecutor
    ) {
        this.timesheetService = timesheetService;
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
        return ReportType.TIMESHEET;
    }

    @Override
    public ApiResponse<String> startExport(Object req) {
        TimesheetReportDto dto = (TimesheetReportDto) req;
        String baseName = ReportUtil.build(dto);
        log.info("generate a timesheet filename");
        File file = ReportUtil.generateFileName(downloadDir, baseName, dto.getFormat()).toFile();
        log.info("timesheet filename generated successfully : {}", file.getName());
        String key = cacheKey.getTimesheetExport(
                auth.getSchema(), auth.getOrgId(), file.getName()
        );
        if(isRedisEnabled) {
            assert redis != null;
            redis.opsForValue().set(
                    key, ReportStatus.PENDING.getValues(), TTL
            );
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
    protected List<UserTimesheetResponseDto> fetchData(
            TimesheetReportDto dto,
            ReportAuthContext context
    ) {
        return timesheetService
                .getAllTimesheets(
                        context.userId(),
                        context.orgId(),
                        context.role(),
                        dto
                )
                .getUserTimesheetResponseDtos();
    }

    @Override
    protected void writeCsv(List<UserTimesheetResponseDto> data, File file, TimesheetReportDto dto) throws Exception {
        if (Timeperiod.DAY.name().equalsIgnoreCase(dto.getTimePeriod())) {
            writer.writeDayCsv(data, file);
        } else {
            writer.writeWeekCsv(data, file);
        }
    }

    @Override
    protected void writeXlsx(List<UserTimesheetResponseDto> data, File file, TimesheetReportDto dto) throws Exception {
        if (Timeperiod.DAY.name().equalsIgnoreCase(dto.getTimePeriod())) {
            writer.writeDayXlsx(data, file);
        } else {
            writer.writeWeekXlsx(data, file);
        }
    }

    @Override
    protected boolean isCsv(TimesheetReportDto dto) {
        return "csv".equalsIgnoreCase(dto.getFormat());
    }

    @Override
    public ApiResponse<String> checkStatus(String exportId, ReportType type) {

        String redisKey = cacheKey.getExportKey(
                type,
                auth.getSchema(),
                auth.getOrgId(),
                exportId
        );

        File file = new File(downloadDir + exportId);

        String status = statusResolver.resolve(
                redisKey,
                file,
                redis,
                tracker
        );

        return new ApiResponse<>(200, "Status fetched", status);
    }
}
