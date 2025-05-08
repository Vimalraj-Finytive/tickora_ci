package com.uniq.tms.tms_microservice.util;

import com.uniq.tms.tms_microservice.dto.TimesheetDto;
import io.jsonwebtoken.io.IOException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
    public class ReportUtils {
        public byte[] exportToCsv(List<TimesheetDto> timesheets) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 PrintWriter writer = new PrintWriter(out)) {

                ClassPathResource resource = new ClassPathResource("templates/timesheet_csv_header.txt");
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                    reader.lines().forEach(writer::println);
                }
                for (TimesheetDto dto : timesheets) {
                    writer.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                            dto.getUserName(),
                            dto.getUserId(),
                            dto.getDate(),
                            dto.getFirstClockInTime(),
                            dto.getLastClockOutTime(),
                            dto.getRegularHoursDuration(),
                            dto.getDayType(),
                            dto.getUserDayType());
                }

                writer.flush();
                return out.toByteArray();
            } catch (IOException | java.io.IOException e) {
                throw new RuntimeException("CSV export failed", e);
            }
        }

        public byte[] exportToXlsx(List<TimesheetDto> timesheets) {
            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Sheet sheet = workbook.createSheet("Timesheets");

                // Load headers from template
                ClassPathResource resource = new ClassPathResource("templates/timesheet_excel_header.txt");
                List<String> headerLines;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))){
                    headerLines = reader.lines().toList();
                }
                String[] headers = headerLines.get(0).split("\\|");

                // Write header
                Row header = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    header.createCell(i).setCellValue(headers[i]);
                }

                // Write data
                int rowIdx = 1;
                for (TimesheetDto dto : timesheets) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(dto.getUserName());
                    row.createCell(1).setCellValue(dto.getUserId());
                    row.createCell(2).setCellValue(dto.getDate().toString());
                    row.createCell(3).setCellValue(dto.getFirstClockInTime().toString());
                    row.createCell(4).setCellValue(dto.getLastClockOutTime().toString());
                    row.createCell(5).setCellValue(dto.getRegularHoursDuration());
                    row.createCell(6).setCellValue(dto.getDayType());
                    row.createCell(7).setCellValue(dto.getUserDayType());
                }

                workbook.write(out);
                return out.toByteArray();
            } catch (IOException | java.io.IOException e) {
                throw new RuntimeException("XLSX export failed", e);
            }
        }
}
