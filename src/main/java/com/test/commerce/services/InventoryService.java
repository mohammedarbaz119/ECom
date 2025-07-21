package com.test.commerce.services;

import com.test.commerce.model.Product;
import com.test.commerce.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryService {
   @Autowired
    private ProductService productService;

   @Autowired
    private ProductRepository productRepository;

   public void incrementQuantity(Long ProductId,Integer quantity){
    Product product = productRepository.findById(ProductId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"product not found"));
    product.setStock(product.getStock()+quantity);
    productRepository.save(product);
   }
   public void decrementQuantity(Long ProductId,Integer quantity){
       Product product = productRepository.findById(ProductId).orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"product not found"));
       product.setStock(product.getStock()-quantity);
       productRepository.save(product);
   }
}
