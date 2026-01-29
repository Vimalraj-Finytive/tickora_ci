package com.uniq.tms.tms_microservice.modules.ReportManagement.strategy.impl;

import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportStatus;
import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportType;
import com.uniq.tms.tms_microservice.modules.ReportManagement.executor.ReportAsyncExecutor;
import com.uniq.tms.tms_microservice.modules.ReportManagement.helper.ExportStatusResolver;
import com.uniq.tms.tms_microservice.modules.ReportManagement.strategy.ReportStrategy;
import com.uniq.tms.tms_microservice.modules.ReportManagement.template.AbstractReportGenerator;
import com.uniq.tms.tms_microservice.modules.ReportManagement.util.ReportUtil;
import com.uniq.tms.tms_microservice.modules.ReportManagement.writer.PayrollWriter;
import com.uniq.tms.tms_microservice.modules.payrollManagement.adapter.PayRollAdapter;
import com.uniq.tms.tms_microservice.modules.payrollManagement.dto.PayRollExportDto;
import com.uniq.tms.tms_microservice.modules.payrollManagement.projection.UserPayRollAmount;
import com.uniq.tms.tms_microservice.modules.userManagement.adapter.UserAdapter;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PayRollReportStrategy
        extends AbstractReportGenerator<PayRollExportDto, UserPayRollAmount>
        implements ReportStrategy {

    private static final Duration TTL = Duration.ofHours(1);

    @Value("${csv.payroll.download.dir}")
    private String downloadDir;

    @Value("${cache.redis.enabled}")
    private boolean isRedisEnabled;

    private final PayRollAdapter adapter;
    private final PayrollWriter writer;
    private final CacheKeyUtil cacheKey;
    private final RedisTemplate<String, Object> redis;
    private final ExportStatusTracker tracker;
    private final AuthHelper auth;
    private final ExportStatusResolver statusResolver;
    private final UserAdapter userAdapter;
    private final ReportAsyncExecutor reportAsyncExecutor;

    public PayRollReportStrategy(
            PayRollAdapter adapter,
            PayrollWriter writer,
            CacheKeyUtil cacheKey,
            @Nullable RedisTemplate<String, Object> redis,
            ExportStatusTracker tracker,
            AuthHelper auth,
            ExportStatusResolver statusResolver, UserAdapter userAdapter, ReportAsyncExecutor reportAsyncExecutor
    ) {
        this.adapter = adapter;
        this.writer = writer;
        this.cacheKey = cacheKey;
        this.redis = redis;
        this.tracker = tracker;
        this.auth = auth;
        this.statusResolver = statusResolver;
        this.userAdapter = userAdapter;
        this.reportAsyncExecutor = reportAsyncExecutor;
    }

    @Override
    public ReportType getType() {
        return ReportType.PAYROLL;
    }

    @Override
    public ApiResponse<String> startExport(Object req) {

        PayRollExportDto dto = (PayRollExportDto) req;

        String baseName = "PayRoll_" + dto.getMonth();

        File file = ReportUtil.generateFileName(downloadDir,baseName, dto
                .getFormat()).toFile();

        String redisKey = cacheKey.getPayRollExport(
                auth.getSchema(),
                auth.getOrgId(),
                file.getName()
        );
        if(isRedisEnabled) {
            assert redis != null;
            redis.opsForValue().set(redisKey, ReportStatus.PENDING.getValues(), TTL);
        }
        ReportAuthContext context = new ReportAuthContext(
                auth.getUserId(),
                auth.getOrgId(),
                auth.getRole(),
                auth.getSchema()
        );

        reportAsyncExecutor.executeAsync(
                this,
                redisKey,
                file,
                dto,
                context,
                redis,
                tracker,
                TTL
        );

        return new ApiResponse<>(202, "Export started", file.getName());
    }

    private List<String> resolveEligibleUsers(
            List<String> userIds,
            List<Long> groupIds
    ){
        Set<String> eligibleUsers = new HashSet<>();
        if(groupIds != null && !groupIds.isEmpty()){
            eligibleUsers.add(String.valueOf(userAdapter.findUserIdsByGroupIds(groupIds)));
        }
        if (userIds != null && !userIds.isEmpty()) {
            eligibleUsers.addAll(userIds);
        }
        return new ArrayList<>(eligibleUsers);
    }

    @Override
    protected List<UserPayRollAmount> fetchData(PayRollExportDto dto,ReportAuthContext context) {
        List<String> eligibleUsers = resolveEligibleUsers(dto.getUserIds(), dto.getGroupIds());
        if(eligibleUsers.isEmpty()){
            return adapter.findAllByMonth(dto.getMonth());
        }
        return adapter.findAllByMonthAndUserIds(dto.getMonth(),eligibleUsers);
    }

    @Override
    protected void writeCsv(List<UserPayRollAmount> data, File file, PayRollExportDto dto) throws Exception {
        writer.writeCsv(data, file);
    }

    @Override
    protected void writeXlsx(List<UserPayRollAmount> data, File file, PayRollExportDto dto) throws Exception {
        writer.writeXlsx(data, file);
    }

    @Override
    protected boolean isCsv(PayRollExportDto dto) {
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
