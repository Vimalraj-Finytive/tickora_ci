package com.uniq.tms.tms_microservice.modules.ReportManagement.helper;

import com.uniq.tms.tms_microservice.modules.ReportManagement.enums.ReportType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ReportDownloadResolver {

    @Value("${csv.download.dir}")
    private String timesheetDir;

    @Value("${csv.payroll.download.dir}")
    private String payrollDir;

    @Value("${csv.request.download.dir}")
    private String timeoffDir;

    public Path resolve(ReportType type, String fileName) {

        String baseDir = switch (type) {
            case TIMESHEET -> timesheetDir;
            case PAYROLL -> payrollDir;
            case TIMEOFF_REQUEST -> timeoffDir;
        };

        return Paths.get(baseDir).resolve(fileName);
    }
}
