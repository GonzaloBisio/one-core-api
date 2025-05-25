package com.one.core.application.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data // Lombok
public class LoginRequestDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}