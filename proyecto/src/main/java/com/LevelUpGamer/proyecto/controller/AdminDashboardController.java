package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.Repository.OrderRepository;
import com.LevelUpGamer.proyecto.Repository.ReviewRepository;
import com.LevelUpGamer.proyecto.model.Order;
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
@Tag(name = "Panel de Administrador", description = "Estadísticas de ventas y moderación.")
public class AdminDashboardController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    /**
     * Devuelve las estadísticas de ventas (Hoy, Esta Semana, Este Mes, Este Año).
     */
    @Operation(summary = "Estadísticas de ventas", description = "Devuelve ingresos totales y cantidad de pedidos por períodos de tiempo.")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSalesStats() {
        LocalDateTime now = LocalDateTime.now();

        // Calcular rangos de fechas
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toLocalDate().atStartOfDay();
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
        LocalDateTime startOfYear = now.with(TemporalAdjusters.firstDayOfYear()).toLocalDate().atStartOfDay();

        Map<String, Object> stats = new HashMap<>();

        // Ventas de HOY
        stats.put("salesToday", orderRepository.sumTotalSalesBetween(startOfDay, now));
        stats.put("ordersToday", orderRepository.countOrdersBetween(startOfDay, now));

        // Ventas de la SEMANA
        stats.put("salesWeek", orderRepository.sumTotalSalesBetween(startOfWeek, now));

        // Ventas del MES
        stats.put("salesMonth", orderRepository.sumTotalSalesBetween(startOfMonth, now));

        // Ventas del AÑO
        stats.put("salesYear", orderRepository.sumTotalSalesBetween(startOfYear, now));

        // Últimos 10 pedidos (para tabla rápida)
        List<Order> recentOrders = orderRepository.findTop10ByOrderByOrderDateDesc();
        stats.put("recentOrders", recentOrders);

        return ResponseEntity.ok(stats);
    }

    /**
     * Moderación: Eliminar un comentario inadecuado.
     */
    @Operation(summary = "Eliminar comentario", description = "Permite a un admin borrar una reseña ofensiva o spam.")
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