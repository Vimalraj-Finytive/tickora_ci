package com.uniq.tms.tms_microservice.shared.util;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.TimesheetReportDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReportUtil {

    private static final Logger log = LogManager.getLogger(ReportUtil.class);

    public static String generateFileName(TimesheetReportDto request, String downloadDir) {
        String format = request.getFormat();
        String timePeriod = request.getTimePeriod();
        LocalDate startDate = request.getFromDate();
        LocalDate endDate = request.getToDate();

        // Build base filename
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String datePart = (timePeriod == null || timePeriod.trim().isEmpty()) ?
                "Timesheet_" + startDate.format(formatter) + "_to_" + endDate.format(formatter) :
                timePeriod.equalsIgnoreCase("DAY") ? "Timesheet_" + startDate.format(formatter) :
                        timePeriod.equalsIgnoreCase("WEEK") ? "Timesheet_Weekly" :
                                timePeriod.equalsIgnoreCase("MONTH") ? "Timesheet_Monthly" :
                                        timePeriod.equalsIgnoreCase("YEAR") ? "Timesheet_Yearly" :
                                                "Invalid or missing date range for time period";

        String baseName = "timesheetReport_" + datePart;
        String extension = "csv".equalsIgnoreCase(format) ? ".csv" : ".xlsx";

        // Handle filename collisions
        Path resourcePath = Paths.get(downloadDir);
        String fileName = baseName + extension;
        Path filePath = resourcePath.resolve(fileName);
        int counter = 1;

        try {
            if (!Files.exists(resourcePath)) {
                Files.createDirectories(resourcePath);
            }

            while (Files.exists(filePath)) {
                fileName = baseName + "(" + counter + ")" + extension;
                filePath = resourcePath.resolve(fileName);
                counter++;
            }
        } catch (IOException e) {
            log.error("Error checking file existence: ", e);
            fileName = baseName + "_" + System.currentTimeMillis() + extension;
        }

        return fileName;
    }

}
