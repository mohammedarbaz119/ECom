package com.test.commerce.jobs;


import com.test.commerce.enums.SaleStatus;
import com.test.commerce.model.*;
import com.test.commerce.repositories.CustomerRepository;
import com.test.commerce.repositories.OrderRepository;
import com.test.commerce.repositories.SaleRepository;
import com.test.commerce.services.EmailService;
import com.test.commerce.services.InventoryService;
import com.test.commerce.services.InvoiceGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDeliveryService {

    private final OrderRepository orderRepository;
    private final SaleRepository saleRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceGenerator invoiceGenerator;
    private final EmailService emailService;
    private final ScheduledExecutorService scheduler= Executors.newScheduledThreadPool(10);;
    private final InventoryService inventoryService;

    private final Map<Long, ScheduledFuture<?>> deliveryJobs = new ConcurrentHashMap<>();

    /**
     * Schedules delivery of an order after a delay
     */
    public void scheduleDelivery(Long orderId, Duration delay) {
        log.info("Scheduling delivery for order {} in {} seconds", orderId, delay.getSeconds());

        Runnable deliveryTask = () -> completeDelivery(orderId);

        ScheduledFuture<?> future = scheduler.schedule(deliveryTask, delay.toSeconds(), TimeUnit.SECONDS);
        deliveryJobs.put(orderId, future);
    }

    /**
     * Completes delivery for an order
     */
    @Transactional
    public void completeDelivery(Long orderId) {
        try {
            Order order = orderRepository.findOrderWithItemsAndProduct(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            Customer customer = customerRepository.findByOrderId(orderId).orElseThrow(() -> new IllegalArgumentException("Customer with orderid "+orderId + "not found: " ));
            if (!order.canBeDelivered()) {
                log.warn("Order {} is not eligible for delivery. Status: {}", orderId, order.getStatus());
                return;
            }
            for(OrderItem item:order.getItems()){
                Product product = item.getProduct();
                Sale sale = new Sale();
                sale.setCustomerEmail(customer.getEmail());
                sale.setRetailer(product.getRetailer());
                sale.setRetailerName(product.getRetailer().getRetailerName());
                sale.setOrderItem(item);
                sale.setSaleDate(LocalDateTime.now());
                sale.setUnitPrice(product.getMrp());
                sale.setTotalAmount(product.getMrp().multiply(new BigDecimal(item.getQuantity())));
                sale.setCommissionAmount( sale.getTotalAmount().multiply(
                        sale.getCommissionRate().divide(new BigDecimal(100), 2, RoundingMode.HALF_UP)
                ));
                sale.setRetailerAmount(sale.getTotalAmount().subtract(sale.getCommissionAmount()));
                sale.setQuantity(item.getQuantity());
                sale.setProductCategory(item.getProduct().getCategory().getName());
                sale.setProduct(product);
                sale.setOrder(order);
                sale.setProductName(item.getProduct().getName());
                sale.setSaleStatus(SaleStatus.DELIVERED);
                sale.setSaleNumber(UUID.randomUUID().toString());
                saleRepository.save(sale);
            }
            order.SetDelivered();
            orderRepository.save(order);
            var a = invoiceGenerator.generateInvoicePdf(order,order.getItems(),order.getCustomer().getEmail());
            emailService.sendInvoiceEmail(customer.getEmail(),a,orderId);
            deliveryJobs.remove(orderId);
            log.info("Order {} marked as delivered", orderId);
        } catch (Exception e) {
            log.error("Failed to complete delivery for order {}: {}", orderId, e.getMessage(), e);
        }
    }

    /**
     * Cancels an ongoing or scheduled delivery
     */
    @Transactional
    public boolean cancelDelivery(Long orderId) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

            if (!order.canBeCancelled()) {
                log.warn("Cannot cancel order {} with status {}", orderId, order.getStatus());
                return false;
            }

            ScheduledFuture<?> job = deliveryJobs.remove(orderId);
            if (job != null && !job.isDone()) {
                job.cancel(true);
                log.info("Cancelled delivery job for order {}", orderId);
            } else {
                log.info("No active delivery job to cancel for order {}", orderId);
            }
            order.CancelOrder();
            orderRepository.save(order);
            returnItemsToInventory(order);
            log.info("Order {} cancelled and items returned to inventory", orderId);
            return true;

        } catch (Exception e) {
            log.error("Failed to cancel delivery for order {}: {}", orderId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Returns items from a cancelled order to inventory
     */
    private void returnItemsToInventory(Order order) {
        order.getItems().forEach(item -> {
            inventoryService.incrementQuantity(item.getProduct().getId(), item.getQuantity());
            log.debug("Returned {} units of product {} to inventory", item.getQuantity(), item.getProduct().getId());
        });
    }

    /**
     * Returns true if an order has an active delivery job scheduled
     */
    public boolean isDeliveryScheduled(Long orderId) {
        ScheduledFuture<?> job = deliveryJobs.get(orderId);
        return job != null && !job.isDone();
    }
}
