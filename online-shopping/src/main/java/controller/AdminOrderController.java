package controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Order;
import model.OrderStatus;
import repository.OrderRepository;

@Slf4j
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderRepository orderRepo;
    
    @GetMapping
    public List<Order> getAllOrders() {
        log.info("Fetching all orders");

        try {
            List<Order> orders = orderRepo.findAll();
            log.info("Fetched {} orders successfully", orders.size());
            return orders;
        } catch (Exception e) {
            log.error("Error while fetching orders", e);
            throw new RuntimeException("Unable to fetch orders at the moment", e); 
        }
    }

    
    @PutMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return orderRepo.findById(id).map(order -> {
            OrderStatus currentStatus = order.getStatus();
            OrderStatus newStatus;

            try {
                newStatus = OrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid status. Allowed values: APPROVED, SHIPPED, DELIVERED, REJECTED");
            }

            if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.REJECTED) {
                return ResponseEntity.badRequest().body("Cannot change status. Order already marked as " + currentStatus);
            }

            order.setStatus(newStatus); 
            orderRepo.save(order);
            return ResponseEntity.ok("Order status updated to " + newStatus);
        }).orElse(ResponseEntity.notFound().build());
    }

    
    @PutMapping("/{id}/tracking")
    public ResponseEntity<String> updateTracking(
            @PathVariable Long id,
            @RequestBody Map<String, String> trackingData) {

        return orderRepo.findById(id).map(order -> {
            order.setTrackingNumber(trackingData.get("trackingNumber"));
            order.setCourierService(trackingData.get("courierService"));
            order.setDeliveryETA(trackingData.get("deliveryETA"));
            orderRepo.save(order);
            return ResponseEntity.ok("Tracking info updated");
        }).orElse(ResponseEntity.notFound().build());
    }

}


