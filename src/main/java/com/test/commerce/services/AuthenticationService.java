package com.test.commerce.services;


import com.test.commerce.enums.Role;
import com.test.commerce.dtos.LoginUserDto;
import com.test.commerce.dtos.RegisterCustomerDto;
import com.test.commerce.dtos.RegisterRetailerDto;
import com.test.commerce.model.Customer;
import com.test.commerce.model.Retailer;
import com.test.commerce.model.User;
import com.test.commerce.repositories.CustomerRepository;
import com.test.commerce.repositories.RetailorRepository;
import com.test.commerce.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RetailorRepository retailorRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            RetailorRepository retailorRepository,
            CustomerRepository customerRepository,
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.retailorRepository = retailorRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User CustomerSignup(RegisterCustomerDto input) {
        Customer customer = new Customer();
        customer.setPassword(passwordEncoder.encode(input.getPassword()));
        customer.setEmail(input.getEmail());
        customer.setRole(Role.CUSTOMER);
        customer.setShippingAddress(input.getAddress());
        customer.setVerified(false);
        if(input.getMobileNo()!=null){
            customer.setPhoneNumber(input.getMobileNo());
        }

        return customerRepository.save(customer);
    }

    public User RetailerSignup(RegisterRetailerDto input) {
        Retailer retailer = new Retailer();
        retailer.setPassword(passwordEncoder.encode(input.getPassword()));
        retailer.setEmail(input.getEmail());
        retailer.setRetailerName(input.getRetailerName());
        retailer.setRetailerAddress(input.getAddress());
        retailer.setRole(Role.RETAILER);
        retailer.setVerified(false);
        if(input.getMobileNo()!=null){
            retailer.setPhoneNumber(input.getMobileNo());
        }

        return retailorRepository.save(retailer);
    }

    public User authenticate(LoginUserDto input) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        input.getEmail(),
                        input.getPassword()
                )
        );

        return userRepository.findByEmail(input.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}