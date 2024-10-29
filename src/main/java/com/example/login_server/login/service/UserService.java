package com.example.login_server.login.service;

import com.example.login_server.login.entity.User;
import com.example.login_server.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()  // 권한 설정 (필요 시 수정 가능)
        );

    }

    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));  // 비밀번호 암호화
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }


    public void updatePassword(String to, String authNum) throws Exception {
        userRepository.updateUserByPassword(to , passwordEncoder.encode(authNum));
    }
}
