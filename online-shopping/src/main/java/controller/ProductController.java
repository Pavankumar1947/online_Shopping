package controller;

import java.awt.print.Pageable;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import model.PaginatedResponse;
import model.Product;
import repository.ProductRepository;
import service.ProductService;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepo;
    private final ProductService productService;
    
    // to fetch all products.
    @GetMapping
    public List<Product> getAll() {
        return productRepo.findAll();
    }
    
    // fetch a single product by its ID.
    @GetMapping("/{id}")
    public ResponseEntity<Product> getOne(@PathVariable Long id) {
        return productRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //Only an ADMIN can create a product, enforced by @PreAuthorize annotation.
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product add(@RequestBody Product product) {
        return productRepo.save(product);
    }
    
    // update an existing product by its ID.
    // Only an ADMIN can update a product, enforced by @PreAuthorize annotation.
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody Product updated) {
        return productRepo.findById(id).map(p -> {
            p.setName(updated.getName());
            p.setDescription(updated.getDescription());
            p.setPrice(updated.getPrice());
            p.setImageUrl(updated.getImageUrl());
            p.setCategory(updated.getCategory());
            p.setSize(updated.getSize());
            p.setAgeGroup(updated.getAgeGroup());
            p.setStockQuantity(updated.getStockQuantity());
            return ResponseEntity.ok(productRepo.save(p));
        }).orElse(ResponseEntity.notFound().build());
    }

    // to delete a product by its ID.
    // Only an ADMIN can delete a product, enforced by @PreAuthorize annotation.
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!productRepo.existsById(id)) return ResponseEntity.notFound().build();
        productRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
 // Paginated endpoint
    @GetMapping
    public ResponseEntity<PaginatedResponse<Product>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = (Pageable) PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    // Non-paginated endpoint (optional)
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProductsWithoutPagination() {
        return ResponseEntity.ok(productService.getAllProducts());
    }
}

