package com.uniq.tms.tms_microservice.util;

import com.opencsv.CSVWriter;
import com.uniq.tms.tms_microservice.dto.TimesheetDto;
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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
    public class ReportUtils {
        public byte[] exportToCsv(List<TimesheetDto> timesheets) {
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

                csvWriter.flush();
                return out.toByteArray();
            } catch (IOException | java.io.IOException e) {
                throw new RuntimeException("CSV export failed", e);
            }
        }

    public byte[] exportToXlsx(List<TimesheetDto> timesheets) {
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

}
