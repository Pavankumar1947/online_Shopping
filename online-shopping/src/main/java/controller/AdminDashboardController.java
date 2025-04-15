package controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.DashboardStats;
import model.Order;
import repository.OrderRepository;
import repository.ProductRepository;
import repository.UserRepository;

@Slf4j
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final UserRepository userRepo;
    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    
    
    @GetMapping
    public DashboardStats getStats() {
        log.info("Fetching admin dashboard stats");

        long userCount = 0;
        long productCount = 0;
        long orderCount = 0;
        double revenue = 0;
        long outOfStock = 0;

        try {
            userCount = userRepo.count();
            productCount = productRepo.count();
            orderCount = orderRepo.count();
            revenue = orderRepo.findAll().stream().mapToDouble(Order::getTotalAmount).sum();
            outOfStock = productRepo.findAll().stream().filter(p -> p.getStockQuantity() <= 0).count();

            log.info("Admin dashboard stats fetched successfully.");
        } catch (Exception e) {
            log.error("Error while fetching dashboard stats", e);
        }

        return new DashboardStats(userCount, productCount, orderCount, revenue, outOfStock);
    }
}
