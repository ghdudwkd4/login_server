package com.example.login_server.login.controller;

import com.example.login_server.login.entity.User;
import com.example.login_server.login.service.UserService;
import com.example.login_server.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        // Get refresh token from the request
        Cookie[] cookies = request.getCookies();
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken != null && JWTUtil.validateToken(refreshToken , JWTUtil.extractUsername(refreshToken))) {
            String username = JWTUtil.extractUsername(refreshToken);
            String newAccessToken = JWTUtil.generateToken(username);

            // Set new access token in an HTTP-only cookie
            Cookie newAccessTokenCookie = new Cookie("access_token", newAccessToken);
            newAccessTokenCookie.setHttpOnly(true);
            newAccessTokenCookie.setMaxAge(60 * 60); // 1 hour
            newAccessTokenCookie.setPath("/");
            response.addCookie(newAccessTokenCookie);

            return ResponseEntity.ok("Token refreshed");
        }

        return ResponseEntity.status(403).body("Invalid refresh token");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User user , HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            String token = JWTUtil.generateToken(user.getUsername());
            String refreshToken = JWTUtil.generateRefreshToken(user.getUsername());


            // HTTP 전용 쿠키에 JWT 설정
            Cookie cookie = new Cookie("access_token", token);
            cookie.setHttpOnly(true); // 스크립트가 쿠키에 액세스하는 것을 방지
            cookie.setMaxAge(60 * 60); // 초 단위의 토큰 만료
            cookie.setPath("/"); // 전체 앱에서 사용할 수 있도록 설정
            response.addCookie(cookie);

            // HTTP 전용 쿠키에 JWT 설정
            Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7); // 1 주일
            refreshTokenCookie.setPath("/");
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(token);  // JWT 반환
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return ResponseEntity.status(401).body("사용자를 찾을 수 없습니다.");
        }
    }

    @PostMapping("/existsByUsername")
    public ResponseEntity<?> existsByUsername(@RequestBody User user) {
        if(userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.ok().body("이미 사용중인 이메일입니다.");
        } else {
            return ResponseEntity.ok("");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("이미 사용중인 이메일입니다.");
        }
        userService.saveUser(user);
        return ResponseEntity.ok("회원가입 되었습니다.");
    }
}