package com.waira.waira_v2.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    public AuthController() {
        
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(HttpSession session) {
        return null;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpSession session) {
        return null;
    }
}
