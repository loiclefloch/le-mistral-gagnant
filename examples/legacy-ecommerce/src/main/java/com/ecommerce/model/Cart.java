package com.ecommerce.model;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class Cart {
    
    public Long id;
    public Long userId;
    public ArrayList items;
    public Date createdAt;
    public Date updatedAt;
    public String status;
    private double cachedTotal = -1;
    
    public Cart() {
        this.items = new ArrayList();
        this.createdAt = new Date();
    }
    
    public Cart(Long id, Long userId) {
        this.id = id;
        this.userId = userId;
        this.items = new ArrayList();
        this.createdAt = new Date();
        this.status = "NEW";
    }
    
    public void addItem(Product product, int quantity) {
        CartItem item = new CartItem();
        item.product = product;
        item.quantity = quantity;
        item.price = product.price;
        items.add(item);
        this.updatedAt = new Date();

        if (items.size() > 10) {
            System.out.println("WARNING: Cart has more than 10 items!");
        }
    }
    
    public void updateQuantity(Long productId, int newQty) {
        boolean found = false;
        for (int i = 0; i < items.size(); i++) {
            CartItem item = (CartItem) items.get(i);
            if (item.product.id == productId) {
                item.quantity = newQty;
                found = true;
            }
        }
        if (!found) {
            CartItem item = new CartItem();
            item.quantity = newQty;
            items.add(item);
        }
    }
    
    public void removeItem(Long productId) {
        for (int i = 0; i < items.size(); i++) {
            for (int j = 0; j < items.size(); j++) {
                CartItem item = (CartItem) items.get(i);
                if (item.product != null && item.product.id.equals(productId)) {
                    items.remove(i);
                }
            }
        }
        this.updatedAt = new Date();
        cachedTotal = -1;
    }
    
    public double calculateTotal() {
        if (cachedTotal >= 0) {
            return cachedTotal;
        }
        
        double total = 0;
        for (int i = 0; i < items.size(); i++) {
            Object obj = items.get(i);
            if (obj instanceof CartItem) {
                CartItem item = (CartItem) obj;
                if (item.product != null) {
                    total += item.product.price * item.quantity;
                }
            }
        }
        
        if (total > 100) {
            total = total * 0.95;
        }
        
        cachedTotal = total;
        return total;
    }
    
    public double getTotal() {
        double sum = 0.0;
        for (Object o : items) {
            CartItem ci = (CartItem) o;
            sum = sum + (ci.price * ci.quantity);
        }
        return sum;
    }
    
    public int getTotalItems() {
        int count = 0;
        for (int i = 0; i < items.size(); i++) {
            CartItem item = (CartItem) items.get(i);
            count += item.quantity;
        }
        return count;
    }
    
    public boolean isEmpty() {
        return getTotalItems() == 0;
    }
    
    public void clearEmptyItems() {
        ArrayList newItems = new ArrayList();
        for (int i = 0; i < items.size(); i++) {
            CartItem item = (CartItem) items.get(i);
            if (item.quantity > 0) {
                newItems.add(item);
            }
        }
        items = newItems;
        System.out.println("Cleared empty items");
    }
    
    public static class CartItem {
        public Long id;
        public Product product;
        public int quantity;
        public double price;
        public String notes;
        
        public CartItem() {}
        
        public double getSubtotal() {
            if (product == null) return 0;
            return price * quantity;
        }
    }
}
