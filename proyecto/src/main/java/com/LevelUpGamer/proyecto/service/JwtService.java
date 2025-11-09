package com.LevelUpGamer.proyecto.service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;

import java.security.Key;
import java.util.Date;
import java.util.Base64;

@Service
public class JwtService {

    private final Key secretKey;
    private final long expirationTime = 1000 * 60 * 60 * 24; // 24 horas

    // Lee la clave secreta desde application.properties
    public JwtService(@Value("${jwt.secret}") String secretString) {
        byte[] keyBytes = secretString.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // Genera un token para un usuario
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // (Añadiremos más métodos aquí después para validar el token)
}