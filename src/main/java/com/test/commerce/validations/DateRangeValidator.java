package com.test.commerce.validations;

import com.test.commerce.dtos.GenerateReportRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<DateRangeValidation, GenerateReportRequestDto> {

    @Override
    public boolean isValid(GenerateReportRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            return true;
        }

        return !dto.getStartDate().isAfter(dto.getEndDate());
    }
}