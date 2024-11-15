package com.example.login_server.login.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;

@Getter
@Setter
public class CustomUserDetails implements UserDetails {

    private Long userId;
    private String username;
    private String nickname;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long userId, String username, String nickname, String password) {
        // roles 권한 필요 시 아래 로직 구현 필요!
        // Collection<? extends GrantedAuthority> authorities
        // this.authorities = authorities;

        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.password = password;

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
