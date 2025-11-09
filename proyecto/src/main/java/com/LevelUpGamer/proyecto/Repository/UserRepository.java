package com.LevelUpGamer.proyecto.Repository;
import com.LevelUpGamer.proyecto.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Necesitaremos estos métodos para comprobar si un usuario ya existe

    // Busca un usuario por su 'username'
    Optional<User> findByUsername(String username);

    // Busca un usuario por su 'email'
    Optional<User> findByEmail(String email);
}