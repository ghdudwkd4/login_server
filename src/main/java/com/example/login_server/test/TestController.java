package com.example.login_server.test;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @PostMapping("/test")
    public ResponseEntity<?> test() {
        try {
            return ResponseEntity.ok("Hello World");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(e.getMessage());
        }

    }
}
