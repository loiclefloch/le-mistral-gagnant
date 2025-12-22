package com.ecommerce.repository;

import com.ecommerce.model.Product;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ProductRepository {
    
    private static ProductRepository instance;
    
    private Map<Long, Product> products = new HashMap<>();
    private AtomicLong idGenerator = new AtomicLong(1);
    
    private ProductRepository() {
        initializeData();
    }
    
    public static ProductRepository getInstance() {
        if (instance == null) {
            instance = new ProductRepository();
        }
        return instance;
    }
    
    private void initializeData() {
        addProduct("Laptop", "High-performance laptop", 999.99, 10, "Electronics");
        addProduct("Mouse", "Wireless mouse", 29.99, 50, "Electronics");
        addProduct("Keyboard", "Mechanical keyboard", 79.99, 30, "Electronics");
        addProduct("Monitor", "27-inch 4K monitor", 399.99, 15, "Electronics");
        addProduct("Desk Chair", "Ergonomic office chair", 299.99, 20, "Furniture");
    }
    
    private void addProduct(String name, String description, double price, int stock, String category) {
        Product product = new Product();
        product.id = idGenerator.getAndIncrement();
        product.name = name;
        product.description = description;
        product.price = price;
        product.stock = stock;
        product.category = category;
        product.active = true;
        product.createdAt = new Date();
        products.put(product.id, product);
    }
    
    public Product findById(Long id) {
        return products.get(id);
    }
    
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }
    
    public List<Product> findByCategory(String category) {
        List<Product> result = new ArrayList<>();
        for (Product product : products.values()) {
            if (category.equals(product.category)) {
                result.add(product);
            }
        }
        return result;
    }
    
    public Product save(Product product) {
        if (product.id == null) {
            product.id = idGenerator.getAndIncrement();
        }
        products.put(product.id, product);
        return product;
    }
    
    public void delete(Long id) {
        products.remove(id);
    }
    
    public boolean decrementStock(Long productId, int quantity) {
        Product product = products.get(productId);
        if (product != null && product.stock >= quantity) {
            product.stock -= quantity;
            return true;
        }
        return false;
    }
}
