package model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id @GeneratedValue
    private Long id;
    private String name;
    private String description;
    private double price;
    private String imageUrl;
    private String category;
    private String size;
    private String ageGroup;
    private int stockQuantity;
}

