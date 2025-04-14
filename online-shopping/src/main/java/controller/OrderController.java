package controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import model.Cart;
import model.CartItem;
import model.Order;
import model.OrderStatus;
import model.Product;
import model.User;
import repository.CartRepository;
import repository.OrderRepository;
import repository.ProductRepository;
import repository.UserRepository;
import util.JwtUtil;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<String> placeOrder(@AuthenticationPrincipal(expression = "username") String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        Cart cart = cartRepo.findByUser(user).orElseThrow();

        if (cart.getItems().isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty");
        }

        double total = 0;

        for (CartItem item : cart.getItems()) {
            Product product = item.getProduct();
            int orderedQty = item.getQuantity();

            if (product.getStockQuantity() < orderedQty) {
                return ResponseEntity.badRequest().body("Insufficient stock for " + product.getName());
            }

            product.setStockQuantity(product.getStockQuantity() - orderedQty);
            total += product.getPrice() * orderedQty;
        }

        cart.getItems().forEach(i -> productRepo.save(i.getProduct()));

        Order order = new Order();
        order.setUser(user);
        order.setItems(cart.getItems());
        order.setTotalAmount(total);
        
        order.setStatus(OrderStatus.PENDING);  // Updated here

        orderRepo.save(order);

        // Clear user's cart
        cart.setItems(new ArrayList<>());
        cartRepo.save(cart);

        return ResponseEntity.ok("Order placed successfully");
    }
    
    
    @GetMapping("/user")
    public List<Order> getUserOrders(@AuthenticationPrincipal(expression = "username") String email) {
        User user = userRepo.findByEmail(email).orElseThrow();
        return orderRepo.findByUser(user);
    }
    
   
    
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newStatusStr = payload.get("status");
        OrderStatus newStatus;

        try {
            // Convert the string to OrderStatus enum (case insensitive)
            newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle invalid status string
            return ResponseEntity.badRequest().body("Invalid status. Allowed values: PENDING, APPROVED, SHIPPED, DELIVERED, REJECTED");
        }

        return orderRepo.findById(id).map(order -> {
            if (OrderStatus.DELIVERED.equals(order.getStatus())) {
                return ResponseEntity.badRequest().body("Delivered orders can't be changed");
            }

            order.setStatus(newStatus);
            orderRepo.save(order);
            return ResponseEntity.ok("Status updated to: " + newStatus);
        }).orElse(ResponseEntity.notFound().build());
    }

    
    
    @GetMapping("/{id}/track")
    public ResponseEntity<Map<String, String>> trackOrder(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        // Check if the Authorization header is provided
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Authorization header missing or malformed"));
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractUsername(token);

        // Ensure the user exists
        User user = userRepo.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "User not found"));
        }

        return orderRepo.findById(id).map(order -> {
            // Check if the order belongs to the logged-in user
            if (!order.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You are not authorized to track this order"));
            }

            // Prepare the status map
            Map<String, String> statusMap = new HashMap<>();
            statusMap.put("orderId", String.valueOf(order.getId()));
            statusMap.put("status", order.getStatus().name()); 
            return ResponseEntity.ok(statusMap);
        }).orElse(ResponseEntity.notFound().build());
    }


}

