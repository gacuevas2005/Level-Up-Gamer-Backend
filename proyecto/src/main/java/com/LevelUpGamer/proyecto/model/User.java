package com.LevelUpGamer.proyecto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Entity
@Table(name = "users")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    private LocalDate dateOfBirth;

    private boolean receiveNotifications;

    private String profilePictureUrl;

    @Column(nullable = false)
    private String userRole; // Aquí se guarda "ROLE_ADMIN", "ROLE_USER", etc.

    private int pointsBalance;

    private int userLevel;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int totalPointsEarned;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Cart cart;

    // --- ¡AQUÍ ESTÁ LA CORRECCIÓN! ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Usamos el valor real de la variable 'userRole'
        // Esto permite que Spring Security reconozca si eres ADMIN o DUOC
        return Collections.singletonList(new SimpleGrantedAuthority(this.userRole));
    }
    // ---------------------------------

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}