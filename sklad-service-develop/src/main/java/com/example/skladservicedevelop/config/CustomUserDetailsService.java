package com.example.skladservicedevelop.config;

import com.example.skladservicedevelop.database.model.EmployeeModel;
import com.example.skladservicedevelop.database.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        EmployeeModel employee = employeeRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + login));

        String roleWithPrefix = employee.getRole().startsWith("ROLE_")
                ? employee.getRole()
                : "ROLE_" + employee.getRole();

        return new org.springframework.security.core.userdetails.User(
                employee.getLogin(),
                employee.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority(roleWithPrefix))
        );
    }
}