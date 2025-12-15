package com.uniq.tms.tms_microservice.shared.helper;

import com.itextpdf.html2pdf.HtmlConverter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PaymentDetailsDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PaymentDto;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

public class InvoiceGeneratorHelper {

    public static ByteArrayOutputStream generateInvoicePdf(PaymentDto plan, String OrgName) throws Exception {

        InputStream htmlStream = InvoiceGeneratorHelper.class
                .getResourceAsStream("/templates/html/invoice-template.html");
        if (htmlStream == null) throw new RuntimeException("Template missing!");

        InputStream logoStream = InvoiceGeneratorHelper.class.getResourceAsStream("/templates/image/tickora.png");
        String base64Logo = Base64.getEncoder().encodeToString(logoStream.readAllBytes());

        InputStream footerStream = InvoiceGeneratorHelper.class.getResourceAsStream("/templates/image/footer.png");
        String base64Footer = Base64.getEncoder().encodeToString(footerStream.readAllBytes());

        String html = new String(htmlStream.readAllBytes(), StandardCharsets.UTF_8);

        List<PaymentDetailsDto> payments = plan.getPayments();
        PaymentDetailsDto latest = (payments != null && !payments.isEmpty()) ? payments.get(0) : null;

        StringBuilder rows = new StringBuilder();
        double totalAmount = 0.0;

        if (payments != null && !payments.isEmpty()) {
            for (PaymentDetailsDto p : payments) {
                totalAmount += (p.getAmount() != null ? p.getAmount() : 0.0);

                rows.append("<tr>")
                        .append("<td>").append(safe(plan.getPlanName())).append("</td>")
                        .append("<td>").append(formatDateOnly(p.getPaidAt())).append("</td>")
                        .append("<td>").append(safe(plan.getBillingCycle())).append("</td>")
                        .append("<td>₹ ").append(String.format("%.2f", p.getAmount())).append("</td>")
                        .append("</tr>");
            }
        } else {
            rows.append("<tr><td colspan='4' style='text-align:center;'>No payment history available</td></tr>");
        }

        html = html.replace("{{paymentRows}}", rows.toString())
                .replace("{{totalAmount}}", String.format("%.2f", totalAmount))
                .replace("{{method}}", formatMethod(latest != null ? latest.getMethod() : null))
                .replace("{{paymentStatus}}", formatStatus(latest != null ? latest.getPaymentStatus() : null))
                .replace("{{invoiceId}}", safe(latest != null ? latest.getInvoiceId() : "-"))
                .replace("{{orgName}}", safe(OrgName))
                .replace("{{email}}", safe(latest != null ? latest.getEmail() : "-"))
                .replace("{{contact}}", normalizeContact(latest != null ? latest.getContact() : null))
                .replace("{{invoiceDate}}", formatDateOnly(LocalDate.now().toString()))
                .replace("{{logo}}", "data:image/png;base64," + base64Logo)
                .replace("{{footerImage}}", "data:image/png;base64," + base64Footer)
                .replace("{{subscribedUser}}",safe(plan.getSubscribedUser().toString()));


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        HtmlConverter.convertToPdf(html, outputStream);
        return outputStream;
    }

    private static String safe(String val) {
        return val != null ? val : "-";
    }

    private static String normalizeContact(String contact) {
        if (contact == null) return "-";
        return contact.replaceAll("^\\+", "");
    }

    private static String formatMethod(String method) {
        return method != null ? method.toUpperCase() : "-";
    }

    private static String formatStatus(String status) {
        if (status == null || status.isEmpty()) return "-";
        return status.substring(0,1).toUpperCase()
                + status.substring(1).toLowerCase();
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private static String formatDateOnly(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) return "-";

        try {
            if (dateTime.contains("T")) {
                return LocalDateTime.parse(dateTime).toLocalDate().format(DATE_FORMATTER);
            }
            if (dateTime.contains(" ")) {
                return LocalDate.parse(dateTime.split(" ")[0]).format(DATE_FORMATTER);
            }
            return LocalDate.parse(dateTime).format(DATE_FORMATTER);

        } catch (Exception e) {
            return dateTime;
        }
    }
}
