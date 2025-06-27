// En cualquier paquete, ej. com.one.core.util
package com.one.core.uitl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderUtil {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "Pepe1234";
        String encodedPassword = encoder.encode(rawPassword);
        System.out.println("Contraseña en crudo: " + rawPassword);
        System.out.println("Contraseña Hasheada (BCrypt): " + encodedPassword);
    }
}