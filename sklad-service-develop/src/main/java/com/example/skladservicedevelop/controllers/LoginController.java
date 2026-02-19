package com.example.skladservicedevelop.controllers;

import com.example.skladservicedevelop.dto.request.LoginRequest;
import com.example.skladservicedevelop.dto.response.LoginResponse;
import com.example.skladservicedevelop.service.LoginService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class LoginController {
    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        System.out.println("=== ПОЛУЧЕН ЗАПРОС НА ЛОГИН: " + request.getUsername() + " ===");
        return ResponseEntity.ok(loginService.login(request));
    }
}
