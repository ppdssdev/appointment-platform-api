package com.appointments_ws.appointments_ws.domain.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {
}
