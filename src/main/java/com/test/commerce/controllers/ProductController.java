package com.test.commerce.controllers;

import com.test.commerce.dtos.ProductCreationDto;
import com.test.commerce.dtos.ProductData;
import com.test.commerce.model.Product;
import com.test.commerce.model.Retailer;
import com.test.commerce.repositories.RetailorRepository;
import com.test.commerce.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/products/retailer")
@Secured("RETAILER")
public class ProductController {

    @Autowired
    private RetailorRepository retailorRepository;

    @Autowired
    private ProductService productService;

    @PostMapping("/add")
    public ResponseEntity<?> CreateProduct(@Valid @RequestBody ProductCreationDto creationDto, Principal principal){
       String email = principal.getName();

           Product product = productService.AddProduct(creationDto,email);
           return ResponseEntity.status(HttpStatus.CREATED).body("Product Created");
    }

    @GetMapping("/getProducts")
    public ResponseEntity<?> getProducts(@RequestParam(required = false) boolean inStock,Principal principal){
        Retailer retailer = retailorRepository.findByEmail( principal.getName()).orElseThrow(() -> new UsernameNotFoundException("Retailer not found"));
            List<ProductData> products = productService.getProducts(retailer.getID(),inStock);
            return ResponseEntity.ok(products);
    }



}
