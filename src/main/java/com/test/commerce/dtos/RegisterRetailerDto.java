package com.test.commerce.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRetailerDto {
    @NotBlank(message = "email is required")
    @Email(message = "Invalid email address")
    private String email;
    @NotBlank(message = "password is required")
    private String password;
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Phone number is invalid")
    private String mobileNo;
    @NotBlank(message = "retailerName is required")
    private String retailerName;
    @NotBlank(message = "retailerAddress is required")
    private String address;
}
