package com.example.skladservicedevelop.service;

import com.example.skladservicedevelop.config.CustomUserDetailsService;
import com.example.skladservicedevelop.config.JwtTokenUtil;
import com.example.skladservicedevelop.database.model.EmployeeModel;
import com.example.skladservicedevelop.database.repository.EmployeeRepository;
import com.example.skladservicedevelop.dto.request.LoginRequest;
import com.example.skladservicedevelop.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {
    private final EmployeeRepository employeeRepository;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        EmployeeModel employee = employeeRepository.findByLogin(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtTokenUtil.generateToken(userDetails);

        Integer warehouseId = null;
        String warehouseName = null;

        if (employee.getWarehouse() != null) {
            warehouseId = employee.getWarehouse().getId();
            warehouseName = employee.getWarehouse().getName();
        }
        return new LoginResponse(
                token,
                employee.getRole(),
                employee.getFullName(),
                warehouseId,
                warehouseName
        );
    }
}
