package com.test.commerce.services;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.test.commerce.dtos.SaleDTO;
import com.test.commerce.enums.ReportStatus;
import com.test.commerce.enums.ReportType;
import com.test.commerce.enums.SaleStatus;
import com.test.commerce.model.SalesReport;
import com.test.commerce.repositories.SaleRepository;
import com.test.commerce.repositories.SalesReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static com.itextpdf.styledxmlparser.jsoup.nodes.Entities.escape;



@Service
public class ReportGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerationService.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private SalesReportRepository reportRequestRepository;

    @Autowired
    private S3UploadService s3UploadService;

    @Autowired
    private SaleRepository saleRepository;

    @Async("reportTaskExecutor")
    public CompletableFuture<Void> generateReport(Long requestId,String email) {
        logger.info("Starting report generation for request ID: {}", requestId);

        SalesReport request = reportRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Report request not found"));

        try {
            request.setStatus(ReportStatus.PROCESSING);
            reportRequestRepository.save(request);

            String s3Key = s3UploadService.generateReportKey(
                    request.getRetailer().getID(),
                    request.getReportType().name(),
                    request.getStartDate().toString(),
                    request.getEndDate().toString(),
                    null
            );

            List<SaleDTO> salesData = saleRepository.findAllByRetailerId(
                    request.getRetailer().getID(),
                    request.getStartDate().atStartOfDay(),
                    request.getEndDate().atTime(LocalTime.MAX)
            );

            byte[] reportContent;
            String contentType;

            if (request.getReportType() == ReportType.CSV) {
                reportContent = generateCsvReport(salesData);
                contentType = s3UploadService.getReportContentType("CSV");
            } else {
                reportContent = generatePdfReport(salesData);
                contentType = s3UploadService.getReportContentType("PDF");
            }

            // Upload to S3 using existing service
            s3UploadService.uploadReport(s3Key, reportContent, contentType);

            // Update request with success
            request.setStatus(ReportStatus.GENERATED);
            request.setS3FilePath(s3Key);
            request.setCompletedAt(LocalDateTime.now());
            reportRequestRepository.save(request);
            emailService.sendSimpleEmail(email,"sale report with id "+requestId+" generated","report is ready to be downloaded to view");


            logger.info("Report generation completed for request ID: {}. File: {}", requestId, s3Key);

        } catch (Exception e) {
            logger.error("Report generation failed for request ID: {}", requestId, e);

            request.setStatus(ReportStatus.ERROR);
            request.setErrorMessage(e.getMessage());
            request.setCompletedAt(LocalDateTime.now());
            reportRequestRepository.save(request);
            emailService.sendSimpleEmail(email,"sale report with id "+requestId+" failed","report generation failed please try to generate it again");
        }

        return CompletableFuture.completedFuture(null);
    }

    private byte[] generateCsvReport(List<SaleDTO> salesData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BigDecimal totalEarned = new BigDecimal(0);

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.println("Date,Item Name,Item Code,Quantity,Unit Price,Total Amount,Comission,Retailer Amount,Customer Email,Sale/Order Status");
            for (SaleDTO record : salesData) {
                if(Objects.equals(record.getSaleStatus(), SaleStatus.DELIVERED)){
                    totalEarned = totalEarned.add(record.getRetailerAmount());
                }
                writer.printf("%s,%s,%s,%d,%.2f,%.2f,%.2f,%.2f,%s,%s%n",
                        record.getSaleDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
                        escapeCSV(record.getProductName()),
                        record.getProductCategory(),
                        record.getQuantity(),
                        record.getUnitPrice(),
                        record.getTotalAmount(),
                        record.getCommissionAmount(),
                        record.getRetailerAmount(),
                        escapeCSV(record.getCustomerEmail()),
                        record.getSaleStatus().toString()
                );
            }
            writer.println("total earnings made in the period = ₹"+ totalEarned);
        }
        return outputStream.toByteArray();
    }

    private byte[] generatePdfReport(List<SaleDTO> salesData) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BigDecimal totalEarned = BigDecimal.ZERO;

        try (PdfWriter writer = new PdfWriter(outputStream);
             PdfDocument pdfDoc = new PdfDocument(writer);
             Document document = new Document(pdfDoc, PageSize.A4.rotate())) {

            document.add(new Paragraph("Sales Report")
                    .setFontSize(16)
                    .simulateBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Create table with 10 columns
            float[] columnWidths = {100f, 100f, 80f, 50f, 70f, 80f, 70f, 100f, 120f, 80f};
            Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

            // Add headers
            String[] headers = {"Date", "Item Name", "Item Code", "Quantity", "Unit Price", "Total Amount", "Commission", "Retailer Amount", "Customer Email", "Product Returned"};
            for (String header : headers) {
                table.addHeaderCell(new Cell().add(new Paragraph(header)));
            }

            // Fill table data
            for (SaleDTO record : salesData) {
                if (Objects.equals(record.getSaleStatus(), SaleStatus.DELIVERED)) {
                    totalEarned = totalEarned.add(record.getRetailerAmount());
                }
                table.addCell(record.getSaleDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
                table.addCell(escape(record.getProductName()));
                table.addCell(escape(record.getProductCategory()));
                table.addCell(String.valueOf(record.getQuantity()));
                table.addCell(record.getUnitPrice().toString());
                table.addCell(record.getTotalAmount().toString());
                table.addCell(record.getCommissionAmount().toString());
                table.addCell(record.getRetailerAmount().toString());
                table.addCell(escape(record.getCustomerEmail()));
                table.addCell(record.getSaleStatus().toString());
            }

            // Add table and earnings note
            document.add(table);
            document.add(new Paragraph("\nTotal earnings made in the period = ₹" + totalEarned)
                    .simulateBold()
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.RIGHT));
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()));
        }

        return outputStream.toByteArray();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
