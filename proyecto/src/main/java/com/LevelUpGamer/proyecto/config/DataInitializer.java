package com.LevelUpGamer.proyecto.config;

import com.LevelUpGamer.proyecto.Repository.CartRepository;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
import com.LevelUpGamer.proyecto.model.Cart;
import com.LevelUpGamer.proyecto.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Esta clase se ejecuta automáticamente cada vez que arranca Spring Boot.
 * Sirve para precargar datos iniciales (Seed Data).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Verificamos si ya existe el usuario 'admin' para no duplicarlo
        if (userRepository.findByUsername("admin").isEmpty()) {

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@levelupgamer.com");
            // ¡IMPORTANTE! Aquí se encripta la contraseña
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setUserRole("ROLE_ADMIN"); // Este es el rol clave para entrar al dashboard

            // Datos de relleno requeridos por tu modelo
            admin.setPointsBalance(100000);
            admin.setUserLevel(4); // Nivel máximo
            admin.setTotalPointsEarned(100000);
            admin.setReceiveNotifications(true);
            admin.setProfilePictureUrl("https://ui-avatars.com/api/?name=Admin+User&background=0D8ABC&color=fff");

            // Guardamos el admin
            User savedAdmin = userRepository.save(admin);

            // IMPORTANTE: Crear un carrito para el admin (para evitar NullPointerException si intenta comprar)
            Cart adminCart = new Cart();
            adminCart.setUser(savedAdmin);
            cartRepository.save(adminCart);

            System.out.println("=========================================");
            System.out.println(" USUARIO ADMIN CREADO AUTOMÁTICAMENTE");
            System.out.println(" Usuario: admin");
            System.out.println(" Clave:   admin123");
            System.out.println("=========================================");
        }
    }
}
