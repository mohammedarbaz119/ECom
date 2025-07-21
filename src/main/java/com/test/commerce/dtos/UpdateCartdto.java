package com.test.commerce.dtos;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UpdateCartdto {
    @Valid
    private List<UpdateCartItemDto> items = new ArrayList<>();
}
