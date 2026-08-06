package com.employeemgmt.controller;

import com.employeemgmt.dto.AuthResponse;
import com.employeemgmt.dto.LoginRequest;
import com.employeemgmt.dto.RegisterRequest;
import com.employeemgmt.model.User;
import com.employeemgmt.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        String userId = requireUserId(authentication);
        User user = authService.getCurrentUser(userId);
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "role", user.getRole()
        ));
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(Authentication authentication, @RequestBody Map<String, String> body) {
        String userId = requireUserId(authentication);
        return ResponseEntity.ok(authService.updateProfile(userId, body));
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody Map<String, String> body) {
        String userId = requireUserId(authentication);
        authService.changePassword(userId, body);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    private String requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return authentication.getName();
    }
}
