package com.LevelUpGamer.proyecto.model;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate; // Para la fecha de nacimiento
import jakarta.persistence.*;
import lombok.Data;
// 1. IMPORTA las clases de Spring Security
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@Data
@Entity
@Table(name = "users")
// 2. IMPLEMENTA la interfaz UserDetails
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private LocalDate dateOfBirth;

    // --- 3. IMPLEMENTACIÓN DE LOS MÉTODOS DE UserDetails ---
    // (Spring usará estos métodos para el proceso de autenticación)

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Por ahora, todos nuestros usuarios tendrán un rol simple: "USER"
        // Esto es necesario para la autenticación.
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        // Le dice a Spring cuál es el campo de la contraseña (hasheada)
        return this.password;
    }

    @Override
    public String getUsername() {
        // Le dice a Spring cuál es el campo del nombre de usuario
        return this.username;
    }

    // --- Métodos que no necesitamos ahora, pero son requeridos ---

    @Override
    public boolean isAccountNonExpired() {
        return true; // Asumimos que las cuentas nunca expiran
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Asumimos que las cuentas nunca se bloquean
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Asumimos que las credenciales nunca expiran
    }

    @Override
    public boolean isEnabled() {
        return true; // Asumimos que todos los usuarios están habilitados
    }
}