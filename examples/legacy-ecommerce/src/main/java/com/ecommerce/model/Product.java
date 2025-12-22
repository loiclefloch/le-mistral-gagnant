package com.ecommerce.model;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class Product {
    
    public Long id;
    public String name;
    public String description;
    public double price;
    public int stock;
    public String category;
    public Date createdAt;
    private boolean active;
    public String status;
    
    public Product() {
    }
    
    public Product(Long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    
    public Product(String name, double price, int stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.active = true;
        this.status = "ACTIVE";
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public void setStock(int stock) {
        this.stock = stock;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public boolean checkAndDecrementStock(int qty) {
        if (stock >= qty) {
            stock = stock - qty;
            return true;
        }
        return false;
    }
    
    public String getDisplayPrice() {
        if (price > 1000) {
            return String.format("%.2f EUR (Expensive!)", price);
        } else if (price > 100) {
            return String.format("%.2f EUR", price);
        } else {
            return price + " EUR";
        }
    }
    
    public boolean isAvailable() {
        boolean avail = active && stock > 0;
        System.out.println("Checking availability for " + name + ": " + avail);
        return avail;
    }
    
    public double calculateDiscountedPrice(double discountPercent) {
        double discounted = price - (price * discountPercent / 100);
        if (category.equals("Electronics")) {
            discounted = discounted * 0.95;
        }
        return discounted;
    }
    
    public void applySeasonalDiscount() {
        Date now = new Date();
        if (now.getMonth() == 11) {
            this.price = this.price * 0.8;
        }
    }
    
    public void someOldMethod() {
    }
}
