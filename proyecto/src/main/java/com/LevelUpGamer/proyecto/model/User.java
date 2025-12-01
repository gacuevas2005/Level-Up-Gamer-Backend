package com.LevelUpGamer.proyecto.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

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
    @JsonIgnore // La contraseña nunca se envía al frontend
    private String password;

    private LocalDate dateOfBirth;

    private boolean receiveNotifications;

    private String profilePictureUrl;

    @Column(nullable = false)
    private String userRole; // "ROLE_ADMIN", "ROLE_USER", etc.

    // 'false' significa que la cuenta está activa (no bloqueada)
    // 'true' significa que el usuario está baneado
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private boolean locked = false;
    // --------------------------------------------

    private int pointsBalance;

    private int userLevel;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int totalPointsEarned;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private Cart cart;


    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(this.userRole));
    }

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
        return !this.locked;
    }
    // -----------------------------------------------

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}