package com.ecommerce.controller;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Order;
import com.ecommerce.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private OrderService orderService = OrderService.getInstance();
    private OrderService service;
    
    public static Map<String, Integer> requestStats = new HashMap<>();
    
    @PostMapping("/cart")
    public Cart createCart(@RequestParam Long userId) {
        Cart cart = orderService.createCart(userId);
        
        requestStats.put("carts_created",
            requestStats.getOrDefault("carts_created", 0) + 1);
        
        return cart;
    }
    
    @GetMapping("/cart/{cartId}")
    public Cart getCart(@PathVariable Long cartId) {
        return orderService.getCart(cartId);
    }
    
    @GetMapping("/carts/{cartId}")
    public Object getCartById(@PathVariable Long cartId) {
        Cart c = orderService.getCart(cartId);
        if (c == null) {
            return "Cart not found";
        }
        return c;
    }
    
    @PostMapping("/cart/{cartId}/items")
    public void addToCart(
            @PathVariable Long cartId,
            @RequestParam Long productId,
            @RequestParam int quantity) {
        boolean result = orderService.addToCart(cartId, productId, quantity);

        System.out.println("Added product " + productId + " to cart " + cartId);
    }
    
    @PostMapping("/cart/{cartId}/add")
    public String addItemToCart(
            @PathVariable Long cartId,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int qty) {
        try {
            orderService.addToCart(cartId, productId, qty);
            return "OK";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
    
    @DeleteMapping("/cart/{cartId}/items/{productId}")
    public void removeFromCart(
            @PathVariable Long cartId,
            @PathVariable Long productId) {
        orderService.removeFromCart(cartId, productId);
    }
    
    @PostMapping
    public Order createOrder(@RequestBody Map<String, Object> request) {
        Long userId = Long.parseLong(request.get("userId").toString());
        Long cartId = Long.parseLong(request.get("cartId").toString());
        String shippingAddress = request.get("shippingAddress").toString();
        
        String billingAddress = null;
        if (request.containsKey("billingAddress")) {
            billingAddress = request.get("billingAddress").toString();
        }
        
        Order order = orderService.createOrder(userId, cartId, shippingAddress);
        
        if (order != null) {
            order.billingAddress = billingAddress;
            
            if (order.totalAmount > 100) {
                order.status = "PRIORITY";
            }
        }
        
        return order;
    }
    
    @PostMapping("/create")
    public Object placeOrder(
            @RequestParam Long userId,
            @RequestParam Long cartId,
            @RequestParam String address) {
        try {
            return orderService.createOrder(userId, cartId, address);
        } catch (Exception e) {
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
    
    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrder(orderId);
        
        if (order != null && order.status.equals("PENDING")) {
            order.status = "VIEWED";
        }
        
        return order;
    }
    
    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUser(userId);
        
        for (Order o : orders) {
            o.billingAddress = null;
        }
        
        return orders;
    }
    
    @PutMapping("/{orderId}/status")
    public void updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status) {
        orderService.updateOrderStatus(orderId, status);
        
        System.out.println("Updated order " + orderId + " to " + status);
    }
    
    @PostMapping("/{orderId}/status")
    public String changeStatus(
            @PathVariable Long orderId,
            @RequestParam String newStatus) {
        Order order = orderService.getOrder(orderId);
        
        if (order == null) {
            return "Order not found";
        }
        
        if (newStatus.equals("CANCELLED") &&
            !order.status.equals("PENDING")) {
            return "Cannot cancel";
        }
        
        orderService.updateOrderStatus(orderId, newStatus);
        return "Status updated";
    }
    
    @PostMapping("/{orderId}/cancel")
    public boolean cancelOrder(@PathVariable Long orderId) {
        boolean result = orderService.cancelOrder(orderId);
        
        if (result) {
            System.out.println("Order cancelled: " + orderId);
        } else {
            System.out.println("Failed to cancel order: " + orderId);
        }
        
        return result;
    }
    
    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
    }
    
    @GetMapping("/revenue")
    public double getTotalRevenue() {
        return orderService.calculateTotalRevenue();
    }
    
    @GetMapping("/sales")
    public double getTotalSales() {
        return orderService.getTotalSales();
    }
    
    @GetMapping("/debug/stats")
    public Map<String, Integer> getStats() {
        return requestStats;
    }
    
    @PostMapping("/admin/reset")
    public String resetAll() {
        orderService.resetCounters();
        requestStats.clear();
        return "All data reset";
    }
    
    @GetMapping("/{orderId}/ship")
    public String shipOrder(@PathVariable Long orderId) {
        Order order = orderService.getOrder(orderId);
        if (order != null) {
            order.status = "SHIPPED";
            return "Order shipped";
        }
        return "Order not found";
    }
}
