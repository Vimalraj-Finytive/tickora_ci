package com.uniq.tms.tms_microservice.modules.ReportManagement.writer;

import com.opencsv.CSVWriter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.projection.TimeOffExportView;
import com.uniq.tms.tms_microservice.modules.ReportManagement.util.ReportStyleUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TimeOffRequestWriter {

    private final ReportStyleUtil styleUtil;

    public TimeOffRequestWriter(ReportStyleUtil styleUtil) {
        this.styleUtil = styleUtil;
    }

    public void writeCsv(List<TimeOffExportView> data, File file) throws Exception {

        Map<Long, List<TimeOffExportView>> grouped =
                data.stream().collect(Collectors.groupingBy(TimeOffExportView::getTimeoffRequestId));

        try (CSVWriter csv = new CSVWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new ClassPathResource("templates/text/timeoff_request_csv_header.txt")
                                    .getInputStream(),
                            StandardCharsets.UTF_8))) {
                csv.writeNext(br.readLine().split(","));
            }

            for (List<TimeOffExportView> rows : grouped.values()) {
                TimeOffExportView first = rows.getFirst();

                csv.writeNext(new String[]{
                        first.getCreatorId(),
                        first.getCreatorName(),
                        first.getPolicyName(),
                        String.valueOf(first.getLeaveStartDate()),
                        String.valueOf(first.getLeaveEndDate()),
                        first.getLeaveType(),
                        first.getStatus()
                });
            }
        }
    }

    public void writeXlsx(List<TimeOffExportView> data, File file) throws Exception {

        Map<Long, List<TimeOffExportView>> grouped =
                data.stream().collect(Collectors.groupingBy(TimeOffExportView::getTimeoffRequestId));

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {

            Sheet sheet = workbook.createSheet("TimeOff Report");

            List<String> headers;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new ClassPathResource("templates/text/timeoff_request_excel_header.txt")
                                    .getInputStream(),
                            StandardCharsets.UTF_8))) {
                headers = br.lines().toList();
            }

            CellStyle headerStyle = styleUtil.createHeaderCellStyle(workbook);
            CellStyle dataStyle = styleUtil.createDataCellStyle(workbook);

            Row headerRow = sheet.createRow(0);
            String[] cols = headers.getFirst().split("\\|");

            for (int i = 0; i < cols.length; i++) {
                styleUtil.createStyledCell(headerRow, i, cols[i], headerStyle);
            }

            int rowIdx = 1;

            for (List<TimeOffExportView> rows : grouped.values()) {
                TimeOffExportView first = rows.getFirst();
                Row row = sheet.createRow(rowIdx++);
                int c = 0;

                styleUtil.createStyledCell(row, c++, first.getCreatorId(), dataStyle);
                styleUtil.createStyledCell(row, c++, first.getCreatorName(), dataStyle);
                styleUtil.createStyledCell(row, c++, first.getPolicyName(), dataStyle);
                styleUtil.createStyledCell(row, c++, String.valueOf(first.getLeaveStartDate()), dataStyle);
                styleUtil.createStyledCell(row, c++, String.valueOf(first.getLeaveEndDate()), dataStyle);
                styleUtil.createStyledCell(row, c++, first.getLeaveType(), dataStyle);
                styleUtil.createStyledCell(row, c, first.getStatus(), dataStyle);
            }

            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fos);
        }
    }
}
