package com.LevelUpGamer.proyecto.controller;

import com.LevelUpGamer.proyecto.dto.ChangePasswordRequest;
import com.LevelUpGamer.proyecto.dto.ProfileResponse;
import com.LevelUpGamer.proyecto.model.User;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/profile") // ¡Esta ruta está protegida por SecurityConfig!
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Para el cambio de clave

    /**
     * OBTENER el perfil del usuario actualmente logueado.
     * El "guardia" (JwtAuthFilter) ya validó el token.
     * Petición: GET /api/profile/me
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        // Obtenemos el "username" que el filtro JWT puso en el contexto
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado (esto no debería pasar)"));

        // Devolvemos el DTO, no la entidad (por seguridad)
        return ResponseEntity.ok(new ProfileResponse(user));
    }

    /**
     * ACTUALIZAR el perfil (username, email, notificaciones).
     * Petición: PUT /api/profile/me
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestBody Map<String, Object> updates) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).get();

        // 1. Validar y actualizar campos
        // (Usamos un Map para que el frontend pueda enviar solo lo que cambió)

        if (updates.containsKey("username")) {
            String newUsername = updates.get("username").toString();
            // Comprobamos que el nuevo username no esté ya en uso por OTRA persona
            if (userRepository.findByUsername(newUsername).isPresent() && !currentUser.getUsername().equals(newUsername)) {
                return ResponseEntity.badRequest().body("Error: El nombre de usuario ya está en uso.");
            }
            currentUser.setUsername(newUsername);
        }

        if (updates.containsKey("email")) {
            String newEmail = updates.get("email").toString();
            if (userRepository.findByEmail(newEmail).isPresent() && !currentUser.getEmail().equals(newEmail)) {
                return ResponseEntity.badRequest().body("Error: El email ya está en uso.");
            }
            currentUser.setEmail(newEmail);
        }

        if (updates.containsKey("receiveNotifications")) {
            currentUser.setReceiveNotifications((Boolean) updates.get("receiveNotifications"));
        }

        // 2. Guardar los cambios en la BD
        User updatedUser = userRepository.save(currentUser);

        // 3. Devolver el perfil actualizado
        return ResponseEntity.ok(new ProfileResponse(updatedUser));
    }

    /**
     * CAMBIAR la contraseña del usuario.
     * Petición: POST /api/profile/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).get();

        // 1. Verificar que la contraseña antigua es correcta
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            return ResponseEntity.badRequest().body("Error: La contraseña antigua es incorrecta.");
        }

        // 2. Verificar que la nueva contraseña no esté vacía
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: La nueva contraseña no puede estar vacía.");
        }

        // 3. Hashear y guardar la nueva contraseña
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        return ResponseEntity.ok("Contraseña actualizada exitosamente.");
    }
}