package com.test.commerce.services;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
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
        document.add(new Paragraph("Items:"));

        for (OrderItem item : items) {
            document.add(new Paragraph(item.getProduct().getName() + " x " + item.getQuantity()+" " +item.getPrice() +" Retailer  "+ item.getProduct().getRetailer().getRetailerName()));
        }

        document.add(new Paragraph("Total: ₹" + order.getTotalamt()));

        document.close();
        return baos;
    }
}
