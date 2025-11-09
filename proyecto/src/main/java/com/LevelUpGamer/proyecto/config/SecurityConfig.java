package com.LevelUpGamer.proyecto.config;
import com.LevelUpGamer.proyecto.Repository.UserRepository;// Importa el UserRepository
import org.springframework.beans.factory.annotation.Autowired; // Importa Autowired
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager; // Importa
import org.springframework.security.authentication.AuthenticationProvider; // Importa
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // Importa
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Importa
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy; // Importa
import org.springframework.security.core.userdetails.UserDetailsService; // Importa
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Importa
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // --- ¡Necesitamos el UserRepository aquí! ---
    @Autowired
    private UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ¡NUEVO!
     * Le dice a Spring Security CÓMO buscar a un usuario.
     * Cuando 'AuthenticationManager' lo necesite, llamará a esto.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    /**
     * ¡NUEVO!
     * El "proveedor" que conecta UserDetailsService y PasswordEncoder.
     * Le dice al manager: "Usa este servicio para encontrar usuarios
     * y este encriptador para comprobar las contraseñas".
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * ¡NUEVO!
     * El "Gerente" que nuestro AuthController usará para autenticar.
     * Lo exponemos como un Bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    /**
     * ¡MODIFICADO!
     * Ahora le decimos a Spring que use nuestro 'AuthenticationProvider'
     * y que NO use sesiones (porque usaremos tokens).
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
                    config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
                    config.setAllowedHeaders(Arrays.asList("*"));
                    return config;
                }))

                // ¡NUEVO! Le decimos que no maneje sesiones (somos 'STATELESS')
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ¡NUEVO! Le decimos qué proveedor de autenticación usar
                .authenticationProvider(authenticationProvider())

                .authorizeHttpRequests(authz -> authz
                                .requestMatchers("/api/products/**").permitAll()
                                .requestMatchers("/api/auth/**").permitAll() // /api/auth/register Y /api/auth/login son públicos
                        // .anyRequest().authenticated() // (Descomenta esto después para proteger otras rutas)
                );

        return http.build();
    }
}