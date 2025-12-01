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
        if (userRepository.findByUsername("admin").isEmpty()) {

            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@levelupgamer.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setUserRole("ROLE_ADMIN");

            admin.setPointsBalance(100000);
            admin.setUserLevel(4);
            admin.setTotalPointsEarned(100000);
            admin.setReceiveNotifications(true);
            admin.setProfilePictureUrl("https://ui-avatars.com/api/?name=Admin+User&background=0D8ABC&color=fff");

            User savedAdmin = userRepository.save(admin);

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
