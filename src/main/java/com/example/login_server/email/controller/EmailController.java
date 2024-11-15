package com.example.login_server.email.controller;

import com.example.login_server.email.dto.EmailPostDto;
import com.example.login_server.email.dto.EmailResponseDto;
import com.example.login_server.email.entity.EmailMessage;
import com.example.login_server.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/send-mail")
@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    // 이메일 인증 - 요청 시 body로 인증번호 반환하도록 작성하였음
    @PostMapping("/email")
    public ResponseEntity sendJoinMail(@RequestBody EmailPostDto emailPostDto) throws Exception {
        EmailMessage emailMessage = EmailMessage.builder()
                .to(emailPostDto.getEmail())
                .subject("[DR-STUDY] 이메일 인증을 위한 인증 코드 발송")
                .build();

        String code = emailService.sendMail(emailMessage, "email");

        EmailResponseDto emailResponseDto = new EmailResponseDto();
        emailResponseDto.setCode(code);

        return ResponseEntity.ok(emailResponseDto);
    }
}
