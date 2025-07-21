package com.test.commerce.controllers;

import com.test.commerce.dtos.*;
import com.test.commerce.model.User;
import com.test.commerce.repositories.UserRepository;
import com.test.commerce.services.AuthenticationService;
import com.test.commerce.services.EmailService;
import com.test.commerce.services.JwtService;
import com.test.commerce.services.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;

    @PostMapping("/customer/register")
    public ResponseEntity<String> RegisterCustomer(@Valid @RequestBody RegisterCustomerDto customerDto){
        if(userRepository.existsByEmail(customerDto.getEmail())){
            return ResponseEntity.badRequest().body("User already exists. Please login.");
        }
        User user = authenticationService.CustomerSignup(customerDto);
        String otp = otpService.generateOtp(customerDto.getEmail());
        try {
            emailService.sendSimpleEmail(
                    customerDto.getEmail(),
                    "Your OTP Code for registration",
                    "Your OTP for registration is: " + otp + "\nIt is valid for 5 minutes."
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send OTP");
        }

        return ResponseEntity.ok("OTP sent to " + customerDto.getEmail());
    }

    @PostMapping("/retailer/register")
    public ResponseEntity<String> RegisterRetailor(@Valid @RequestBody RegisterRetailerDto customerDto){
        if(userRepository.existsByEmail(customerDto.getEmail())){
            return ResponseEntity.badRequest().body("User already exists. Please login.");
        }
        User user = authenticationService.RetailerSignup(customerDto);
        String otp = otpService.generateOtp(customerDto.getEmail());
        try {
            emailService.sendSimpleEmail(
                    customerDto.getEmail(),
                    "Your OTP Code for registration",
                    "Your OTP for registration is: " + otp + "\nIt is valid for 5 minutes."
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send OTP");
        }

        return ResponseEntity.ok("OTP sent to " + customerDto.getEmail());
    }

    @PostMapping("/otp/register/verify")
    public ResponseEntity<TokenResponse> verifyAndSendToken(@Valid @RequestBody OtpRequest otpRequest) {
        boolean valid = otpService.validateOtp(otpRequest.getEmail(), otpRequest.getOtp());
        if (!valid) {
            var a = new TokenResponse();
            a.setMessage("invalid or expired Otp");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(a);
        } else {
            User user = userRepository.findByEmail(otpRequest.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            user.setVerified(true);
            userRepository.save(user);
            String token = jwtService.generateToken(user);
            TokenResponse tk = new TokenResponse();
            tk.setToken(token);
            return ResponseEntity.status(201).body(tk);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginUserDto userDto){
        if(!userRepository.existsByEmail(userDto.getEmail())){
            return ResponseEntity.badRequest().body("User doesn't exists. Please register.");
        }
        User user = authenticationService.authenticate(userDto);
        String otp = otpService.generateOtp(userDto.getEmail());
        try {
            emailService.sendSimpleEmail(
                    userDto.getEmail(),
                    "Your OTP Code for login",
                    "Your OTP for login is: " + otp + "\nIt is valid for 5 minutes."
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send OTP");
        }

        return ResponseEntity.ok("OTP sent to " + userDto.getEmail());
    }

    @PostMapping("/otp/login/verify")
    public ResponseEntity<TokenResponse> verifyAndSendTokenforLogin(@Valid @RequestBody OtpRequest otpRequest) {
        boolean valid = otpService.validateOtp(otpRequest.getEmail(), otpRequest.getOtp());
        if (!valid) {
            var a = new TokenResponse();
            a.setMessage("invalid or expired Otp");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(a);
        } else {
            User user = userRepository.findByEmail(otpRequest.getEmail()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            user.setVerified(true);
            userRepository.save(user);
            String token = jwtService.generateToken(user);
            TokenResponse tk = new TokenResponse();
            tk.setToken(token);
            return ResponseEntity.status(200).body(tk);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<String> h(){
        return  ResponseEntity.ok("gfddg");
    }

}
