package com.LevelUpGamer.proyecto.controller;

// DTOs (Cajas de datos)
import com.LevelUpGamer.proyecto.dto.ChangePasswordRequest;
import com.LevelUpGamer.proyecto.dto.ProfileResponse;
// Modelo y Repositorio
import com.LevelUpGamer.proyecto.model.User;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
// Servicios
import com.LevelUpGamer.proyecto.service.FileStorageService;
// Utilidad para la extensión del archivo
import org.apache.commons.io.FilenameUtils;
// Imports de Spring
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Imports de Java
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/profile") // Todas las rutas aquí están protegidas
public class ProfileController {

    // --- Inyección de Dependencias ---

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService; // El servicio para guardar archivos

    // --- Endpoints ---

    /**
     * OBTENER el perfil del usuario actualmente logueado.
     * Petición: GET /api/profile/me
     */
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(new ProfileResponse(user));
    }

    /**
     * ACTUALIZAR la información del perfil (username, email, notificaciones).
     * Petición: PUT /api/profile/me
     */
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestBody Map<String, Object> updates) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validar y actualizar campos de texto
        if (updates.containsKey("username")) {
            String newUsername = updates.get("username").toString();
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

        // Guardar los cambios en la BD
        User updatedUser = userRepository.save(currentUser);
        return ResponseEntity.ok(new ProfileResponse(updatedUser));
    }

    /**
     * Sube o actualiza la FOTO de perfil del usuario logueado.
     * Petición: POST /api/profile/picture
     */
    @PostMapping("/picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file) {

        // 1. Obtener el usuario actual
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            // 2. Generar un nombre de archivo único (ej: user_1_avatar.png)
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String newFilename = "user_" + currentUser.getId() + "_avatar." + extension;

            // 3. Guardar el archivo en el disco (en la carpeta 'uploads')
            fileStorageService.store(file, newFilename);

            // 4. --- ¡LA CORRECCIÓN CRÍTICA! ---
            // Guardar la RUTA PÚBLICA ABSOLUTA en la BD
            String publicPath = "http://localhost:8081/uploads/" + newFilename;
            currentUser.setProfilePictureUrl(publicPath);
            userRepository.save(currentUser);

            // 5. Devolver la nueva ruta al frontend
            return ResponseEntity.ok(Map.of("profilePictureUrl", publicPath));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al subir el archivo: " + e.getMessage());
        }
    }

    /**
     * CAMBIAR la contraseña del usuario.
     * Petición: POST /api/profile/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // 1. Verificar que la contraseña antigua es correcta
        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            return ResponseEntity.badRequest().body("Error: La contraseña antigua es incorrecta.");
        }

        // 2. Verificar que la nueva contraseña no esté vacía
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Error: La nueva contraseña no puede estar vacía.");
        }

        // 3. Hashear y guardar la nueva contraseña
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        return ResponseEntity.ok("Contraseña actualizada exitosamente.");
    }
}