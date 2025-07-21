package com.test.commerce.controllers;

import com.test.commerce.dtos.AddCartQuantity;
import com.test.commerce.dtos.CartDto;
import com.test.commerce.dtos.CustomerProductData;
import com.test.commerce.dtos.UpdateCartdto;
import com.test.commerce.model.Customer;
import com.test.commerce.model.Order;
import com.test.commerce.repositories.CustomerRepository;
import com.test.commerce.services.CartService;
import com.test.commerce.services.OrderService;
import com.test.commerce.services.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/customer/")
@Secured("CUSTOMER")
public class CustomerContoller {

    @Autowired
    private CartService cartService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<?> getProducts(
            @RequestParam(required = false) Long retailerId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<CustomerProductData> result = productService.searchProducts(
                retailerId, categoryId, search, inStock, sortBy, page, size
        );
        return ResponseEntity.ok(result);
    }

    @GetMapping("/product/{id}")
    public ResponseEntity<?> getProduct(@PathVariable Long id){
        Optional<CustomerProductData> product = productService.getProduct(id);
        if(product.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("product", product.get());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/product/{ProductId}/addtocart")
    public ResponseEntity<?> AddProductToCart(@PathVariable Long ProductId, @Valid @RequestBody AddCartQuantity addCartQuantity, Principal principal){
       Customer customer = customerRepository.findByEmail(principal.getName()).orElseThrow(()->new UsernameNotFoundException("customer not found"));
       cartService.AddProductToCart(ProductId,addCartQuantity.getQuantity(),customer.getID());
       return ResponseEntity.ok("product has been added to cart");
    }

    @PutMapping("/cart/{id}/updatecart")
    public ResponseEntity<?> updateCart(@PathVariable Long id, @Valid @RequestBody UpdateCartdto cartdto){
        cartService.UpdateCart(id,cartdto.getItems());
        return ResponseEntity.ok("cart has been updated");
    }

    @GetMapping("/getCart")
    public ResponseEntity<?> getCart(Principal principal){
            CartDto cart = cartService.getCart(principal.getName());
            return ResponseEntity.ok(cart);
    }

    @GetMapping("/order/history")
    public ResponseEntity<?> gethistory(Principal principal){
        Customer customer = customerRepository.findByEmail(principal.getName()).orElseThrow(()->new UsernameNotFoundException("customer not found"));
        var a = orderService.getOrderSummaryByEmail(customer.getEmail());
        return ResponseEntity.ok(a);
    }


}
