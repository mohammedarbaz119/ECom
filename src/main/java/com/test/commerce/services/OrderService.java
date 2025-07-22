package com.test.commerce.services;

import com.test.commerce.dtos.OrderDTO;
import com.test.commerce.dtos.OrderItemDTO;
import com.test.commerce.dtos.OrderSummaryDTO;
import com.test.commerce.enums.OrderStatus;
import com.test.commerce.jobs.OrderDeliveryService;
import com.test.commerce.model.*;
import com.test.commerce.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final Duration DELIVERY_DURATION = Duration.ofSeconds(10);
    private final InventoryService inventoryService;
    private final CustomerRepository customerRepository;
    private final OrderDeliveryService orderDeliveryService;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final SaleRepository saleRepository;


    @Transactional
    public Long PlaceOrder(Long cartId, String email) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CART NOT FOUND"));
        Customer customer = customerRepository.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "customer not found"));
        if(cart.getItems().isEmpty()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"cart cannot be empty");
        }
        Order order = new Order();
        order.setCustomer(customer);
        order.setOrderPlacedAt(LocalDateTime.now());
        order.setDeliveryAddress(customer.getShippingAddress());
        BigDecimal totalprice = new BigDecimal(0);
        List<OrderItem> list = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(item.getProduct());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setPrice(item.getProduct().getMrp().multiply(new BigDecimal(item.getQuantity())));
            totalprice = totalprice.add(orderItem.getPrice());
            list.add(orderItem);
            inventoryService.decrementQuantity(item.getProduct().getId(), item.getQuantity());
        }
        order.setTotalamt(totalprice);
        order.setItems(list);
        order = orderRepository.save(order);
        orderItemRepository.saveAll(list);
        orderDeliveryService.scheduleDelivery(order.getId(), DELIVERY_DURATION);
        cartItemRepository.deleteAllById(cart.getItems().stream().map(CartItem::getId).toList());
        cart.getItems().clear();
        cart.setQuantity(0);
        cartRepository.save(cart);
        return order.getId();
    }

    @Transactional
    public boolean ReturnOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ORDER NOT FOUND"));
            if (order.getStatus() == OrderStatus.CANCELLED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Cancelled orders cannot be retuned");
            }
            for (OrderItem item : order.getItems()) {
                Sale sale = item.getSale();
                sale.setReturned();
                inventoryService.incrementQuantity(item.getProduct().getId(), item.getQuantity());
                saleRepository.save(sale);
            }
            order.SetDelivered();
            orderRepository.save(order);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public OrderSummaryDTO getOrderSummaryByEmail(String email) {
        List<Order> orders = customerRepository.getOrders(email);

        List<OrderDTO> orderDTOs = orders.stream().map(order -> {
            List<OrderItemDTO> itemDTOs = order.getItems().stream().map(item -> {

                return new OrderItemDTO(
                        item.getId(),
                        item.getProduct().getName(),
                        item.getProduct().getCategory().getName(),
                        item.getQuantity(),
                        item.getPrice()
                );
            }).collect(Collectors.toList());

            return new OrderDTO(
                    order.getId(),
                    order.getDeliveryAddress(),
                    order.getTotalamt(),
                    order.getStatus(),
                    order.getOrderPlacedAt(),
                    order.getDeliveredDate(),
                    order.getReturnedDate(),
                    itemDTOs
            );
        }).collect(Collectors.toList());

        BigDecimal totalAmount = orderDTOs.stream()
                .map(OrderDTO::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderSummaryDTO(orderDTOs, orderDTOs.size(), totalAmount);
    }



}
