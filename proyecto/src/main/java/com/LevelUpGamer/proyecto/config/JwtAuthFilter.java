package com.LevelUpGamer.proyecto.config;

import com.LevelUpGamer.proyecto.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 1. Validar que hay token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Cargamos datos básicos del usuario para asegurar que existe
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // --- AQUÍ ESTÁ LA SOLUCIÓN MÁGICA ---
                    // Extraemos los roles DIRECTAMENTE del token json
                    List<String> rolesFromToken = jwtService.extractClaim(jwt, claims -> claims.get("roles", List.class));

                    Collection<? extends GrantedAuthority> authorities;

                    if (rolesFromToken != null) {
                        // Si el token tiene roles, los usamos
                        authorities = rolesFromToken.stream()
                                .map(role -> new SimpleGrantedAuthority(role))
                                .collect(Collectors.toList());

                        System.out.println("✅ ADMIN ACCESS: Roles leídos del Token: " + rolesFromToken);
                    } else {
                        // Si no, usamos los de la DB (fallback)
                        authorities = userDetails.getAuthorities();
                        System.out.println("⚠️ ADMIN WARNING: Usando roles de DB (Token sin roles)");
                    }

                    // Creamos la autenticación con las autoridades CORRECTAS
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            authorities // <--- Pasamos las autoridades verificadas
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Autorizamos al usuario en el contexto
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error en autenticación JWT: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}