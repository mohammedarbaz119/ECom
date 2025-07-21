package com.test.commerce.controllers;

import com.test.commerce.jobs.OrderDeliveryService;
import com.test.commerce.model.Customer;
import com.test.commerce.model.Order;
import com.test.commerce.repositories.CustomerRepository;
import com.test.commerce.repositories.OrderRepository;
import com.test.commerce.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/customer/orders")
@Secured("CUSTOMER")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderDeliveryService orderDeliveryService;

    @PostMapping("/{cartId}/placeOrder")
    public ResponseEntity<Map<String, Long>> placeOrder(@PathVariable Long cartId, Principal principal) {
        Long orderid = orderService.PlaceOrder(cartId, principal.getName());
        Map<String, Long> r = new HashMap<>();
        r.put("orderId", orderid);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/order/{orderId}/status")
    public ResponseEntity<String> getOrderStatus(@PathVariable Long orderId,Principal principal) {
        boolean doesOrderBelongToCustomer = orderRepository.doesOrderBelongToCustomer(orderId, principal.getName());
        if(!doesOrderBelongToCustomer){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("order does belong this customer access denied");
        }
        boolean Inprocess = orderDeliveryService.isDeliveryScheduled(orderId);
        if (Inprocess) {
            return ResponseEntity.ok("delivery is currently going");
        } else {
            Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ORDER NOT FOUND"));
            return ResponseEntity.ok("ORDER IS " + order.getStatus());
        }
    }
    @PostMapping("/order/{orderId}/cancel")
    public ResponseEntity<String> Cancel(@PathVariable Long orderId,Principal principal) {
        boolean doesOrderBelongToCustomer = orderRepository.doesOrderBelongToCustomer(orderId, principal.getName());
        if(!doesOrderBelongToCustomer){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("order does belong this customer access denied");
        }
        boolean Inprocess = orderDeliveryService.isDeliveryScheduled(orderId);
        if (Inprocess) {
            boolean done = orderDeliveryService.cancelDelivery(orderId);
            if(done){
                return ResponseEntity.ok("order with orderId "+orderId+" has been cancelled");
            }else{
                return ResponseEntity.status(HttpStatus.CONFLICT).body("order cannot be canceled");
            }
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Order cannot be cancelled");
        }
    }
    @PostMapping("/order/{orderId}/return")
    public ResponseEntity<?> Return(@PathVariable Long orderId,Principal principal) {
        boolean doesOrderBelongToCustomer = orderRepository.doesOrderBelongToCustomer(orderId, principal.getName());
        if (!doesOrderBelongToCustomer) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("order does belong this customer access denied");
        }
        boolean Inprocess = orderDeliveryService.isDeliveryScheduled(orderId);
        if (Inprocess) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Order with "+ orderId +" is currently being delivered it cannot be returned before delivery ");
        }
        boolean returned = orderService.ReturnOrder(orderId);
        if(returned){
            return ResponseEntity.ok("order returned");
        }else {
            Map<String,String> repsonse = new HashMap<>();
            repsonse.put("message","there some error in retuning the order, this could a result of a error in the server itself");
            repsonse.put("orderId",orderId.toString());
            return ResponseEntity.internalServerError().body(repsonse);
        }
    }



}
