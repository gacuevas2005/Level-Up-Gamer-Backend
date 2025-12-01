package com.LevelUpGamer.proyecto.Repository;

import com.LevelUpGamer.proyecto.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product p " +
            "WHERE o.user.id = :userId")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);


    // Suma total de ventas (dinero) en un rango de fechas
    @Query("SELECT COALESCE(SUM(o.finalPrice), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Double sumTotalSalesBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Cantidad total de pedidos en un rango de fechas
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    Long countOrdersBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Obtener los pedidos recientes (para una tabla en el dashboard)
    List<Order> findTop10ByOrderByOrderDateDesc();
}