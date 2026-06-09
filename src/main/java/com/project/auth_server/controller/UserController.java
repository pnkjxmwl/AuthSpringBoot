package com.project.auth_server.controller;

import com.project.auth_server.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    // Accessible by both USER and ADMIN
    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
            "id",    userDetails.getId(),
            "email", userDetails.getEmail(),
            "role",  userDetails.getAuthorities().toString()
        ));
    }

    // Accessible only by ADMIN
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminDashboard() {
        return ResponseEntity.ok(Map.of(
            "message", "Welcome to the Admin Dashboard",
            "status",  "secure"
        ));
    }

    // Accessible only by ADMIN — list all users (example)
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> listUsers() {
        return ResponseEntity.ok(Map.of("message", "Admin: User list endpoint"));
    }
}