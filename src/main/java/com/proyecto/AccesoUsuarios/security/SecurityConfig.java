package com.proyecto.AccesoUsuarios.security;

import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF solo para las rutas API públicas, necesario para poder recibir conexiones de la app móvil
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/admin/usuarios/verificaciones-delivery/**"))
            .authorizeHttpRequests(auth -> auth
                // Endpoints que REQUIEREN autenticacion (mas especificos primero)
                .requestMatchers(
                    "/api/ordenes/mis-compras",
                    "/api/favoritos",
                    "/api/direcciones/**"
                ).authenticated()
                // Rutas publicas permitidas sin iniciar sesion (APIs moviles y recursos)
                .requestMatchers(
                    "/", 
                    "/login", 
                    "/registro", 
                    "/registro/guardar", 
                    "/api/usuarios/**", 
                    "/api/productos/**",
                    "/api/campesino/**",
                    "/api/ordenes/crear",
                    "/api/resenas/**",
                    "/api/favoritos/**",
                    "/api/carrito/**",
                    "/api/soporte/**",
                    "/api/pedidos/**",
                    "/api/reputacion/**",
                    "/api/analiticas/**",
                    "/api/finanzas/**",
                    "/api/cliente/**",
                    "/api/delivery/**",
                    "/api/tracking/**",
                    "/api/rutas/**",
                    "/css/**", 
                    "/js/**", 
                    "/img/**", 
                    "/images/**",
                    "/error",
                    "/politica-cookies",
                    "/politica-privacidad",
                    "/terminos-y-condiciones",
                    "/contacto",
                    "/como-funciona",
                    "/impacto-social",
                    "/sobre-nosotros",
                    "/api/horeca/**",
                    "/api/ia/**"
                ).permitAll()
                
                // Rutas protegidas por ROL de tu plataforma web
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/soporte/**").hasRole("SOPORTE")
                .requestMatchers("/campesino/**").hasRole("CAMPESINO")
                .requestMatchers("/delivery/**").hasRole("REPARTIDOR")
                
                // Vistas comunes de la web que requieren autenticación
                .requestMatchers(
                    "/carrito/**", 
                    "/checkout/**", 
                    "/compra-exitosa/**", 
                    "/mis-compras/**", 
                    "/favoritos/**", 
                    "/perfil/**", 
                    "/direccion/**"
                )                .hasAnyRole("CLIENTE", "CAMPESINO", "ADMIN", "SOPORTE", "REPARTIDOR")
                
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(agroConectaSuccessHandler()) // Redirección inteligente web
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/login?denied");
                })
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Tu Handler inteligente original para redireccionar según el rol del usuario en la web
    @Bean
    public AuthenticationSuccessHandler agroConectaSuccessHandler() {
        return (request, response, authentication) -> {
            Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());

            if (roles.contains("ROLE_ADMIN")) {
                response.sendRedirect("/admin/dashboard");
            } else if (roles.contains("ROLE_SOPORTE")) {
                response.sendRedirect("/soporte/dashboard");
            } else if (roles.contains("ROLE_CAMPESINO")) {
                response.sendRedirect("/campesino/productos");
            } else if (roles.contains("ROLE_REPARTIDOR")) {
                response.sendRedirect("/delivery/dashboard");
            } else if (roles.contains("ROLE_CLIENTE")) {
                response.sendRedirect("/tienda");
            } else {
                response.sendRedirect("/home");
            }
        };
    }
}