package com.example.spaceXdashboard_backend.dto;

import java.time.LocalDateTime;

public class ProfileResponse {
    private String username;
    private String email;
    private LocalDateTime createdAt;

    public ProfileResponse(String username, String email, LocalDateTime createdAt) {
        this.username = username;
        this.email = email;
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}