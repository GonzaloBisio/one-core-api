package com.one.core.config;

import com.one.core.application.exception.CustomAuthenticationEntryPoint;
import com.one.core.application.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        String hierarchy = String.join("\n",
                "ROLE_SUPER_ADMIN > ROLE_TENANT_ADMIN",
                "ROLE_TENANT_ADMIN > ROLE_TENANT_USER",
                "ROLE_SUPER_ADMIN > ROLE_PURCHASING_MANAGER",
                "ROLE_SUPER_ADMIN > ROLE_INVENTORY_MANAGER",
                "ROLE_SUPER_ADMIN > ROLE_SALES_MANAGER",
                "ROLE_SUPER_ADMIN > ROLE_SALES_PERSON",
                "ROLE_SUPER_ADMIN > ROLE_WAREHOUSE_STAFF",
                "ROLE_SUPER_ADMIN > ROLE_PRODUCTION_MANAGER"
        );
        roleHierarchy.setHierarchy(hierarchy);
        return roleHierarchy;
    }

    @Bean
    public static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setRoleHierarchy(roleHierarchy);
        return expressionHandler;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/swagger-ui.html", "/v3/api-docs/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/auth/**", "/actuator/**", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()

                        // --- HARDENING POR PATHS ---
                        // Purchase Orders: bloquear TENANT_USER (tanto GET como POST/PUT/etc)
                        .requestMatchers("/purchase-orders/**")
                        .hasAnyRole("TENANT_ADMIN","PURCHASING_MANAGER","SUPER_ADMIN")

                        // Reports: lectura para TENANT_USER
                        .requestMatchers(HttpMethod.GET, "/api/reports/**")
                        .hasAnyRole("TENANT_USER","TENANT_ADMIN","SUPER_ADMIN")

                        // Productos / Categorías: GET para TENANT_USER, escritura solo admins
                        .requestMatchers(HttpMethod.GET, "/products/**", "/product-categories/**")
                        .hasAnyRole("TENANT_USER","TENANT_ADMIN","SUPER_ADMIN")
                        .requestMatchers("/products/**", "/product-categories/**")
                        .hasAnyRole("TENANT_ADMIN","SUPER_ADMIN")

                        // Inventario: solo lectura para TENANT_USER
                        .requestMatchers(HttpMethod.GET, "/inventory/**")
                        .hasAnyRole("TENANT_USER","TENANT_ADMIN","INVENTORY_MANAGER","SUPER_ADMIN")
                        .requestMatchers("/inventory/**")
                        .hasAnyRole("TENANT_ADMIN","INVENTORY_MANAGER","SUPER_ADMIN")

                        // Ventas / Producción: GET visibles para TENANT_USER (ajustá si querés)
                        .requestMatchers(HttpMethod.GET, "/sales-orders/**", "/production-orders/**", "/event-orders/**")
                        .hasAnyRole("TENANT_USER","TENANT_ADMIN","SUPER_ADMIN")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // --- ORÍGENES PERMITIDOS ---
        // Se especifican los dominios exactos desde los que se permitirán las peticiones.
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://one-core-sistems.vercel.app"
        ));

        // --- MÉTODOS HTTP PERMITIDOS ---
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        ));

        // --- CABECERAS PERMITIDAS ---
        // Es una mejor práctica ser explícito en lugar de usar "*", especialmente cuando se usan credenciales.
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // --- CABECERAS EXPUESTAS ---
        // Permite al frontend leer cabeceras como 'Content-Disposition' para la descarga de archivos.
        configuration.setExposedHeaders(Arrays.asList(
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials",
                "Content-Disposition"
        ));

        // --- PERMITIR CREDENCIALES ---
        // Crucial para que las cookies y los tokens de autorización funcionen desde otro origen.
        configuration.setAllowCredentials(true);

        // --- CACHÉ DE PREFLIGHT ---
        // El navegador puede cachear la respuesta a la petición OPTIONS por este tiempo (en segundos).
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica esta configuración a todas las rutas

        return source;
    }
}
