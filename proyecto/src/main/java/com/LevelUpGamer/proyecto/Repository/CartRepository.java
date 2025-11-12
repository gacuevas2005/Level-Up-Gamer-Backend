package com.LevelUpGamer.proyecto.Repository;

import com.LevelUpGamer.proyecto.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // Método para encontrar un carrito por el ID del usuario
    Optional<Cart> findByUserId(Long userId);

    // Método para encontrar un carrito por el nombre de usuario
    Optional<Cart> findByUserUsername(String username);
}