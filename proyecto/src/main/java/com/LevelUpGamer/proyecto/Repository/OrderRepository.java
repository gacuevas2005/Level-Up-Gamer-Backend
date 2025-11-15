package com.LevelUpGamer.proyecto.Repository;

import com.LevelUpGamer.proyecto.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // Para un futuro historial de pedidos
    List<Order> findByUserId(Long userId);
}