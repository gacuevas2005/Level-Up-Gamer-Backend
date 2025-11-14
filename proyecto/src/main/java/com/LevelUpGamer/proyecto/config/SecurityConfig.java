package com.LevelUpGamer.proyecto.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <-- Import correcto
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Se inyecta el filtro JWT
    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // Usa el servicio inyectado
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // No usa sesiones
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class) // Añade el "guardia"

                // --- Configuración de Permisos (Corregida) ---
                .authorizeHttpRequests(authz -> authz

                        // 1. Permite ver productos (Solo GET)
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()

                        // 2. Permite Login y Registro
                        .requestMatchers("/api/auth/**").permitAll()

                        // 3. ¡LA LÍNEA CLAVE! Permite ver las imágenes subidas
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

                        // 4. Todo lo demás (subir reseñas, ver perfil, etc.) requiere login
                        .anyRequest().authenticated()
                );
        // --- Fin de la Configuración ---

        return http.build();
    }
}