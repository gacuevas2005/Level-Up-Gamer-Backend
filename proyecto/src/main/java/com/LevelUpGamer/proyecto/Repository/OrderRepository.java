package com.LevelUpGamer.proyecto.Repository;

import com.LevelUpGamer.proyecto.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.product p " +
            "WHERE o.user.id = :userId")
    List<Order> findByUserIdWithItems(@Param("userId") Long userId);
}