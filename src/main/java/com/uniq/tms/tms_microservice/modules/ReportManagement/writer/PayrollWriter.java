package com.uniq.tms.tms_microservice.modules.ReportManagement.writer;

import com.opencsv.CSVWriter;
import com.uniq.tms.tms_microservice.modules.payrollManagement.projection.UserPayRollAmount;
import com.uniq.tms.tms_microservice.modules.ReportManagement.util.ReportStyleUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class PayrollWriter {

    private final ReportStyleUtil styleUtil;

    public PayrollWriter(ReportStyleUtil styleUtil) {
        this.styleUtil = styleUtil;
    }

    public void writeCsv(List<UserPayRollAmount> data, File file) throws Exception {
        try (CSVWriter csv = new CSVWriter(new FileWriter(file))) {

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new ClassPathResource("templates/text/payroll_amount_csv_header.txt")
                                    .getInputStream(),
                            StandardCharsets.UTF_8))) {

                csv.writeNext(br.readLine().split(","));
            }

            for (UserPayRollAmount p : data) {
                csv.writeNext(new String[]{
                        p.getUserId(),
                        p.getUserName(),
                        p.getPayrollName(),
                        p.getMonth(),
                        String.valueOf(p.getPayrollStatus()),
                        String.valueOf(p.getRegularDays()),
                        String.valueOf(p.getRegularHrs()),
                        String.valueOf(p.getOvertimeHrs()),
                        String.valueOf(p.getTotalHrs()),
                        String.valueOf(p.getMonthlyNetSalary()),
                        String.valueOf(p.getUnpaidLeaveDeduction()),
                        String.valueOf(p.getRegularPayrollAmount()),
                        String.valueOf(p.getOvertimePayrollAmount()),
                        String.valueOf(p.getTotalPayrollAmount()),
                        String.valueOf(p.getTotalAmount()),
                        p.getNotes()
                });
            }
        }
    }

    public void writeXlsx(List<UserPayRollAmount> data, File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream fos = new FileOutputStream(file)) {

            Sheet sheet = workbook.createSheet("Payroll Report");
            CellStyle header = styleUtil.createHeaderCellStyle(workbook);
            CellStyle dataStyle = styleUtil.createDataCellStyle(workbook);

            List<String> headers;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            new ClassPathResource("templates/text/payroll_amount_excel_header.txt")
                                    .getInputStream(),
                            StandardCharsets.UTF_8))) {
                headers = br.lines().toList();
            }

            String[] cols = headers.getFirst().split("\\|");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                styleUtil.createStyledCell(headerRow, i, cols[i], header);
            }

            int rowIdx = 1;
            for (UserPayRollAmount p : data) {
                Row row = sheet.createRow(rowIdx++);
                int c = 0;
                styleUtil.createStyledCell(row, c++, p.getUserId(), dataStyle);
                styleUtil.createStyledCell(row, c++, p.getUserName(), dataStyle);
                styleUtil.createStyledCell(row, c++, p.getPayrollName(), dataStyle);
                styleUtil.createStyledCell(row, c++, p.getMonth(), dataStyle);
                styleUtil.createStyledCell(row, c++, p.getPayrollStatus(), dataStyle);
                styleUtil.createStyledCell(row, c++, p.getRegularDays(), dataStyle);
                styleUtil.createStyledCell(row, c++, p.getRegularHrs(), dataStyle);
                styleUtil.createStyledCell(row, c++, p.getOvertimeHrs(), dataStyle);
                styleUtil.createStyledCell(row, c++, p.getTotalHrs(), dataStyle);
                styleUtil.createStyledCell(row, c++, String.valueOf(p.getMonthlyNetSalary()), dataStyle);
                styleUtil.createStyledCell(row, c++, String.valueOf(p.getUnpaidLeaveDeduction()), dataStyle);
                styleUtil.createStyledCell(row, c++, String.valueOf(p.getRegularPayrollAmount()), dataStyle);
                styleUtil.createStyledCell(row, c++, String.valueOf(p.getOvertimePayrollAmount()), dataStyle);
                styleUtil.createStyledCell(row, c++, String.valueOf(p.getTotalPayrollAmount()), dataStyle);
                styleUtil.createStyledCell(row, c++, String.valueOf(p.getTotalAmount()), dataStyle);
                styleUtil.createStyledCell(row, c, p.getNotes(), dataStyle);
            }

            workbook.write(fos);
        }
    }
}
