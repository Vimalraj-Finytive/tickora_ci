package com.uniq.tms.tms_microservice.modules.ReportManagement.util;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.TimesheetReportDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

@Component
public final class ReportUtil {

    private static final Logger log = LogManager.getLogger(ReportUtil.class);

    private ReportUtil() {}

    public static String build(TimesheetReportDto dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return switch (dto.getTimePeriod().toUpperCase()) {
            case "DAY" -> "Timesheet_" + dto.getFromDate().format(formatter);
            case "WEEK" -> "Timesheet_Weekly";
            case "MONTH" -> "Timesheet_Monthly";
            default -> "Timesheet_" +
                    dto.getFromDate().format(formatter) +
                    "_to_" +
                    dto.getToDate().format(formatter);
        };
    }

    public static Path generateFileName(
            String downloadDir,
            String baseName,
            String format
    ) {
        String extension = "csv".equalsIgnoreCase(format) ? ".csv" : ".xlsx";

        Path dir = Paths.get(downloadDir);
        String fileName = baseName + extension;
        Path filePath = dir.resolve(fileName);

        int counter = 1;

        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            while (Files.exists(filePath)) {
                fileName = baseName + "(" + counter + ")" + extension;
                filePath = dir.resolve(fileName);
                counter++;
            }
        } catch (IOException e) {
            log.error("File collision handling failed", e);
            fileName = baseName + "_" + System.currentTimeMillis() + extension;
            filePath = dir.resolve(fileName);
        }

        return filePath;
    }
}
