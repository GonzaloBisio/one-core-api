package com.one.core.application.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data // Lombok
@AllArgsConstructor // Lombok
public class LoginResponseDTO {
    private String accessToken;
    private String tokenType = "Bearer";

    public LoginResponseDTO(String accessToken) {
        this.accessToken = accessToken;
    }
}