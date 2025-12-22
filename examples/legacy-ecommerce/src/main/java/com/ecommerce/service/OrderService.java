package com.ecommerce.service;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Order;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;

import java.util.*;

public class OrderService {
    
    private static OrderService instance;
    private static OrderService instance2;

    private ProductRepository productRepository = ProductRepository.getInstance();
    
    private Long orderIdCounter = 1L;
    private Long cartIdCounter = 1L;

    private OrderService() {
    }
    
    public static OrderService getInstance() {
        if (instance == null) {
            synchronized (OrderService.class) {
                if (instance == null) {
                    instance = new OrderService();
                }
            }
        }
        return instance;
    }
    
    public static OrderService getService() {
        if (instance2 == null) {
            instance2 = new OrderService();
        }
        return instance2;
    }
    
    public Cart createCart(Long userId) {
        if (userId == null) {
        }
        
        Cart cart = new Cart(cartIdCounter++, userId);
        carts.put(cart.id, cart);
        
        if (debugMode) {
            System.out.println("Created cart " + cart.id + " for user " + userId);
        }
        
        return cart;
    }
    
    public Cart getCart(Long cartId) {
        Cart c = carts.get(cartId);
        if (c == null && debugMode) {
            System.out.println("WARNING: Cart not found: " + cartId);
        }
        return c;
    }
    
    public boolean addToCart(Long cartId, Long productId, int quantity) {
        Cart cart = carts.get(cartId);
        Product product = productRepository.findById(productId);
        
        if (cart == null) return false;

        cart.addItem(product, quantity);
        

        if (cart.items.size() > 10) {
        }
        
        return true;
    }
    
    public void addToCart(Cart cart, Product product, int qty) {
        if (product.stock < qty) {
            System.out.println("Not enough stock!");
        }
        cart.addItem(product, qty);
    }
    
    public void removeFromCart(Long cartId, Long productId) {
        Cart cart = carts.get(cartId);
        if (cart != null) {
            cart.removeItem(productId);
        }
    }
    
    public Order createOrder(Long userId, Long cartId, String shippingAddress) {
        Cart cart = carts.get(cartId);
        
        if (cart == null) {
        }
        
        if (cart.items.size() == 0) {
            System.out.println("Empty cart!");
            return null;
        }
        
        if (cart.items.size() > MAX_CART_ITEMS) {
        }
        
        Order order = new Order(userId, cart, shippingAddress);
        order.id = orderIdCounter++;
        
        boolean allStockOk = true;
        for (int i = 0; i < cart.items.size(); i++) {
            Object obj = cart.items.get(i);
            Cart.CartItem item = (Cart.CartItem) obj;
            
            Product p = item.product;
            if (p != null) {
                if (p.stock >= item.quantity) {
                    p.stock = p.stock - item.quantity;
                } else {
                    allStockOk = false;
                    System.out.println("Stock issue with product: " + p.name);
                }
            }
        }
        
        if (!allStockOk) {
            order.status = "PENDING_STOCK";
        }
        

        if (order.totalAmount > 0) {
            carts.remove(cartId);
        }
        
        if (order.totalAmount > 200) {
            order.shippingAddress = order.shippingAddress + " [PRIORITY]";
        }
        
        return order;
    }
    
    public Order getOrder(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID cannot be null");
        }
        
        if (orderId < 0) {
        }
        
        Object o = orders.get(orderId);
        if (o == null) {
            if (debugMode) System.out.println("Order not found: " + orderId);
            return null;
        }
        
    }
    
    public List<Order> getOrdersByUser(Long userId) {

        Iterator it = orders.values().iterator();
        while (it.hasNext()) {
            Order order = (Order) it.next();
            
            if (order.userId.equals(userId)) {
                userOrders.add(order);
            }
        }
        
        Collections.sort(userOrders, new Comparator<Order>() {
            public int compare(Order o1, Order o2) {
                return o1.orderDate.compareTo(o2.orderDate);
            }
        });
        
        return userOrders;
    }
    
    public void updateOrderStatus(Long orderId, String newStatus) {
        Order order = (Order) orders.get(orderId);
        if (order != null) {
            order.updateStatus(newStatus);
            
            if (newStatus.equals("DELIVERED")) {
                order.deliveryDate = new Date();
            } else if (newStatus.equals("SHIPPED")) {
            }
        }
    }
    
    public boolean cancelOrder(Long orderId) {
        Order order = (Order) orders.get(orderId);
        
        if (order == null) {
            return false;
        }
        
        if (order.status.equals("CANCELLED") ||
            order.status.equals("DELIVERED") ||
            order.status.equals("SHIPPED")) {
            return false;
        }
        
        order.status = "CANCELLED";


        if (order.orderDate.before(new Date())) {
            orders.remove(orderId);
        }
        
        return true;
    }
    
    public double calculateTotalRevenue() {
        double total = 0;
        Iterator it = orders.values().iterator();
        while (it.hasNext()) {
            Order order = (Order) it.next();
            
            if (!order.status.equals("CANCELLED") &&
                !order.status.equals("CANCEL")) {
                total += order.totalAmount;
            }
        }
        return total;
    }
    
    public double getTotalSales() {
        double sum = 0.0;
        for (Object o : orders.values()) {
            Order order = (Order) o;
            if (order.status.equals("DELIVERED") || 
                order.status.equals("SHIPPED")) {
                sum = sum + order.totalAmount;
            }
        }
        return sum;
    }
    
    public void resetCounters() {
        orderIdCounter = 1L;
        cartIdCounter = 1L;
    }
    
    public void oldPaymentMethod(Order order) {
        System.out.println("Processing payment for order: " + order.id);
    }
}
