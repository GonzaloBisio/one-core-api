package com.one.core.application.controller.auth;

import com.one.core.application.dto.auth.LoginRequestDTO;
import com.one.core.application.dto.auth.LoginResponseDTO;
import com.one.core.application.security.JwtTokenProvider;
import com.one.core.application.security.UserPrincipal; // Tu UserDetails personalizado
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Extrae el tenantId del UserPrincipal (que fue cargado por CustomUserDetailsService)
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String tenantId = userPrincipal.getTenantId();

        // Genera el token JWT incluyendo el tenantId
        String jwt = tokenProvider.generateToken(authentication, tenantId);
        return ResponseEntity.ok(new LoginResponseDTO(jwt));
    }
}