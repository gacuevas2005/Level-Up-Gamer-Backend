package com.LevelUpGamer.proyecto.controller;

// DTOs
import com.LevelUpGamer.proyecto.dto.ChangePasswordRequest;
import com.LevelUpGamer.proyecto.dto.ProfileResponse;
// Modelo y Repositorio
import com.LevelUpGamer.proyecto.model.User;
import com.LevelUpGamer.proyecto.Repository.UserRepository;
// Servicios
import com.LevelUpGamer.proyecto.service.FileStorageService;
// Swagger Imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// Utilidad
import org.apache.commons.io.FilenameUtils;
// Spring Imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Perfil de Usuario", description = "Operaciones para ver y editar el perfil, subir foto y cambiar contraseña.")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * OBTENER el perfil.
     */
    @Operation(summary = "Obtener mi perfil", description = "Devuelve la información del usuario actualmente autenticado (basado en el Token JWT).")
    @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = ProfileResponse.class)))
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(new ProfileResponse(user));
    }

    /**
     * ACTUALIZAR la información del perfil.
     */
    @Operation(summary = "Actualizar datos del perfil", description = "Permite cambiar username, email o preferencias. Valida que el nuevo username/email no estén en uso.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "El nombre de usuario o email ya están en uso")
    })
    @PutMapping("/me")
    public ResponseEntity<?> updateMyProfile(@RequestBody Map<String, Object> updates) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

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

        User updatedUser = userRepository.save(currentUser);
        return ResponseEntity.ok(new ProfileResponse(updatedUser));
    }

    /**
     * Sube la FOTO de perfil.
     */
    @Operation(summary = "Subir foto de perfil", description = "Sube una imagen (jpg, png), la guarda en el servidor y actualiza la URL del perfil.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Foto subida exitosamente. Retorna la nueva URL pública."),
            @ApiResponse(responseCode = "400", description = "Error al leer el archivo o formato inválido")
    })
    @PostMapping(value = "/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProfilePicture(
            @Parameter(description = "Archivo de imagen a subir", required = true)
            @RequestParam("file") MultipartFile file) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        try {
            String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String newFilename = "user_" + currentUser.getId() + "_avatar." + extension;

            fileStorageService.store(file, newFilename);

            String publicPath = "http://localhost:8081/uploads/" + newFilename;
            currentUser.setProfilePictureUrl(publicPath);
            userRepository.save(currentUser);

            return ResponseEntity.ok(Map.of("profilePictureUrl", publicPath));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al subir el archivo: " + e.getMessage());
        }
    }

    /**
     * CAMBIAR la contraseña.
     */
    @Operation(summary = "Cambiar contraseña", description = "Verifica la contraseña actual y actualiza a la nueva contraseña encriptada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Contraseña actualizada"),
            @ApiResponse(responseCode = "400", description = "La contraseña antigua es incorrecta o la nueva es inválida")
    })
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
            return ResponseEntity.badRequest().body("Error: La contraseña antigua es incorrecta.");
        }

        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Error: La nueva contraseña no puede estar vacía.");
        }

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        return ResponseEntity.ok("Contraseña actualizada exitosamente.");
    }
}