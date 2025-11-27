package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.Repository.OrderRepository;
import com.LevelUpGamer.proyecto.Repository.ReviewRepository;
import com.LevelUpGamer.proyecto.Repository.UserRepository; // <--- NUEVO IMPORT
import com.LevelUpGamer.proyecto.model.Order;
import com.LevelUpGamer.proyecto.model.User; // <--- NUEVO IMPORT
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Panel de Administrador", description = "Estadísticas, moderación y gestión de usuarios.")
public class AdminDashboardController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository; // <--- INYECCIÓN NUEVA PARA GESTIONAR USUARIOS

    /**
     * Devuelve las estadísticas de ventas (Hoy, Esta Semana, Este Mes, Este Año).
     */
    @Operation(summary = "Estadísticas de ventas", description = "Devuelve ingresos totales y cantidad de pedidos.")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSalesStats() {
        LocalDateTime now = LocalDateTime.now();

        // Calcular rangos de fechas
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        LocalDateTime startOfYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate().atStartOfDay();

        Map<String, Object> stats = new HashMap<>();

        // Ventas y Pedidos
        stats.put("salesToday", orderRepository.sumTotalSalesBetween(startOfDay, now));
        stats.put("ordersToday", orderRepository.countOrdersBetween(startOfDay, now));
        stats.put("salesWeek", orderRepository.sumTotalSalesBetween(startOfWeek, now));
        stats.put("salesMonth", orderRepository.sumTotalSalesBetween(startOfMonth, now));
        stats.put("salesYear", orderRepository.sumTotalSalesBetween(startOfYear, now));

        // Últimos 10 pedidos
        List<Order> recentOrders = orderRepository.findTop10ByOrderByOrderDateDesc();
        stats.put("recentOrders", recentOrders);

        return ResponseEntity.ok(stats);
    }

    // --- NUEVA SECCIÓN: GESTIÓN DE USUARIOS ---

    /**
     * Obtener todos los usuarios registrados.
     * RUTA: GET /api/admin/users
     */
    @Operation(summary = "Listar usuarios", description = "Obtiene la lista de todos los usuarios registrados.")
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Banear o Desbanear a un usuario.
     * RUTA: PUT /api/admin/users/{id}/ban
     */
    @Operation(summary = "Banear/Desbanear", description = "Invierte el estado de bloqueo del usuario.")
    @PutMapping("/users/{id}/ban")
    public ResponseEntity<?> toggleUserBan(@PathVariable Long id) {
        return userRepository.findById(id).map(user -> {
            // Protección: No permitir que un Admin se banee a sí mismo o a otro Admin
            if (user.getUserRole().equals("ROLE_ADMIN")) {
                return ResponseEntity.badRequest().body("No puedes banear a un administrador.");
            }

            // Invertir el estado (Si era false pasa a true, y viceversa)
            boolean currentState = user.isLocked();
            user.setLocked(!currentState);

            userRepository.save(user);

            String status = user.isLocked() ? "baneado" : "desbaneado";
            return ResponseEntity.ok("Usuario " + status + " exitosamente.");
        }).orElse(ResponseEntity.notFound().build());
    }

    // ------------------------------------------

    /**
     * Moderación: Eliminar un comentario inadecuado.
     */
    @Operation(summary = "Eliminar comentario", description = "Borrar una reseña ofensiva o spam.")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId) {
        if (reviewRepository.existsById(reviewId)) {
            reviewRepository.deleteById(reviewId);
            return ResponseEntity.ok("Comentario eliminado por moderación.");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}