package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.Repository.CartRepository;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
import com.LevelUpGamer.proyecto.dto.AuthResponse;
import com.LevelUpGamer.proyecto.dto.LoginRequest;
import com.LevelUpGamer.proyecto.dto.RegisterRequest;
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
            description = "Crea una cuenta nueva. Asigna automáticamente rol, puntos iniciales (0) y crea un carrito vacío. Si el email es @duocuc.cl, asigna rol DUOC."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "El nombre de usuario o el correo electrónico ya están en uso")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {

        // 1. Validaciones previas usando los datos del request
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: ¡El nombre de usuario ya está en uso!");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: ¡El email ya está en uso!");
        }

        // 2. Crear la entidad User y pasarle los datos
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setDateOfBirth(request.getDateOfBirth());

        // 3. Encriptar contraseña
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        System.out.println("Iniciando registro para: " + newUser.getEmail());

        // A. Asignar Rol
        if (newUser.getEmail() != null && newUser.getEmail().endsWith("@duocuc.cl")) {
            newUser.setUserRole("ROLE_DUOC");
            System.out.println("Rol asignado: ROLE_DUOC");
        } else {
            newUser.setUserRole("ROLE_USER");
            System.out.println("Rol asignado: ROLE_USER");
        }

        // B. Inicializar Puntos y Nivel
        newUser.setPointsBalance(0);
        newUser.setTotalPointsEarned(0);
        newUser.setUserLevel(1);
        System.out.println("Puntos y nivel inicializados.");

        // ---------------------------------------------------

        try {
            // 4. Guardar el usuario
            User savedUser = userRepository.save(newUser);
            System.out.println("Usuario guardado con ID: " + savedUser.getId() + " y Rol: " + savedUser.getUserRole());

            // 5. Crear un carrito vacío asociado al usuario
            Cart newCart = new Cart();
            newCart.setUser(savedUser);
            cartRepository.save(newCart);
            System.out.println("Carrito creado para el usuario.");

            return ResponseEntity.ok("¡Usuario registrado exitosamente!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al guardar el usuario: " + e.getMessage());
        }
    }

    /**
     * Endpoint para iniciar sesión.
     */
    @Operation(
            summary = "Iniciar sesión",
            description = "Autentica las credenciales y devuelve un Token JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
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