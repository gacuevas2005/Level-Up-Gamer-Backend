package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.Repository.OrderRepository;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
import com.LevelUpGamer.proyecto.model.Order;
import com.LevelUpGamer.proyecto.model.User;
import com.LevelUpGamer.proyecto.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Endpoint para crear un pedido (Checkout)
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Order newOrder = orderService.createOrderFromCart(username);
            return ResponseEntity.ok(newOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * ¡NUEVO! Obtener historial de boletas del usuario logueado
     * GET http://localhost:8081/api/orders/my-orders
     */
    @GetMapping("/my-orders")
    public ResponseEntity<List<Order>> getMyOrders() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        List<Order> orders = orderRepository.findByUserIdWithItems(user.getId());

        return ResponseEntity.ok(orders);
    }
}