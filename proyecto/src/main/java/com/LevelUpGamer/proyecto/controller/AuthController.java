package com.LevelUpGamer.proyecto.controller;
import com.LevelUpGamer.proyecto.model.User;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.LevelUpGamer.proyecto.dto.AuthResponse; // Importa los DTOs
import com.LevelUpGamer.proyecto.dto.LoginRequest;
import com.LevelUpGamer.proyecto.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager; // Importa el AuthManager

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

    /**
     * Endpoint para registrar un nuevo usuario.
     * Petición: POST http://localhost:8081/api/auth/register
     * @param newUser El JSON del usuario a crear (viene del body)
     * @return Una respuesta de éxito o error.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User newUser) {

        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: ¡El nombre de usuario ya está en uso!");
        }

        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: ¡El email ya está en uso!");
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        userRepository.save(newUser);

        return ResponseEntity.ok("¡Usuario registrado exitosamente!");
    }
    /**
     * Endpoint para iniciar sesión.
     * Petición: POST http://localhost:8081/api/auth/login
     * @param loginRequest El JSON con "username" y "password"
     * @return Un JSON con el "token"
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
