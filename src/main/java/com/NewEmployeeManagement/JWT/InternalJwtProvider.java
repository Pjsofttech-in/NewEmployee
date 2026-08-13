package com.NewEmployeeManagement.JWT;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static io.jsonwebtoken.Jwts.SIG.HS256;


@Component
public class InternalJwtProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String generateInternalToken() {
        return Jwts.builder().issuer("employee-service")
                .claim("type", "INTERNAL")
                .claim("scope", "ATTENDANCE-ACCESS").issuedAt(new Date()).expiration(
                        new Date(System.currentTimeMillis() + 2 * 60 * 60 * 1000) // 24 hours
                )
                .signWith(
                        Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)),
                        HS256
                )
                .compact();
    }
}
