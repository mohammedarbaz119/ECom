package com.test.commerce.controllers;


import com.test.commerce.dtos.GenerateReportRequestDto;
import com.test.commerce.enums.ReportStatus;
import com.test.commerce.model.Retailer;
import com.test.commerce.model.SalesReport;
import com.test.commerce.repositories.RetailorRepository;
import com.test.commerce.repositories.SalesReportRepository;
import com.test.commerce.services.ReportGenerationService;
import com.test.commerce.services.S3UploadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/api/retailer")
@RestController
@Secured("RETAILER")
public class RetailerController {

    @Autowired
    private RetailorRepository retailorRepository;

    @Autowired
    private S3UploadService s3UploadService;

    @Autowired
    private ReportGenerationService reportGenerationService;

    @Autowired
    private SalesReportRepository salesReportRepository;


    @PostMapping("/report/generate")
    public ResponseEntity<Map<String,Long>> generateReport(@Valid @RequestBody GenerateReportRequestDto dto, Principal principal){
        Retailer retailer  = retailorRepository.findByEmail(principal.getName()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Retailer not found"));
        SalesReport report = new SalesReport();
        report.setRetailer(retailer);
        report.setReportType(dto.getReportType());
        report.setStartDate(dto.getStartDate());
        report.setEndDate(dto.getEndDate());
        salesReportRepository.save(report);
        reportGenerationService.generateReport(report.getId(),retailer.getEmail());
        Map<String,Long> repsonse = new HashMap<>();
        repsonse.put("reportId", report.getId());
        return ResponseEntity.ok(repsonse);
    }

    @GetMapping("/report/{reportId}/status")
    public ResponseEntity<Map<String,String>> getStatus(@PathVariable Long reportId,Principal principal){

     boolean doesReportBelongtoRetailer = salesReportRepository.doesReportBelongToRetailer(reportId, principal.getName());
     Map<String,String> response = new HashMap<>();

     if(!doesReportBelongtoRetailer){
         response.put("error", "Report does not belong to this retailer - access denied");
         return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
     }
     SalesReport report = salesReportRepository.findById(reportId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"report not found"));
    if(report.getStatus()== ReportStatus.PROCESSING){
        response.put("status", "Report is currently being generated");
        return ResponseEntity.accepted().body(response);
    } else if (report.getStatus()==ReportStatus.ERROR) {
        response.put("error", "Report generation failed");
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
    } else if (report.getStatus()==ReportStatus.GENERATED) {
        response.put("message","Report generated successfully");
        response.put("active","5 hours");
        response.put("reportUrl", s3UploadService.generateReportDownloadUrl(report.getS3FilePath(), report.getRetailer().getID(), Duration.ofHours(5)));
        return ResponseEntity.ok(response);
     }
        response.put("error", "Unknown report status, this could be due to report generation not started");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }


}
