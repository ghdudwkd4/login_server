package com.example.login_server.login.controller;

import com.example.login_server.login.dto.CustomUserDetails;
import com.example.login_server.login.entity.User;
import com.example.login_server.login.service.UserService;
import com.example.login_server.util.JWTUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        // Get refresh token from the request
        Cookie[] cookies = request.getCookies();
        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken != null && JWTUtil.validateToken(refreshToken , "refresh")) {
            Map<String , Object> user = JWTUtil.extractUserData(refreshToken, "refresh");

            Map<String, Object> claims = new HashMap<>();
            claims.put("nickname" , user.get("nickname").toString());
            claims.put("userId" , user.get("userId").toString());
            String newAccessToken = JWTUtil.generateToken(JWTUtil.extractUsername(refreshToken, "refresh"), claims);

            // HTTP 전용 쿠키에 JWT 설정
            Cookie newAccessTokenCookie = new Cookie("access_token", newAccessToken);
            newAccessTokenCookie.setHttpOnly(true);
            newAccessTokenCookie.setMaxAge(60 * 60); // 1 hour
            newAccessTokenCookie.setPath("/");
//            newAccessTokenCookie.setSecure(true); https 설정
            response.addCookie(newAccessTokenCookie);

            return ResponseEntity.ok(user.get("userId").toString());
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User user , HttpServletResponse response) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword())
            );

            // SecurityContextHolder 에 Authentication 셋팅
            SecurityContextHolder.getContext().setAuthentication(auth);

            // token 생성 시 추후 nickname 도 같이 얻기 위해 Authentication 에서 조회 후 세팅
            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            Map<String , Object> claims = new HashMap<>();
            claims.put("nickname", userDetails.getNickname());
            claims.put("userId", userDetails.getUserId());

            String token = JWTUtil.generateToken(user.getUsername() , claims);
            String refreshToken = JWTUtil.generateRefreshToken(user.getUsername() , claims);

            // HTTP 전용 쿠키에 JWT 설정
            Cookie cookie = new Cookie("access_token", token);
            cookie.setHttpOnly(true); // 스크립트가 쿠키에 액세스하는 것을 방지
            cookie.setMaxAge(60 * 60); // (초 , 분 , 시 , 일) 60*60 = 1시간
            cookie.setPath("/"); // 전체 앱에서 사용할 수 있도록 설정
//            cookie.setSecure(true); https 설정
            response.addCookie(cookie);

            // HTTP 전용 쿠키에 JWT 설정
            Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7); // 1 주일
            refreshTokenCookie.setPath("/");
//            refreshTokenCookie.setSecure(true); https 설정
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(userDetails.getUserId());  // User ID 반환
        } catch (AuthenticationException e) {
            e.printStackTrace();
            return ResponseEntity.status(404).body("사용자를 찾을 수 없습니다.");
        }
    }

    @PostMapping("/existsByUsername")
    public ResponseEntity<?> existsByUsername(@RequestBody User user) {
        if(userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.ok().body(false);
        } else {
            return ResponseEntity.ok().body(true);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        if (userService.existsByUsername(user.getUsername())) {
            return ResponseEntity.badRequest().body("이미 사용중인 이메일 입니다.");
        }
        if (userService.existsByNickname(user.getNickname())) {
            return ResponseEntity.badRequest().body("이미 사용중인 닉네임 입니다.");
        }
        userService.saveUser(user);
        return ResponseEntity.ok("회원가입 되었습니다.");
    }

    @PostMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody User user) throws Exception {
        try {
            userService.updatePassword(user);
            return ResponseEntity.ok("비밀번호가 정상적으로 변경 되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/user")
    public ResponseEntity<?> findNotInUserId(HttpServletRequest request , HttpServletResponse response) throws Exception {
        return ResponseEntity.ok(userService.findNotInUserId());
    }
}