package com.example.login_server.login.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Setter
@Getter
public class UserResponse {
    private long userId;
    private String username;
    private String nickname;
    private String job;

}
