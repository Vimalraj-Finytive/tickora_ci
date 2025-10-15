package com.uniq.tms.tms_microservice.shared.helper;


import com.itextpdf.html2pdf.HtmlConverter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PaymentDetailsDto;
import com.uniq.tms.tms_microservice.modules.organizationManagement.dto.PaymentDto;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;


public class InvoiceGeneratorHelper {

    public static ByteArrayOutputStream generateInvoicePdf(PaymentDto plan, String OrgName) throws Exception {

            InputStream htmlStream = InvoiceGeneratorHelper.class
                    .getResourceAsStream("/templates/invoice-template.html");
        InputStream logoStream = InvoiceGeneratorHelper.class.getResourceAsStream("/templates/image/tickora.png");
        byte[] logoBytes = logoStream.readAllBytes();
        String base64Logo = Base64.getEncoder().encodeToString(logoBytes);
        InputStream footerStream = InvoiceGeneratorHelper.class.getResourceAsStream("/templates/image/footer.png");
        if (footerStream == null) {
            throw new RuntimeException("Footer image not found: /templates/image/footer.png");
        }
        byte[] footerBytes = footerStream.readAllBytes();
        String base64Footer = Base64.getEncoder().encodeToString(footerBytes);

        if (htmlStream == null) {
                throw new RuntimeException("Template not found: /templates/invoice.html");
            }

            String html = new String(htmlStream.readAllBytes(), StandardCharsets.UTF_8);

            PaymentDetailsDto pay = plan.getPayment();

            html = html.replace("{{planName}}", safe(plan.getPlanName()))
                    .replace("{{orgName}}", safe(OrgName))
                    .replace("{{status}}", safe(plan.getStatus()))
                    .replace("{{start}}", safe(plan.getStart()))
                    .replace("{{end}}", safe(plan.getEnd()))
                    .replace("{{subscribedUser}}", String.valueOf(plan.getSubscribedUser()))
                    .replace("{{billingCycle}}", safe(plan.getBillingCycle()))
                    .replace("{{email}}", safe(pay != null ? pay.getEmail() : "N/A"))
                    .replace("{{contact}}", safe(pay != null ? pay.getContact() : "N/A"))
                    .replace("{{invoiceDate}}", LocalDate.now().toString())
                    .replace("{{invoiceId}}", safe(pay != null ? pay.getInvoiceId() : "N/A"))
                    .replace("{{paymentId}}", safe(pay != null ? pay.getPaymentId() : "N/A"))
                    .replace("{{paymentStatus}}", safe(pay != null ? pay.getPaymentStatus() : "N/A"))
                    .replace("{{method}}", safe(pay != null ? pay.getMethod() : "N/A"))
                    .replace("{{paidAt}}", safe(pay != null ? pay.getPaidAt() : "N/A"))
                    .replace("{{amount}}", safe(pay != null ? String.valueOf(pay.getAmount()) : "N/A"))
                    .replace("{{currency}}", safe(pay != null ? pay.getCurrency() : "N/A"))
                    .replace("{{logo}}", "data:image/png;base64," + base64Logo)
                    .replace("{{footerImage}}", "data:image/png;base64," + base64Footer)
                    .replace("{{bank}}", safe(pay != null && pay.getBank() != null ? pay.getBank() : "N/A"));

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(html, outputStream);

            return outputStream;
        }

        private static String safe(String val) {
            return val != null ? val : "-";
        }
}
