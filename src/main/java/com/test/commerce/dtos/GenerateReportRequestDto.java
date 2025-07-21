package com.test.commerce.dtos;

import com.test.commerce.enums.ReportType;
import com.test.commerce.validations.DateRangeValidation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@DateRangeValidation
public class GenerateReportRequestDto {
    @NotNull(message = "reportType is required")
    private ReportType reportType;
    @NotNull(message = "startDate is required")
    private LocalDate startDate;
    @NotNull(message = "endDate is required")
    private LocalDate endDate;
}
