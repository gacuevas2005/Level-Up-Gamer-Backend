package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.model.Cart;
import com.LevelUpGamer.proyecto.Repository.CartRepository;

import com.LevelUpGamer.proyecto.model.User;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.LevelUpGamer.proyecto.dto.AuthResponse;
import com.LevelUpGamer.proyecto.dto.LoginRequest;
import com.LevelUpGamer.proyecto.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    // ¡NUEVA INYECCIÓN! Necesitamos el repo de carritos
    @Autowired
    private CartRepository cartRepository;

    /**
     * Endpoint para registrar un nuevo usuario.
     * ¡MODIFICADO!
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User newUser) {

        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: ¡El nombre de usuario ya está en uso!");
        }
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: ¡El email ya está en uso!");
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        // --- ¡AQUÍ ESTÁ LA LÓGICA DE ROLES! ---

        // 1. Revisa el correo
        if (newUser.getEmail() != null && newUser.getEmail().endsWith("@duocuc.cl")) {
            newUser.setUserRole("ROLE_DUOC");
        } else {
            newUser.setUserRole("ROLE_USER");
        }

        // 2. Inicializa los puntos y el nivel
        newUser.setPointsBalance(0);
        newUser.setUserLevel(1);



        // 1. Guarda el usuario
        User savedUser = userRepository.save(newUser);

        // 2. ¡Crea un carrito vacío para este nuevo usuario!
        Cart newCart = new Cart();
        newCart.setUser(savedUser); // Asocia el carrito al usuario que acabamos de guardar
        cartRepository.save(newCart); // Guarda el nuevo carrito en la BD

        // --- FIN DE LA NUEVA LÓGICA ---

        return ResponseEntity.ok("¡Usuario registrado exitosamente!");
    }

    /**
     * Endpoint para iniciar sesión.
     * (Sin cambios)
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }
}