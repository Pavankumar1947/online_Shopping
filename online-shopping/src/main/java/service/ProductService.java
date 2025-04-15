package service;

import java.awt.print.Pageable;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import model.PaginatedResponse;
import model.Product;
import repository.ProductRepository;

@Service
public class ProductService {
	
	@Autowired
    private ProductRepository productRepo;
	
	public ProductService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }
	
	// Paginated response
	public PaginatedResponse<Product> getAllProducts(Pageable pageable) {
        Page<Product> page = productRepo.findAll(pageable);
        return new PaginatedResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getTotalPages(),
            page.getTotalElements()
        );
    }
	
	// Non-paginated (for reference)
    public List<Product> getAllProducts() {
        return productRepo.findAll();
    }

}
