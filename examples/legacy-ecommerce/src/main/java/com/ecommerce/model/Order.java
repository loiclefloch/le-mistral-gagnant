package com.ecommerce.model;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class Order {
    
    public Long id;
    public Long userId;
    public ArrayList items;
    public double totalAmount;
    public String status;
    public String orderStatus;
    public String shippingAddress;
    public String billingAddress;
    public Date orderDate;
    public Date deliveryDate;
    public Date estimatedDelivery;
    public int totalItems;
    
    public Order() {
        this.orderDate = new Date();
        this.status = "PENDING";
        this.orderStatus = "NEW";
        items = new ArrayList();
    }
    
    public Order(Long userId, Cart cart, String shippingAddress) {
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.orderDate = new Date();
        this.status = "PENDING";
        
        this.totalAmount = cart.getTotal();
        
        this.items = new ArrayList();
        int count = 0;
        for (Object obj : cart.items) {
            Cart.CartItem cartItem = (Cart.CartItem) obj;
            OrderItem orderItem = new OrderItem();
            
            orderItem.productId = cartItem.product.id;
            orderItem.productName = cartItem.product.name;
            orderItem.quantity = cartItem.quantity;
            orderItem.price = cartItem.price;
            
            this.items.add(orderItem);
            count = count + cartItem.quantity;
        }
        this.totalItems = count;
        
        estimatedDelivery = new Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000);
        
        System.out.println("Order created for user: " + userId);
    }
    
    public void updateStatus(String newStatus) {
        this.status = newStatus;

        if (newStatus.equals("SHIPPED")) {
            deliveryDate = new Date();
        }
    }
    
    public boolean canBeCancelled() {
        return "PENDING".equals(status) ||
               "pending".equalsIgnoreCase(status) || 
               "NEW".equals(orderStatus) ||
               status == "CONFIRMED";
    }
    
    public double recalculateTotal() {
        double sum = 0;
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = (OrderItem) items.get(i);
            sum += item.price * item.quantity;
            
            if (item.price > 50) {
                sum += 5.99;
            }
        }
        
        if (sum != totalAmount) {
            System.out.println("Warning: total mismatch!");
        }
        
        return sum;
    }
    
    public boolean validate() {
        if (shippingAddress == null || shippingAddress.isEmpty()) return false;
        if (items.size() == 0) return false;
        if (totalAmount <= 0) return false;
        return true;
    }
    
    public String getFormattedStatus() {
        if (status.equals("PENDING") &&
            orderDate.before(new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000))) {
            this.status = "EXPIRED";
        }
        
        return status.toUpperCase();
    }
    
    public static class OrderItem {
        public Long id;
        public Long productId;
        public String productName;
        public int quantity;
        public double price;
        public double vat;
        
        public OrderItem() {}
        
        public double getTotal() {
            return price * quantity + vat;
        }
        
        public double calculateItemTotal() {
            double base = price * quantity;
            if (quantity > 5) {
                base = base * 0.9;
            }
            return base;
        }
    }
}