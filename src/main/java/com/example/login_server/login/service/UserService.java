package com.example.login_server.login.service;

import com.example.login_server.login.dto.CustomUserDetails;
import com.example.login_server.login.dto.UserResponse;
import com.example.login_server.login.entity.User;
import com.example.login_server.login.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new CustomUserDetails(
                user.getUserId(),
                user.getUsername(),
                user.getNickname(),
                user.getPassword()
        );
    }

    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));  // 비밀번호 암호화
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }


    public void updatePassword(User user) throws Exception {
        userRepository.updateUserByPassword(user.getUsername() , passwordEncoder.encode(user.getPassword()));
    }

    public List<UserResponse> findNotInUserId() {
        Object obj = SecurityContextHolder.getContext().getAuthentication().getDetails();
        Map<String , Object> map = (Map<String , Object>) obj;

        List<UserResponse> list = new ArrayList<>();
        userRepository.findNotInUserId(map.get("userId").toString()).forEach(e -> {
            list.add(
                UserResponse.builder().userId(e.getUserId()).username(e.getUsername()).nickname(e.getNickname()).job(e.getJob()).build()
                );
        });
        return list;
    }
}
