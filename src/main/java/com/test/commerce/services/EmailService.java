package com.test.commerce.services;

import com.itextpdf.io.source.ByteArrayOutputStream;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    @Autowired
    private final JavaMailSender mailSender;

    @Async
    public void sendSimpleEmail(String to, String subject, String message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(to);
        mail.setSubject(subject);
        mail.setText(message);
        mailSender.send(mail);
    }

    public void sendInvoiceEmail(String to, ByteArrayOutputStream invoicePdf, Long orderId) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
      try{
          helper.setTo(to);
          helper.setSubject("Your Invoice for Order #" + orderId);
          helper.setText("Please find your invoice attached.");
          helper.setFrom("mda835856@gmail.com", "Ecom Store");
      } catch (Exception e) {
          log.info(e.getMessage());
      }
        ByteArrayResource pdfResource = new ByteArrayResource(invoicePdf.toByteArray());
        helper.addAttachment("Invoice_Order_" + orderId + ".pdf", pdfResource);
        mailSender.send(message);
    }
}