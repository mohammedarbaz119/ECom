package com.test.commerce.services;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.AreaBreakType;
import com.itextpdf.layout.properties.TextAlignment;
import com.test.commerce.model.Order;
import com.test.commerce.model.OrderItem;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class InvoiceGenerator {

    public ByteArrayOutputStream generateInvoicePdf(Order order , List<OrderItem> items,String email) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("INVOICE"));
        document.add(new Paragraph("Order ID: " + order.getId()));
        document.add(new Paragraph("Customer: " + email));
        document.add(new Paragraph("Delivered Date: " + order.getDeliveredDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))));
        document.add(new Paragraph("Items").setTextAlignment(TextAlignment.CENTER));


        for (OrderItem item : items) {
            document.add(new Paragraph(item.getProduct().getName() + " x" + item.getQuantity()+" = " +item.getPrice()).setMarginLeft(5));
            document.add(new Paragraph("Retailer "+ item.getProduct().getRetailer().getRetailerName()).setMarginLeft(5));
        }

        document.add(new Paragraph("Total: ₹" + order.getTotalamt()).setMarginLeft(6));

        document.close();
        return baos;
    }
}
