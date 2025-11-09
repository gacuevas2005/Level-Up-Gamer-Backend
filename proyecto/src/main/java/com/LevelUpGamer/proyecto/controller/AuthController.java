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
@RequestMapping("/api/auth") // Ruta base para autenticación (registro, login, etc.)
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // Inyectamos el "hasheador" de contraseñas que creamos en SecurityConfig
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager; // El "verificador"

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

        // 1. Comprobar si el nombre de usuario ya existe
        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            // Mensaje en español para el frontend
            return ResponseEntity
                    .badRequest()
                    .body("Error: ¡El nombre de usuario ya está en uso!");
        }

        // 2. Comprobar si el email ya existe
        if (userRepository.findByEmail(newUser.getEmail()).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: ¡El email ya está en uso!");
        }

        // 3. ¡HASHEAR la contraseña!
        // Tomamos la contraseña de texto plano (newUser.getPassword())
        // y la reemplazamos por su versión hasheada.
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

        // 4. Guardar el usuario en la base de datos
        userRepository.save(newUser);

        // Devolvemos una respuesta 200 OK
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

        // 1. Autenticar al usuario
        // Esto usa el 'AuthenticationProvider' que definiremos en SecurityConfig.
        // Automáticamente comprueba si el usuario existe y si el hash de la
        // contraseña es correcto. Si no, lanza una excepción.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        // 2. Si la autenticación fue exitosa, obtenemos los detalles del usuario
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. Generamos el token JWT
        String token = jwtService.generateToken(userDetails);

        // 4. Devolvemos el token en la respuesta
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
