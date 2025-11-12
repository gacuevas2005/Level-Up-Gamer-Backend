package com.LevelUpGamer.proyecto.config;

import com.LevelUpGamer.proyecto.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Si no hay cabecera "Authorization" o no empieza con "Bearer ",
        // pasamos al siguiente filtro.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraemos el token (quitando "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // Extraemos el nombre de usuario del token
            username = jwtService.extractUsername(jwt);

            // Si tenemos nombre de usuario Y no está ya autenticado
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Cargamos los detalles del usuario desde la BD
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Validamos el token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Creamos un token de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No usamos credenciales (contraseña) aquí
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // ¡Establecemos al usuario como autenticado!
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            // Pasamos al siguiente filtro
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Si el token es inválido (expirado, malformado, etc.)
            // Simplemente no lo autenticamos y seguimos.
            // (Podríamos enviar un error 401 aquí si quisiéramos)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token JWT inválido o expirado");
            return;
        }
    }
}