package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.dto.request.LoginRequest;
import com.example.skladservicedevelop.dto.response.LoginResponse;

public interface LoginService {
    LoginResponse login(LoginRequest request);
}
