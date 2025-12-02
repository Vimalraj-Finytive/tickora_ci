package com.uniq.tms.tms_microservice.modules.timesheetManagement.services.impl;

import com.opencsv.CSVWriter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.FileExportResponseDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.TimesheetReportDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.dto.UserTimesheetResponseDto;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.enums.Timeperiod;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.ReportService;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.services.TimesheetService;
import com.uniq.tms.tms_microservice.modules.userManagement.services.UserService;
import com.uniq.tms.tms_microservice.shared.util.ReportStyleUtil;
import io.jsonwebtoken.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final Logger log = LogManager.getLogger(ReportServiceImpl.class);

    private final TimesheetService timesheetService;
    private final UserService userService;
    private final ReportStyleUtil reportStyleUtil;
    
    public ReportServiceImpl(TimesheetService timesheetService, UserService userService, ReportStyleUtil reportStyleUtil) {
        this.timesheetService = timesheetService;
        this.userService = userService;
        this.reportStyleUtil = reportStyleUtil;
    }

    @Value("${csv.download.dir}")
    private String downloadDir;

    /**
     *
     * @param timesheet
     * @return timesheet report based on selected day timeperiod
     */
    public byte[] exportTimesheetDayCsv(List<UserTimesheetResponseDto> timesheet, String sheetName) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out , StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(outputStreamWriter, CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {
            ClassPathResource resource = new ClassPathResource("templates/text/timesheet_csv_header.txt");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String headerLine = reader.readLine();
                if(headerLine != null){
                    csvWriter.writeNext(headerLine.split(","));
                }
            }
            int serialNumber = 1;
            for (UserTimesheetResponseDto resultDto : timesheet) {
                List<TimesheetDto> timesheets = resultDto.getTimesheets();
                if (timesheets != null) {
                    for (TimesheetDto dto : timesheets) {
                        csvWriter.writeNext(new String[]{
                                String.valueOf(serialNumber++),
                                dto.getUserName(),
                                dto.getMobileNumber(),
                                dto.getGroupName(),
                                dto.getStatus(),
                                dto.getFirstClockInTime(),
                                dto.getLastClockOutTime(),
                                dto.getRegularHoursDuration()
                        });
                    }
                }
            }
            csvWriter.flush();
            return out.toByteArray();
        } catch (IOException | java.io.IOException e) {
            throw new RuntimeException("CSV export failed", e);
        }
    }

    public byte[] exportTimesheetDayXlsx(List<UserTimesheetResponseDto> timesheet) {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Timesheets");
                ClassPathResource resource = new ClassPathResource("templates/text/timesheet_excel_header.txt");
                List<String> headerLines;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    headerLines = reader.lines().toList();
                }
                String[] headers = headerLines.get(0).split("\\|");
                CellStyle headerCellStyle = workbook.createCellStyle();
                headerCellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerCellStyle.setFont(headerFont);
                headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
                headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                reportStyleUtil.setThinBorder(headerCellStyle);
                CellStyle dataCellStyle = workbook.createCellStyle();
                dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
                dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                reportStyleUtil.setThinBorder(dataCellStyle);
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    reportStyleUtil.createStyledCell(headerRow, i, " " + headers[i] + " ", headerCellStyle);
                }
                int rowIdx = 1;
                int serialNumber = 1;
                for (UserTimesheetResponseDto resultDto : timesheet) {
                    List<TimesheetDto> timesheets = resultDto.getTimesheets();
                    if (timesheets != null) {
                        for (TimesheetDto dto : timesheets) {
                            Row row = sheet.createRow(rowIdx++);
                            int col = 0;
                            reportStyleUtil.createStyledCell(row, col++, String.valueOf(serialNumber++), dataCellStyle);
                            reportStyleUtil.createStyledCell(row, col++, dto.getUserName(), dataCellStyle);
                            reportStyleUtil.createStyledCell(row, col++, dto.getMobileNumber(), dataCellStyle);
                            reportStyleUtil.createStyledCell(row, col++, dto.getGroupName(), dataCellStyle);
                            reportStyleUtil.createStyledCell(row, col++, dto.getStatus(), dataCellStyle);
                            reportStyleUtil.createStyledCell(row, col++, String.valueOf(dto.getDate()), dataCellStyle);
                            reportStyleUtil.createStyledCell(row, col++, String.valueOf(dto.getFirstClockInTime()), dataCellStyle);
                            reportStyleUtil.createStyledCell(row, col++, String.valueOf(dto.getLastClockOutTime()), dataCellStyle);
                            reportStyleUtil.createStyledCell(row, col, dto.getRegularHoursDuration(), dataCellStyle);
                        }
                    }
                    }
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
                sheet.setColumnWidth(3, 15 * 256);
                workbook.write(out);
                return out.toByteArray();

            } catch (IOException | java.io.IOException e) {
                throw new RuntimeException("XLSX export failed", e);
            }
        }

    public byte[] exportTimesheetWeekXlsx(List<UserTimesheetResponseDto> timesheets) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Timesheets");
            String[] staticHeaders = {"S.No", "Member Name", "Member Mobile Number", "Group Name", "Days"};
            List<String> summaryHeaders = List.of("Total Present", "Total Absent", "Total Paid Leave", "Total Not Marked");
            CellStyle headerCellStyle = reportStyleUtil.createHeaderCellStyle(workbook);
            CellStyle dataCellStyle = reportStyleUtil.createDataCellStyle(workbook);
            if (timesheets == null || timesheets.isEmpty()) {
                workbook.write(out);
                return out.toByteArray();
            }
            List<TimesheetDto> firstUserTimesheets = timesheets.getFirst().getTimesheets();
            List<String> dateHeaders = firstUserTimesheets.stream()
                    .map(t -> String.valueOf(t.getDate()))
                    .toList();
            Row headerRow = sheet.createRow(0);
            int col = 0;
            for (String h : staticHeaders) {
                Cell cell = headerRow.createCell(col++);
                cell.setCellValue(h);
                cell.setCellStyle(headerCellStyle);
            }
            for (String date : dateHeaders) {
                Cell cell = headerRow.createCell(col++);
                cell.setCellValue(date);
                cell.setCellStyle(headerCellStyle);
            }
            for (String sh : summaryHeaders) {
                Cell cell = headerRow.createCell(col++);
                cell.setCellValue(sh);
                cell.setCellStyle(headerCellStyle);
            }
            int rowIndex = 1;
            int serialNumber = 1;
            for (UserTimesheetResponseDto user : timesheets) {
                List<TimesheetDto> userTimesheets = user.getTimesheets();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                Map<String, TimesheetDto> dateToTimesheet = userTimesheets.stream()
                        .collect(Collectors.toMap(
                                t -> t.getDate().format(formatter),
                                t -> t
                        ));
                Row statusRow = sheet.createRow(rowIndex++);
                Row inRow = sheet.createRow(rowIndex++);
                Row outRow = sheet.createRow(rowIndex++);
                Row whRow = sheet.createRow(rowIndex++);

                int c = 0;
                reportStyleUtil.createStyledCell(statusRow, c, String.valueOf(serialNumber), dataCellStyle);
                reportStyleUtil.createStyledCell(statusRow, c + 1, user.getSummary().getUserName(), dataCellStyle);
                reportStyleUtil.createStyledCell(statusRow, c + 2, user.getSummary().getMobileNumber(), dataCellStyle);
                reportStyleUtil.createStyledCell(statusRow, c + 3, user.getSummary().getGroupName(), dataCellStyle);
                reportStyleUtil.createStyledCell(statusRow, c + 4, "STATUS", dataCellStyle);
                reportStyleUtil.createStyledCell(inRow, c + 4, "IN", dataCellStyle);
                reportStyleUtil.createStyledCell(outRow, c + 4, "OUT", dataCellStyle);
                reportStyleUtil.createStyledCell(whRow, c + 4, "WH", dataCellStyle);
                c += staticHeaders.length;
                for (String date : dateHeaders) {
                    TimesheetDto t = dateToTimesheet.get(date);

                    if (t != null) {
                        reportStyleUtil.createStyledCell(statusRow, c, t.getStatus(), dataCellStyle);
                        reportStyleUtil.createStyledCell(inRow, c, t.getFirstClockInTime(), dataCellStyle);
                        reportStyleUtil.createStyledCell(outRow, c, t.getLastClockOutTime(), dataCellStyle);
                        reportStyleUtil.createStyledCell(whRow, c, t.getRegularHoursDuration(), dataCellStyle);
                    } else {
                        reportStyleUtil.createStyledCell(statusRow, c, "-", dataCellStyle);
                        reportStyleUtil.createStyledCell(inRow, c, "-", dataCellStyle);
                        reportStyleUtil.createStyledCell(outRow, c, "-", dataCellStyle);
                        reportStyleUtil.createStyledCell(whRow, c, "-", dataCellStyle);
                    }
                    c++;
                }

                reportStyleUtil.createStyledCell(statusRow, c++, String.valueOf(user.getSummary().getPresentCount()), dataCellStyle);
                reportStyleUtil.createStyledCell(statusRow, c++, String.valueOf(user.getSummary().getAbsentCount()), dataCellStyle);
                reportStyleUtil.createStyledCell(statusRow, c++, String.valueOf(user.getSummary().getPaidLeaveCount()), dataCellStyle);
                reportStyleUtil.createStyledCell(statusRow, c++, String.valueOf(user.getSummary().getNotMarkedCount()), dataCellStyle);

                for (int i = c - summaryHeaders.size(); i < c; i++) {
                    reportStyleUtil.createStyledCell(inRow, i, "", dataCellStyle);
                    reportStyleUtil.createStyledCell(outRow, i, "", dataCellStyle);
                    reportStyleUtil.createStyledCell(whRow, i, "", dataCellStyle);
                }

                int firstRow = rowIndex - 4;
                int lastRow = rowIndex - 1;

                for (int colIndex = 0; colIndex <= 3; colIndex++) {
                    CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, colIndex, colIndex);
                    sheet.addMergedRegion(region);
                    RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
                }

                for (int colIndex = c - summaryHeaders.size(); colIndex < c; colIndex++) {
                    CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, colIndex, colIndex);
                    sheet.addMergedRegion(region);
                    RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
                }

                serialNumber++;
            }

            int totalCols = staticHeaders.length + dateHeaders.size() + summaryHeaders.size();
            for (int i = 0; i < totalCols; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException | java.io.IOException e) {
            throw new RuntimeException("Error generating Excel file", e);
        }

    }

    public byte[] exportTimesheetWeekCsv(List<UserTimesheetResponseDto> timesheets) {
        StringBuilder sb = new StringBuilder();
        String[] staticHeaders = {"S.No", "Member Name", "Member Mobile Number", "Group Name", "Days"};
        List<String> summaryHeaders = List.of("Total Present", "Total Absent", "Total Paid Leave", "Total Not Marked");

        if (timesheets == null || timesheets.isEmpty()) {
            return sb.toString().getBytes(StandardCharsets.UTF_8);
        }
        List<TimesheetDto> firstUserTimesheets = timesheets.get(0).getTimesheets();
        List<String> dateHeaders = firstUserTimesheets.stream()
                .map(t -> String.valueOf(t.getDate()))
                .toList();
        List<String> headerRow = new ArrayList<>();
        headerRow.addAll(Arrays.asList(staticHeaders));
        headerRow.addAll(dateHeaders);
        headerRow.addAll(summaryHeaders);
        sb.append(String.join(",", headerRow)).append("\n");
        int serialNumber = 1;
        for (UserTimesheetResponseDto user : timesheets) {
            Map<String, TimesheetDto> dateToTimesheet = user.getTimesheets().stream()
                    .collect(Collectors.toMap(
                            t -> String.valueOf(t.getDate()),
                            t -> t
                    ));
            String userName = user.getSummary().getUserName();
            String mobile = user.getSummary().getMobileNumber();
            String group = user.getSummary().getGroupName();
            String[] labels = {"STATUS", "IN", "OUT", "WH"};
            for (String label : labels) {
                List<String> row = new ArrayList<>();
                row.add(String.valueOf(serialNumber));
                row.add(escapeCsv(userName));
                row.add(escapeCsv(mobile));
                row.add(escapeCsv(group));
                row.add(escapeCsv(label));
                for (String date : dateHeaders) {
                    TimesheetDto t = dateToTimesheet.get(date);

                    switch (label) {
                        case "STATUS" -> row.add(t != null ? t.getStatus() : "-");
                        case "IN" -> row.add(t != null ? t.getFirstClockInTime() : "-");
                        case "OUT" -> row.add(t != null ? t.getLastClockOutTime() : "-");
                        case "WH" -> row.add(t != null ? t.getRegularHoursDuration() : "-");
                    }
                }
                if ("STATUS".equals(label)) {
                    row.add(String.valueOf(user.getSummary().getPresentCount()));
                    row.add(String.valueOf(user.getSummary().getAbsentCount()));
                    row.add(String.valueOf(user.getSummary().getPaidLeaveCount()));
                    row.add(String.valueOf(user.getSummary().getNotMarkedCount()));
                } else {
                    row.addAll(Arrays.asList("", "", "", ""));
                }
                sb.append(String.join(",", row)).append("\n");
            }
            serialNumber++;
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        boolean hasSpecial = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (hasSpecial) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }

    public FileExportResponseDto generateTimesheetFile(TimesheetReportDto request, String userIdFromToken, String orgId, String role, String fileName) {
        List<UserTimesheetResponseDto> timesheets = timesheetService.getAllTimesheets(userIdFromToken,orgId,role,request).getUserTimesheetResponseDtos();
        log.info("Requested Timesheet Date Range: {} to {}", request.getFromDate(), request.getToDate());
        log.info("Total timesheets fetched: {}", timesheets.size());
        String format = request.getFormat();
        String timePeriod = request.getTimePeriod();
        LocalDate startDate = request.getFromDate();
        LocalDate endDate = request.getToDate();
        if(request.getGroupId() != null && request.getGroupId().size() == 1){
            Long requestedGroupId = request.getGroupId().getFirst();
            String requestedGroupName = userService.findGroupName(requestedGroupId);
            log.info("Selected single Group name:{}", requestedGroupName);
            for (UserTimesheetResponseDto dto : timesheets){
                if (dto.getTimesheets() != null){
                    for (TimesheetDto timesheetDto : dto.getTimesheets()){
                        timesheetDto.setGroupName(requestedGroupName);
                    }
                }
            }
        }
        Path resourcePath = Paths.get(downloadDir);
        if (!Files.exists(resourcePath)) {
            try {
                Files.createDirectories(resourcePath);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }

        Path filePath = resourcePath.resolve(fileName);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String sheetName;
        if ("DAY".equalsIgnoreCase(timePeriod)) {
            sheetName = "Timesheet_" + startDate.format(formatter);
        } else if ("WEEK".equalsIgnoreCase(timePeriod)) {
            sheetName = "Timesheet_Weekly";
        } else if ("MONTH".equalsIgnoreCase(timePeriod)) {
            sheetName = "Timesheet_Monthly";
        } else {
            sheetName = "Timesheet_" + startDate.format(formatter) + "_to_" + endDate.format(formatter);
        }
        byte[] data;
        if (Timeperiod.DAY.name().equalsIgnoreCase(timePeriod)) {
            data = "csv".equalsIgnoreCase(format) ?
                    exportTimesheetDayCsv(timesheets, sheetName) :
                    exportTimesheetDayXlsx(timesheets);
        } else {
            data = "csv".equalsIgnoreCase(format) ?
                    exportTimesheetWeekCsv(timesheets) :
                    exportTimesheetWeekXlsx(timesheets);
        }
        try {
            Files.write(filePath, data);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
