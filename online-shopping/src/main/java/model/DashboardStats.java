package model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DashboardStats {
    
	private long totalUsers;
    private long totalProducts;
    private long totalOrders;
    private double totalRevenue;
    private long outOfStockCount;
}
