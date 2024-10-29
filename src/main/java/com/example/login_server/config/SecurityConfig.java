package com.example.login_server.config;

import com.example.login_server.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (API 불필요)
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/api/auth/**","/api/send-mail/**").permitAll() // 인증 관련 API
                        .anyRequest().authenticated() // 그 외의 요청은 인증 필요
                )
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                /* API 서버로 이용할거라 login, logout 주석
                .formLogin((form) -> form
                        .loginPage("/login") // 커스텀 로그인 페이지
                        .defaultSuccessUrl("/dashboard") // 로그인 성공 후 리다이렉트할 페이지
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login")
                        .permitAll()
                )
                */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 사용하지 않음 (JWT)
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager 빈을 수동으로 설정 (필요한 경우)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // CORS 설정 정의
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:63342")); // 프론트엔드 서버 도메인 추가
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // 허용할 HTTP 메서드
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "dataType", "async")); // 허용할 헤더
        configuration.setAllowCredentials(true); // 인증 정보 포함 여부 (필요시 설정)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 CORS 설정 적용

        return source;
    }
}
