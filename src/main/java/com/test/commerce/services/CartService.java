package com.test.commerce.services;

import com.test.commerce.dtos.*;
import com.test.commerce.model.Cart;
import com.test.commerce.model.CartItem;
import com.test.commerce.model.Customer;
import com.test.commerce.model.Product;
import com.test.commerce.repositories.CartItemRepository;
import com.test.commerce.repositories.CartRepository;
import com.test.commerce.repositories.CustomerRepository;
import com.test.commerce.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CartService {
    private final int MAX_QUANTITY=20;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private S3UploadService s3UploadService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CustomerRepository customerRepository;

    public void AddProductToCart(Long ProductId, Integer quantity, Long CustomerId) {
        Customer customer = customerRepository.findById(CustomerId).orElseThrow(() -> new UsernameNotFoundException("customer not found"));
        Cart cart = cartRepository.findByCustomerID(CustomerId).orElseGet(() -> {
            Cart cart1 = new Cart();
            cart1.setCustomer(customer);
            return cartRepository.save(cart1);
        });
        if (cart.getQuantity()!=null && (cart.getQuantity() + quantity) > MAX_QUANTITY) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart cannot exceed 20 items.");
        }
        Product product = productRepository.findById(ProductId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (product.getStock() < quantity) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity cannot be greater than stock");
        }
        Optional<CartItem> p = cart.getItems().stream().filter(l->l.getId()== product.getId()).findFirst();
        if(p.isPresent()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"product is already present in cart increase quantity");
        }
        try {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setQuantity(quantity);
            cartItem.setProduct(product);
            cartItemRepository.save(cartItem);
            cart.setQuantity(cart.getQuantity()!=null? cart.getQuantity()+quantity : quantity);
            cartRepository.save(cart);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "somethign went worng" + e.getMessage());
        }
    }

    public CartDto getCart(String email) {
        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        Cart cart = cartRepository.findCartwithfetch(customer.getID()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CART NOT FOUND"));
        CartDto dto = new CartDto();
        dto.setId(cart.getId());
        dto.setQuantity(cart.getQuantity());
        BigDecimal total = new BigDecimal("0");
        List<CartItemDto> items = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            CartItemDto dto1 = new CartItemDto();
            dto1.setId(item.getId());
            dto1.setProductData(new CustomerProductData(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getProduct().getRetailer().getRetailerName(),
                    item.getProduct().getDescription(),
                    item.getProduct().getStock(),
                    item.getProduct().getMrp(),
                    item.getProduct().getCategory().getName(),
                    item.getProduct().getCreatedAt(),
                    item.getProduct().getImageUrl() != null ? s3UploadService.getObjectUrl(item.getProduct().getImageUrl()) : null
            ));
            dto1.setProductQuantity(item.getQuantity());
            items.add(dto1);
            total = total.add(item.getProduct().getMrp().multiply(new BigDecimal(item.getQuantity())));
        }
        dto.setItems(items);
        dto.setTotalPrice(total);
        return dto;
    }
    @Transactional
    public void UpdateCart(Long cartId, List<UpdateCartItemDto> items){
     List<Long> todel = new ArrayList<>();
     Cart cart = cartRepository.findById(cartId)
             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));
     Integer allquantity = items.stream()
                .map(UpdateCartItemDto::getQuantity).reduce(0,Integer::sum);
        Map<Long, UpdateCartItemDto> dtoMap = items.stream()
                .collect(Collectors.toMap(UpdateCartItemDto::getId, Function.identity()));

     if(allquantity>MAX_QUANTITY){
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Quantity of all Products cannot be greater than 20");
     }
     for (CartItem item : cart.getItems()) {
            UpdateCartItemDto dto = dtoMap.get(item.getId());
            if (dto != null) {
                item.setQuantity(dto.getQuantity());
            }
        }
     cart.setQuantity(allquantity);

     Set<Long> incomingItemIds = items.stream()
                .map(UpdateCartItemDto::getId)
                .collect(Collectors.toSet());
     List<Long> deleteIds = cart.getItems().stream().map(CartItem::getId).filter(l->!incomingItemIds.contains(l)).toList();
     cartItemRepository.deleteAllById(deleteIds);
     cartRepository.save(cart);
    }



    public void removeProductFromCart(Long customerId, Long productId) {
        Cart cart = cartRepository.findByCustomerID(customerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found"));

        CartItem cartItem = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not in cart"));

        cartItemRepository.delete(cartItem);

        cart.setQuantity(cart.getQuantity() - cartItem.getQuantity());

        cartRepository.save(cart);
    }



}
