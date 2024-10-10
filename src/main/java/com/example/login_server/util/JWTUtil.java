package com.example.login_server.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JWTUtil {
    private static final String ACCESS_KEY = "your_secret_key";
    private static final String REFRESH_KEY = "your_refresh_key";
    private static final long ACCESS_TIME = 1000 * 60 * 60; // 1 시간
    private static final long REFRESH_TIME = 1000 * 60 * 60 * 24 * 7; // 1 주일

    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TIME))
                .signWith(SignatureAlgorithm.HS256, ACCESS_KEY)
                .compact();
    }

    public static String generateRefreshToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TIME)) //
                .signWith(SignatureAlgorithm.HS512, REFRESH_KEY)
                .compact();
    }

    public static String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public static boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public static boolean validateToken(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    private static Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(ACCESS_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}