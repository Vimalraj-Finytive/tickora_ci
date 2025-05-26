package com.uniq.tms.tms_microservice.service.impl;

import com.opencsv.CSVWriter;
import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import com.uniq.tms.tms_microservice.dto.UserTimesheetResponseDto;
import com.uniq.tms.tms_microservice.service.ReportService;
import io.jsonwebtoken.io.IOException;
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
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    /**
     *
     * @param timesheet
     * @return timesheet report based on selected day timeperiod
     */
    @Override
    public byte[] exportTimesheetDayCsv(List<UserTimesheetResponseDto> timesheet, String sheetName) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out , StandardCharsets.UTF_8);
             CSVWriter csvWriter = new CSVWriter(outputStreamWriter, CSVWriter.DEFAULT_SEPARATOR,
                     CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END)) {

            ClassPathResource resource = new ClassPathResource("templates/timesheet_csv_header.txt");
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
                                dto.getGroupname(),
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

    @Override
    public byte[] exportTimesheetDayXlsx(List<UserTimesheetResponseDto> timesheet) {
            try (Workbook workbook = new XSSFWorkbook();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                Sheet sheet = workbook.createSheet("Timesheets");

                // Load headers from template
                ClassPathResource resource = new ClassPathResource("templates/timesheet_excel_header.txt");
                List<String> headerLines;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    headerLines = reader.lines().toList();
                }
                String[] headers = headerLines.get(0).split("\\|");

                // Header cell style
                CellStyle headerCellStyle = workbook.createCellStyle();
                headerCellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
                headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setColor(IndexedColors.WHITE.getIndex());
                headerCellStyle.setFont(headerFont);
                headerCellStyle.setAlignment(HorizontalAlignment.CENTER);
                headerCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                setThinBorder(headerCellStyle);

                // Data cell style
                CellStyle dataCellStyle = workbook.createCellStyle();
                dataCellStyle.setAlignment(HorizontalAlignment.CENTER);
                dataCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                setThinBorder(dataCellStyle);

                // Write headers
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    createStyledCell(headerRow, i, " " + headers[i] + " ", headerCellStyle);
                }

                // Write data
                int rowIdx = 1;
                int serialNumber = 1;
                for (UserTimesheetResponseDto resultDto : timesheet) {
                    List<TimesheetDto> timesheets = resultDto.getTimesheets();
                    if (timesheets != null) {
                        for (TimesheetDto dto : timesheets) {
                            Row row = sheet.createRow(rowIdx++);
                            int col = 0;
                            createStyledCell(row, col++, String.valueOf(serialNumber++), dataCellStyle);
                            createStyledCell(row, col++, dto.getUserName(), dataCellStyle);
                            createStyledCell(row, col++, dto.getMobileNumber(), dataCellStyle);
                            createStyledCell(row, col++, dto.getGroupname(), dataCellStyle);
                            createStyledCell(row, col++, dto.getStatus(), dataCellStyle);
                            createStyledCell(row, col++, String.valueOf(dto.getDate()), dataCellStyle);
                            createStyledCell(row, col++, String.valueOf(dto.getFirstClockInTime()), dataCellStyle);
                            createStyledCell(row, col++, String.valueOf(dto.getLastClockOutTime()), dataCellStyle);
                            createStyledCell(row, col, dto.getRegularHoursDuration(), dataCellStyle);
                        }
                    }
                    }

                // Auto-size columns
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                workbook.write(out);
                return out.toByteArray();

            } catch (IOException | java.io.IOException e) {
                throw new RuntimeException("XLSX export failed", e);
            }
        }

    @Override
    public byte[] exportTimesheetWeekXlsx(List<UserTimesheetResponseDto> timesheets) {

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Timesheets");

            // Prepare headers
            String[] staticHeaders = {"S.No", "Member Name", "Member Mobile Number", "Group Name", "Days"};
            List<String> summaryHeaders = List.of("Total Present", "Total Absent", "Total Paid Leave", "Total Not Marked");

            CellStyle headerCellStyle = createHeaderCellStyle(workbook);
            CellStyle dataCellStyle = createDataCellStyle(workbook);

            // If no data, return empty workbook bytes
            if (timesheets == null || timesheets.isEmpty()) {
                workbook.write(out);
                return out.toByteArray();
            }

            // Use the first user's timesheets dates as dynamic headers (assuming all users have same date range)
            List<TimesheetDto> firstUserTimesheets = timesheets.get(0).getTimesheets();
            List<String> dateHeaders = firstUserTimesheets.stream()
                    .map(t -> String.valueOf(t.getDate()))  // getDate returns String like "2025-04-28"
                    .toList();

            // Create header row
            Row headerRow = sheet.createRow(0);

            int col = 0;
            // Static headers
            for (String h : staticHeaders) {
                Cell cell = headerRow.createCell(col++);
                cell.setCellValue(h);
                cell.setCellStyle(headerCellStyle);
            }
            // Date columns
            for (String date : dateHeaders) {
                Cell cell = headerRow.createCell(col++);
                cell.setCellValue(date);
                cell.setCellStyle(headerCellStyle);
            }
            // Summary headers
            for (String sh : summaryHeaders) {
                Cell cell = headerRow.createCell(col++);
                cell.setCellValue(sh);
                cell.setCellStyle(headerCellStyle);
            }

            // Start writing user data, 4 rows per user: Status, IN, OUT, WH
            int rowIndex = 1;
            int serialNumber = 1;

            for (UserTimesheetResponseDto user : timesheets) {
                List<TimesheetDto> userTimesheets = user.getTimesheets();

                // Map date to timesheet for fast lookup
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

                // Fill static cells for Status row (Sr No, User Name, Mobile Number, Group Name, "STATUS")
                createStyledCell(statusRow, c, String.valueOf(serialNumber), dataCellStyle);
                createStyledCell(statusRow, c + 1, user.getSummary().getUserName(), dataCellStyle);
                createStyledCell(statusRow, c + 2, user.getSummary().getMobileNumber(), dataCellStyle);
                createStyledCell(statusRow, c + 3, user.getSummary().getGroupname(), dataCellStyle);
                createStyledCell(statusRow, c + 4, "STATUS", dataCellStyle);

                // For other 3 rows, add labels in 5th column
                createStyledCell(inRow, c + 4, "IN", dataCellStyle);
                createStyledCell(outRow, c + 4, "OUT", dataCellStyle);
                createStyledCell(whRow, c + 4, "WH", dataCellStyle);

                c += staticHeaders.length; // Move to date columns

                // Fill date columns for each row
                for (String date : dateHeaders) {
                    TimesheetDto t = dateToTimesheet.get(date);

                    if (t != null) {
                        createStyledCell(statusRow, c, t.getStatus(), dataCellStyle);
                        createStyledCell(inRow, c, t.getFirstClockInTime(), dataCellStyle);
                        createStyledCell(outRow, c, t.getLastClockOutTime(), dataCellStyle);
                        createStyledCell(whRow, c, t.getRegularHoursDuration(), dataCellStyle);
                    } else {
                        createStyledCell(statusRow, c, "-", dataCellStyle);
                        createStyledCell(inRow, c, "-", dataCellStyle);
                        createStyledCell(outRow, c, "-", dataCellStyle);
                        createStyledCell(whRow, c, "-", dataCellStyle);
                    }
                    c++;
                }

                // Summary columns only on status row
                createStyledCell(statusRow, c++, String.valueOf(user.getSummary().getPresentCount()), dataCellStyle);
                createStyledCell(statusRow, c++, String.valueOf(user.getSummary().getAbsentCount()), dataCellStyle);
                createStyledCell(statusRow, c++, String.valueOf(user.getSummary().getPaidLeaveCount()), dataCellStyle);
                createStyledCell(statusRow, c++, String.valueOf(user.getSummary().getNotMarkedCount()), dataCellStyle);

                // Empty summary cells for other rows
                for (int i = c - summaryHeaders.size(); i < c; i++) {
                    createStyledCell(inRow, i, "", dataCellStyle);
                    createStyledCell(outRow, i, "", dataCellStyle);
                    createStyledCell(whRow, i, "", dataCellStyle);
                }

                // --- MERGE CELLS vertically for columns 0 to 3 (S.No, Name, Mobile, Group) ---
                int firstRow = rowIndex - 4; // starting row of this user block
                int lastRow = rowIndex - 1;  // last row of this user block (whRow)

                for (int colIndex = 0; colIndex <= 3; colIndex++) {
                    CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, colIndex, colIndex);
                    sheet.addMergedRegion(region);

                    // Set thin borders around merged region
                    RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
                }

                for (int colIndex = c - summaryHeaders.size(); colIndex < c; colIndex++) {
                    CellRangeAddress region = new CellRangeAddress(firstRow, lastRow, colIndex, colIndex);
                    sheet.addMergedRegion(region);

                    // Set thin borders around merged region
                    RegionUtil.setBorderTop(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderBottom(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderLeft(BorderStyle.THIN, region, sheet);
                    RegionUtil.setBorderRight(BorderStyle.THIN, region, sheet);
                }


                serialNumber++;
            }

            // Autosize columns
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

        // Prepare headers
        String[] staticHeaders = {"S.No", "Member Name", "Member Mobile Number", "Group Name", "Days"};
        List<String> summaryHeaders = List.of("Total Present", "Total Absent", "Total Paid Leave", "Total Not Marked");

        if (timesheets == null || timesheets.isEmpty()) {
            return sb.toString().getBytes(StandardCharsets.UTF_8);
        }

        // Extract date headers from the first user's timesheet
        List<TimesheetDto> firstUserTimesheets = timesheets.get(0).getTimesheets();
        List<String> dateHeaders = firstUserTimesheets.stream()
                .map(t -> String.valueOf(t.getDate())) // assuming t.getDate() returns a String
                .toList();

        // Combine all headers
        List<String> headerRow = new ArrayList<>();
        headerRow.addAll(Arrays.asList(staticHeaders));
        headerRow.addAll(dateHeaders);
        headerRow.addAll(summaryHeaders);

        // Write header
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
            String group = user.getSummary().getGroupname();

            // 4 Rows: STATUS, IN, OUT, WH
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

                // Only STATUS row includes summary columns
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

    //  method to create a styled cell
        private void createStyledCell(Row row, int colIndex, String value, CellStyle cellStyle) {
            Cell cell = row.createCell(colIndex);
            cell.setCellValue(value);
            cell.setCellStyle(cellStyle);
        }

        //  method to apply thin borders
        private void setThinBorder(CellStyle style) {
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
        }

    // Helper for header style
    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setThinBorder(style);
        return style;
    }


    // Helper for data style
    private CellStyle createDataCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setThinBorder(style);
        return style;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        boolean hasSpecial = value.contains(",") || value.contains("\"") || value.contains("\n");
        if (hasSpecial) {
            value = value.replace("\"", "\"\""); // escape double quotes
            return "\"" + value + "\"";
        }
        return value;
    }
}
