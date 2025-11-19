package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.model.Order;
import com.LevelUpGamer.proyecto.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Gestión de Pedidos", description = "Endpoints para procesar compras (checkout) y gestionar pedidos.")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Endpoint para crear un pedido (Checkout)
     */
    @Operation(
            summary = "Finalizar compra (Checkout)",
            description = "Convierte el carrito del usuario actual en un pedido formal. " +
                    "Calcula el precio final aplicando descuentos (si es usuario Duoc), " +
                    "otorga puntos de fidelidad y actualiza el nivel del usuario. " +
                    "Finalmente, vacía el carrito."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compra realizada con éxito. Devuelve el detalle del pedido."),
            @ApiResponse(responseCode = "400", description = "El carrito está vacío o hubo un error en el proceso."),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado.")
    })
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