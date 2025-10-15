package com.uniq.tms.tms_microservice.shared.helper;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PaymentDetailsDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PaymentDto;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

public class InvoiceGeneratorHelper {

    public static ByteArrayOutputStream generateInvoicePdf(PaymentDto plan) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, outputStream);
        document.open();
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, BaseColor.BLACK);
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.GRAY);
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.GRAY);
        Paragraph company = new Paragraph("Tickora", titleFont);
        company.setAlignment(Element.ALIGN_CENTER);
        document.add(company);

        Paragraph tagline = new Paragraph("Time Management", subtitleFont);
        tagline.setAlignment(Element.ALIGN_CENTER);
        document.add(tagline);

        document.add(new Paragraph("\n"));

        PaymentDetailsDto pay = plan.getPayment();
        if (pay != null) {
            Paragraph email = new Paragraph("Email: " + (pay.getEmail() != null ? pay.getEmail() : "N/A"), smallFont);
            email.setAlignment(Element.ALIGN_LEFT);
            document.add(email);

            Paragraph contact = new Paragraph("Contact: " + (pay.getContact() != null ? pay.getContact() : "N/A"), smallFont);
            contact.setAlignment(Element.ALIGN_LEFT);
            document.add(contact);
        }
        document.add(new Paragraph("\nInvoice Date: " + LocalDate.now(), smallFont));
        document.add(new Paragraph("\n"));

        PdfPTable outerBox = new PdfPTable(1);
        outerBox.setWidthPercentage(100);
        PdfPCell outerCell = new PdfPCell();
        outerCell.setPadding(15f);
        outerCell.setBorderColor(BaseColor.LIGHT_GRAY);

        PdfPTable planTable = new PdfPTable(2);
        planTable.setWidthPercentage(100);
        planTable.setWidths(new float[]{3, 5});
        planTable.setSpacingAfter(10f);

        addRow(planTable, "Plan Name", plan.getPlanName(), labelFont, valueFont);
        addRow(planTable, "Status", plan.getStatus(), labelFont, valueFont);
        addRow(planTable, "Start Date", plan.getStart(), labelFont, valueFont);
        addRow(planTable, "End Date", plan.getEnd(), labelFont, valueFont);
        addRow(planTable, "Subscribed Users", String.valueOf(plan.getSubscribedUser()), labelFont, valueFont);
        addRow(planTable, "Billing Cycle", plan.getBillingCycle(), labelFont, valueFont);

        outerCell.addElement(planTable);
        if (pay != null) {
            PdfPTable payTable = new PdfPTable(2);
            payTable.setWidthPercentage(100);
            payTable.setWidths(new float[]{3, 5});
            payTable.setSpacingBefore(5f);

            addRow(payTable, "Invoice ID", pay.getInvoiceId(), labelFont, valueFont);
            addRow(payTable, "Payment ID", pay.getPaymentId(), labelFont, valueFont);
            addRow(payTable, "Payment Status", pay.getPaymentStatus(), labelFont, valueFont);
            addRow(payTable, "Method", pay.getMethod(), labelFont, valueFont);
            addRow(payTable, "Paid At", pay.getPaidAt(), labelFont, valueFont);
            addRow(payTable, "Amount", pay.getAmount() + " " + pay.getCurrency(), labelFont, valueFont);
            addRow(payTable, "Currency", pay.getCurrency(), labelFont, valueFont);
            addRow(payTable, "Bank", pay.getBank() != null ? pay.getBank() : "N/A", labelFont, valueFont);

            outerCell.addElement(payTable);
        } else {
            outerCell.addElement(new Paragraph("No payment details found.", smallFont));
        }

        outerBox.addCell(outerCell);
        document.add(outerBox);
        document.add(new Paragraph("\n"));
        Paragraph thanks = new Paragraph("Thank you for your subscription!", subtitleFont);
        thanks.setAlignment(Element.ALIGN_CENTER);
        document.add(thanks);
        document.close();
        return outputStream;
    }

    private static void addRow(PdfPTable table, String field, String value, Font labelFont, Font valueFont) {
        PdfPCell cell1 = new PdfPCell(new Phrase(field, labelFont));
        PdfPCell cell2 = new PdfPCell(new Phrase(value != null ? value : "-", valueFont));
        cell1.setBorder(Rectangle.NO_BORDER);
        cell2.setBorder(Rectangle.NO_BORDER);
        cell1.setPadding(6f);
        cell2.setPadding(6f);
        table.addCell(cell1);
        table.addCell(cell2);
    }
}
