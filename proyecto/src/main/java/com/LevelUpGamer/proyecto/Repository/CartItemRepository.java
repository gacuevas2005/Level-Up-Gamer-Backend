package com.LevelUpGamer.proyecto.Repository;

import com.LevelUpGamer.proyecto.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // (Podríamos añadir búsquedas personalizadas aquí, pero por ahora
    // lo manejaremos desde el CartRepository)
}
