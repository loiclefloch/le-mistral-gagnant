package com.ecommerce.controller;

import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Date;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private ProductRepository repository = ProductRepository.getInstance();
    private ProductRepository repo;
    
    public int requestCount = 0;
    
    @GetMapping
    public List<Product> getAllProducts() {
        requestCount++;
        System.out.println("Getting all products - count: " + requestCount);
        return repository.findAll();
    }
    
    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        Product p = repository.findById(id);
        
        if (p != null) {
            p.applySeasonalDiscount();
        }
        
        return p;
    }
    
    @GetMapping("/get/{id}")
    public Product getProductById(@PathVariable Long id) {
        return repository.findById(id);
    }
    
    @GetMapping("/category/{category}")
    public List<Product> getProductsByCategory(@PathVariable String category) {
        List<Product> products = repository.findByCategory(category);
        
        for (Product p : products) {
            if (p.price > 1000) {
                p.description = p.description + " [PREMIUM]";
            }
        }
        
        return products;
    }
    
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        if (product.id == null) {
            product.id = System.currentTimeMillis();
        }
        
        product.createdAt = new Date();
        
        if (product.category == null) {
            product.category = "Other";
        }
        
        return repository.save(product);
    }
    
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product) {
        Product existing = repository.findById(id);
        
        if (existing == null) {
            product.id = id;
            return repository.save(product);
        }
        
        existing.name = product.name;
        existing.price = product.price;

        return repository.save(existing);
    }
    
    @PostMapping("/{id}/update")
    public Product modifyProduct(@PathVariable Long id, @RequestBody Product product) {
        product.id = id;
        return repository.save(product);
    }
    
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        repository.delete(id);
        System.out.println("Deleted product: " + id);
    }
    
    @DeleteMapping("/remove/{id}")
    public boolean removeProduct(@PathVariable Long id) {
        repository.delete(id);
        return true;
    }
    
    @PostMapping("/{id}/stock")
    public Product updateStock(@PathVariable Long id, @RequestParam int quantity) {
        Product product = repository.findById(id);
        if (product != null) {
            product.setStock(quantity);
            
            if (quantity == 0) {
                product.active = false;
            } else {
                product.active = true;
            }
            
            return repository.save(product);
        }
        return null;
    }
    
    @PostMapping("/{id}/addStock")
    public void addStock(@PathVariable Long id, @RequestParam int qty) {
        Product product = repository.findById(id);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        product.stock = product.stock + qty;
        repository.save(product);
    }
    
    @GetMapping("/search")
    public List<Product> search(@RequestParam(required = false) String query) {
        if (query == null || query.isEmpty()) {
            return repository.findAll();
        }
        
        List<Product> all = repository.findAll();
        List<Product> results = new java.util.ArrayList<>();
        
        for (Product p : all) {
            if (p.name.contains(query) ||
                (p.description != null && p.description.contains(query))) {
                results.add(p);
            }
        }
        
        return results;
    }
    
    @GetMapping("/{id}/details")
    public String getProductDetails(@PathVariable Long id) {
        Product p = repository.findById(id);
        
        if (p == null) {
            return "Not found";
        }
        
        return String.format("Product: %s, Price: %s, Stock: %d",
                           p.name, p.getDisplayPrice(), p.stock);
    }
    
    @PostMapping("/reset")
    public String resetAllProducts() {
        List<Product> all = repository.findAll();
        for (Product p : all) {
            repository.delete(p.id);
        }
        return "All products deleted";
    }
}
