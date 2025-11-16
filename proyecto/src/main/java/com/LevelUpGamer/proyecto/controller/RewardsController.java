package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.Repository.ProductRepository;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
import com.LevelUpGamer.proyecto.dto.RedeemRequest;
import com.LevelUpGamer.proyecto.model.Product;
import com.LevelUpGamer.proyecto.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rewards") // Ruta protegida (requiere token)
public class RewardsController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository; // Para verificar el producto

    // Define los costos y niveles de las recompensas
    // (En una app real, esto estaría en una tabla de la BD)
    private static final Map<Long, Integer> REWARD_COST = Map.of(
            1L, 30000,  // Catan
            2L, 30000,  // Carcassonne
            8L, 40000,  // Mouse Logitech (Asumiendo ID 8)
            9L, 40000,  // Mousepad Razer (Asumiendo ID 9)
            4L, 50000,  // Auriculares HyperX (Asumiendo ID 4)
            3L, 50000,  // Mando Xbox (Asumiendo ID 3)
            7L, 100000 // Silla Gamer (Asumiendo ID 7)
    );
    private static final Map<Long, Integer> REWARD_LEVEL = Map.of(
            1L, 1, 2L, 1, // Nivel 1
            8L, 2, 9L, 2, // Nivel 2
            4L, 3, 3L, 3, // Nivel 3
            7L, 4  // Nivel 4
    );


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
        // ¡OJO! No tocamos 'totalPointsEarned', tal como pediste.

        userRepository.save(user);

        // (Aquí iría la lógica para "entregar" el producto,
        // como crear un pedido con costo 0)

        return ResponseEntity.ok("¡Canje realizado con éxito!");
    }
}
