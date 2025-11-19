package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.Repository.ProductRepository;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
import com.LevelUpGamer.proyecto.dto.RedeemRequest;
import com.LevelUpGamer.proyecto.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
@Tag(name = "Sistema de Recompensas", description = "Endpoints para el canje de productos utilizando Puntos Duoc (Gamificación).")
public class RewardsController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    // Define los costos y niveles de las recompensas
    private static final Map<Long, Integer> REWARD_COST = Map.of(
            1L, 30000,  // Catan
            2L, 30000,  // Carcassonne
            8L, 40000,  // Mouse Logitech
            9L, 40000,  // Mousepad Razer
            4L, 50000,  // Auriculares HyperX
            3L, 50000,  // Mando Xbox
            7L, 100000  // Silla Gamer
    );
    private static final Map<Long, Integer> REWARD_LEVEL = Map.of(
            1L, 1, 2L, 1, // Nivel 1
            8L, 2, 9L, 2, // Nivel 2
            4L, 3, 3L, 3, // Nivel 3
            7L, 4         // Nivel 4
    );

    /**
     * Canjear una recompensa.
     */
    @Operation(
            summary = "Canjear puntos por un producto",
            description = "Permite a un usuario canjear sus 'puntos gastables' por un producto. " +
                    "Valida tres condiciones: 1. Que la recompensa exista. " +
                    "2. Que el usuario tenga el Nivel requerido. " +
                    "3. Que el usuario tenga saldo suficiente de puntos."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Canje realizado con éxito. Se han descontado los puntos."),
            @ApiResponse(responseCode = "400", description = "Error de validación (Producto inválido, Nivel insuficiente o Puntos insuficientes).")
    })
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemReward(@RequestBody RedeemRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Long productId = request.getProductId();

        // 1. Validar que la recompensa existe
        if (!REWARD_COST.containsKey(productId)) {
            return ResponseEntity.badRequest().body("Error: Recompensa no válida.");
        }

        Integer requiredCost = REWARD_COST.get(productId);
        Integer requiredLevel = REWARD_LEVEL.get(productId);

        // 2. Validar Nivel
        if (user.getUserLevel() < requiredLevel) {
            return ResponseEntity.badRequest().body("Error: No tienes el nivel suficiente para canjear esto.");
        }

        // 3. Validar Puntos GASTABLES
        if (user.getPointsBalance() < requiredCost) {
            return ResponseEntity.badRequest().body("Error: No tienes suficientes puntos para este canje.");
        }

        // 4. ¡Éxito! Realiza el canje
        user.setPointsBalance(user.getPointsBalance() - requiredCost);
        // No tocamos 'totalPointsEarned' (el nivel se mantiene)

        userRepository.save(user);

        return ResponseEntity.ok("¡Canje realizado con éxito!");
    }
}