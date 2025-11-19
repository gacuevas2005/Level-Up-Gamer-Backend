package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.Repository.CartRepository;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
import com.LevelUpGamer.proyecto.dto.AuthResponse;
import com.LevelUpGamer.proyecto.dto.LoginRequest;
import com.LevelUpGamer.proyecto.model.Cart;
import com.LevelUpGamer.proyecto.model.User;
import com.LevelUpGamer.proyecto.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para el registro de nuevos usuarios y el inicio de sesión (obtención de Token JWT).")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CartRepository cartRepository;

    /**
     * Endpoint para registrar un nuevo usuario.
     */
    @Operation(
            summary = "Registrar un nuevo usuario",
            description = "Crea una cuenta nueva en el sistema. " +
                    "Si el correo termina en '@duocuc.cl', se asigna automáticamente el rol ROLE_DUOC y beneficios. " +
                    "También inicializa un carrito de compras vacío para el usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El nombre de usuario o el correo electrónico ya están en uso")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User newUser) {

        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: ¡El nombre de usuario ya está en uso!");
        }
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: ¡El email ya está en uso!");
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        // --- LÓGICA DE ROLES ---
        if (newUser.getEmail() != null && newUser.getEmail().endsWith("@duocuc.cl")) {
            newUser.setUserRole("ROLE_DUOC");
        } else {
            newUser.setUserRole("ROLE_USER");
        }

        // Inicializa puntos y nivel
        newUser.setPointsBalance(0);
        newUser.setTotalPointsEarned(0);
        newUser.setUserLevel(1);

        // 1. Guarda el usuario
        User savedUser = userRepository.save(newUser);

        // 2. Crea un carrito vacío para este nuevo usuario
        Cart newCart = new Cart();
        newCart.setUser(savedUser);
        cartRepository.save(newCart);

        return ResponseEntity.ok("¡Usuario registrado exitosamente!");
    }

    /**
     * Endpoint para iniciar sesión.
     */
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica las credenciales del usuario (username y password) y devuelve un Token JWT. " +
                    "Este token debe usarse en la cabecera 'Authorization' para acceder a rutas protegidas."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso, devuelve el token"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas (usuario o contraseña no válidos)")
    })
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