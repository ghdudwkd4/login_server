package com.example.login_server.util;

import com.example.login_server.login.dto.CustomUserDetails;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JWTUtil {
    private static final String ACCESS_KEY = "your_secret_key";
    private static final String REFRESH_KEY = "your_refresh_key";
    private static final long ACCESS_TIME = 1000 * 60 * 60; // 1 시간
    private static final long REFRESH_TIME = 1000 * 60 * 60 * 24 * 7; // 1 주일

    public static String generateToken(String username , Map<String , Object> claims) {
        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TIME))
                .signWith(SignatureAlgorithm.HS256, ACCESS_KEY)
                .compact();
    }

    public static String generateRefreshToken(String username , Map<String , Object> claims) {
        return Jwts.builder()
                .setSubject(username)
                .addClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TIME)) //
                .signWith(SignatureAlgorithm.HS512, REFRESH_KEY)
                .compact();
    }

    public static String extractUsername(String token , String type) {
        return extractClaims(token , type).getSubject();
    }

    public static Map<String, Object> extractUserData(String token , String type) {
        return extractClaims(token , type);
    }

    public static boolean isTokenExpired(String token , String type) {
        return extractClaims(token , type).getExpiration().before(new Date());
    }

    public static boolean validateToken(String token, String type) {
//        return extractUsername(token , type).equals(username) && !isTokenExpired(token , type);
        return !isTokenExpired(token , type);
    }

    // Authentication 객체 생성
    public static UsernamePasswordAuthenticationToken getAuthentication(String token , String type) {
        Claims claims = extractClaims(token , type);

        // Spring Security 권한 설정
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Authentication 객체에 userId와 roles 포함하여 생성
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(claims.getSubject(), null, authorities);

        // 필요시 authToken에 추가적인 정보를 담아 컨트롤러에서 사용 가능
        authToken.setDetails(claims); // 예: userId를 setDetails로 설정

        return authToken;
    }

    private static Claims extractClaims(String token , String type) {
        return Jwts.parser()
                .setSigningKey(type.equals("access") ? ACCESS_KEY : REFRESH_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}