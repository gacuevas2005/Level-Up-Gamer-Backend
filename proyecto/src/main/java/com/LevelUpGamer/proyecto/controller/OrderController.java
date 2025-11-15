package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.model.Order;
import com.LevelUpGamer.proyecto.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Endpoint para crear un pedido (Checkout)
     * Petición: POST http://localhost:8081/api/orders/checkout
     * (Requiere token de autenticación)
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            Order newOrder = orderService.createOrderFromCart(username);

            // (En el futuro, podríamos devolver un DTO de respuesta)
            return ResponseEntity.ok(newOrder);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}